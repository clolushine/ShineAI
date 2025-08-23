/*
 * ShineAI - An IntelliJ IDEA plugin for AI services.
 * Copyright (C) 2025 Shine Zhong
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details (usually in the LICENSE file).
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.shine.ai.util;

import cn.hutool.core.io.resource.FileResource;
import cn.hutool.http.HttpUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.MessageType;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.settings.LoginDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.shine.ai.settings.AIAssistantSettingsPanel.SHINE_AI_BASE_URL;


public class ShineAIUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ShineAIUtil.class);

    public static String baseUrl = SHINE_AI_BASE_URL;
    public static AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();
    public static Integer timeout = state.requestTimeout;

    public static boolean isLoginDialogShown = false; // 静态变量，跟踪弹窗状态
    // --- 新增的并发控制变量 ---
    private static final ReentrantLock REFRESH_TOKEN_LOCK = new ReentrantLock();
    private static final Condition TOKEN_REFRESHED = REFRESH_TOKEN_LOCK.newCondition();
    private static volatile boolean isTokenRefreshInProgress = false; // volatile 保证多线程可见性
    private static final int MAX_RETRIES = 2; // 最大重试次数

    public static String request(String url, String method, Object options, Integer retry) {
        // validate input retry count, ensure it's not null and within bounds
        int currentRetryCount = (retry != null && retry >= 0) ? retry : 0;
        String fullURL = baseUrl + url;

        try {
            String response;
            switch (method.toUpperCase()) {
                case "GET":
                    // 确保 options 是 JsonObject 并在拼接 URL 参数时正确处理
                    assert options instanceof JsonObject;
                    JsonObject getBody = (JsonObject) options;
                    StringBuilder newUrlBuilder = new StringBuilder(fullURL + "?");
                    for (Map.Entry<String, JsonElement> entry : getBody.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue().toString().replace("\"", "");
                        newUrlBuilder.append(key).append("=").append(value).append("&");
                    }
                    // 去除最后一个 "&"
                    String finalGetUrl = newUrlBuilder.length() > (fullURL + "?").length() ?
                            newUrlBuilder.substring(0, newUrlBuilder.length() - 1) : fullURL;

                    response = HttpUtil.createGet(finalGetUrl)
                            .bearerAuth(state.UserToken)
                            .timeout(timeout)
                            .execute().body();
                    break;
                case "POST":
                    assert options instanceof JsonObject;
                    JsonObject postBody = (JsonObject) options;
                    response = HttpUtil.createPost(fullURL)
                            .header("Content-Type", "application/json")
                            .bearerAuth(state.UserToken)
                            .body(postBody.toString())
                            .timeout(timeout)
                            .execute().body();
                    break;
                case "UPLOAD":
                    assert options instanceof HashMap;
                    Map uploadBody = (Map) options;
                    response = HttpUtil.createPost(fullURL)
                            .bearerAuth(state.UserToken)
                            .form(uploadBody)
                            .timeout(timeout)
                            .execute().body();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            // --- TOKEN 过期处理逻辑 ---
            if (response.contains("JwtTokenInvalid") || response.contains("JwtTokenExpired")) {
                if (currentRetryCount < MAX_RETRIES - 1) { // 允许重试，但不超过最大重试次数前的一次
                    // 进入同步区，确保只有一个线程处理刷新
                    REFRESH_TOKEN_LOCK.lock();
                    try {
                        if (!isTokenRefreshInProgress) {
                            // 我是第一个发现Token过期并需要刷新的线程
                            isTokenRefreshInProgress = true;
                            // 释放锁，以便刷新Token的操作可以进行，并且不会阻塞其他等待的线程
                            // 如果refreshUserToken本身内部不需要这个锁，可以考虑在这种情况下暂时解锁
                            // 但是为了确保原子性，通常是让刷新操作在锁定状态下完成
                            // 如果 refreshUserToken 的网络请求很长，这会阻塞所有等待线程直到完成
                            // 实际应用中，如果 refreshUserToken 内部是耗时操作，
                            // 更好的做法是将其放在另一个线程或 CompletableFuture 中，
                            // 然后再 signalAll。但为了简化，这里直接执行。
                            REFRESH_TOKEN_LOCK.unlock(); // 暂时释放锁
                            try {
                                refreshUserToken(currentRetryCount); // 执行Token刷新
                            } finally {
                                REFRESH_TOKEN_LOCK.lock(); // 重新获取锁以便更新状态和唤醒
                                isTokenRefreshInProgress = false; // 刷新完成，标记为不再进行中
                                TOKEN_REFRESHED.signalAll(); // 唤醒所有等待的线程
                            }
                        } else {
                            // Token正在刷新中，当前线程需要等待
                            LOG.info("Token refresh in progress, waiting for new token for URL: " + url);
                            try {
                                // 最多等待一个合理的超时时间，防止死锁或无限等待
                                // 例如，等待10秒，如果10秒内刷新还没完成，就当失败处理
                                boolean refreshed = TOKEN_REFRESHED.await(10, TimeUnit.SECONDS); // 等待直到被唤醒或超时
                                if (!refreshed) {
                                    LOG.warn("Token refresh wait timed out for URL: " + url);
                                    return "{\"status\":\"err\", \"message\":\"Token refresh timed out\"}";
                                }
                                LOG.info("Token refresh completed, retrying request for URL: " + url);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt(); // 恢复中断状态
                                LOG.error("Token refresh wait interrupted for URL: " + url + " Exception: " + e.getMessage());
                                return "{\"status\":\"err\", \"message\":\"Token refresh interrupted\"}";
                            }
                        }
                    } finally {
                        // 确保锁最终被释放
                        if (REFRESH_TOKEN_LOCK.isHeldByCurrentThread()) {
                            REFRESH_TOKEN_LOCK.unlock();
                        }
                    }
                    // Token已刷新（或者等待并获得了新Token），使用新的Token重试请求
                    // 注意：这里 retryCount 应该加1，因为这是一次重试（因为Token过期了）
                    return request(url, method, options, currentRetryCount + 1);
                } else {
                    // 达到最大重试次数，不再尝试刷新或重试
                    LOG.warn("Max retries reached for URL: " + url + ". Token expired.");
                    state.setUserInfo(new JsonObject());
                    state.UserToken = ""; // 清空Token

                    // 避免重复显示登录对话框和通知

                    if (!isLoginDialogShown) { // 原子操作设置，确保只有一个线程执行
                        // 在事件调度线程中显示UI组件和通知
                        SwingUtilities.invokeLater(() -> {
                            new LoginDialog().openDialog("Token expired");
                            Notifications.Bus.notify(new Notification(MsgEntryBundle.message("group.id"),
                                    "Token expired",
                                    "Login info expired,Please login and try again.",
                                    NotificationType.ERROR));
                            isLoginDialogShown = false; // 对话框关闭后可以重置，以便下次需要时再次显示
                        });
                    }
                    return "{\"status\":\"err\"}";
                }
            }

            // --- 正常响应处理 ---
            JsonObject object = JsonParser.parseString(response).getAsJsonObject();
            if (!object.keySet().isEmpty() && object.has("status")) {
                if (StringUtil.equals(object.get("status").getAsString(), "err")) {
                    String errorMessage = "";
                    if (object.keySet().contains("message")) {
                        errorMessage = object.get("message").getAsString();
                    }
                    LOG.info("ShineAIUtil Request: question={}, response={}", errorMessage, response);
                }
            }
            return response;

        } catch (Exception e) {
            LOG.error("ShineAIUtil Request: exception for URL=" + url + ", method=" + method + ", error=" + e.getMessage(), e);
            System.out.println("ShineAIUtil Request: exception" + e.getMessage()); // 调试用
            return "{\"status\":\"err\", \"message\":\"" + e.getMessage() + "\"}";
        }
    }

    public static void loginUser(String mail,String pass, JComponent component) {
        JsonObject params = new JsonObject();
        String URL = "/login";
        params.addProperty("mail",mail);
        params.addProperty("pass",pass);
        try {
            String grants = request(URL,"POST",params,0);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
            if (!object.keySet().isEmpty()) {
                if (Objects.equals(object.get("status").getAsString(), "err")) {
                    String errorMessage = "";
                    if (object.keySet().contains("message")) {
                        errorMessage = object.get("message").getAsString();
                    }
                    BalloonUtil.showBalloon("LoginUser Failed " + object.get("code") + "\n\nLoginUser failed, error: " + (errorMessage.isEmpty() ? object.get("err_key").getAsString() : errorMessage),MessageType.ERROR,component);
                    return;
                }
            }
            JsonObject resData = object.get("data").getAsJsonObject(); // 直接赋值
            state.setUserInfo(resData); // 存到storage
            state.UserToken = resData.get("token").getAsString();
            BalloonUtil.showBalloon("LoginUser successful ",MessageType.INFO,component);
        } catch (Exception e) {
            LOG.info("ShineAIUtil loginUser: exception={}",e.getMessage());
            BalloonUtil.showBalloon("Create UserToken Failed" + "\n\nCreate UserToken failed, error: " + e.getMessage(),MessageType.ERROR,component);
        }
    }

    public static void logOutUser(JComponent component) {
        JsonObject params = new JsonObject();
        String URL = "/user/logout";
        if (state.getUserInfo().has("id")) {
            params.addProperty("uid",state.getUserInfo().get("id").getAsString());
        }
        try {
            String grants = request(URL,"POST",params,0);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
            if (!object.keySet().isEmpty()) {
                if (Objects.equals(object.get("status").getAsString(), "err")) {
                    String errorMessage = "";
                    if (object.keySet().contains("message")) {
                        errorMessage = object.get("message").getAsString();
                    }
                    BalloonUtil.showBalloon("LogoutUser Failed " + object.get("code") + "\n\nLogoutUser failed, error: " + (errorMessage.isEmpty() ? object.get("err_key").getAsString() : errorMessage),MessageType.ERROR,component);
                }
            }
            BalloonUtil.showBalloon("LogoutUser successful ",MessageType.INFO,component);
        } catch (Exception e) {
            LOG.info("ShineAIUtil logOutUser: exception={}",e.getMessage());
            BalloonUtil.showBalloon("LogoutUser Failed" + "\n\nLogoutUser failed, error: " + e.getMessage(),MessageType.ERROR,component);
        }
        // 无论是否退出登录，均删除登录信息
        state.setUserInfo(new JsonObject());
        state.UserToken = "";
    }

    public static void refreshUserToken(Integer retry) {
        JsonObject params = new JsonObject();
        String URL = "/reToken";
        if (state.getUserInfo().has("refreshToken")) {
            params.addProperty("refreshToken",state.getUserInfo().get("refreshToken").getAsString());
        }
        try {
            String grants = request(URL,"POST",params,retry);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
            if (object.get("code").getAsInt() == 0 && object.has("data")) {
                JsonObject resData = object.get("data").getAsJsonObject(); // 解构data
                state.UserToken = resData.get("token").getAsString(); // 更新 token

                System.out.println("reToken success!");
                System.out.println(state.UserToken);
            }
        } catch (Exception e) {
            LOG.info("ShineAIUtil RefreshUserToken: exception={}",e.getMessage());
            MessageDialogBuilder.okCancel("RefreshUserToken Failed","RefreshUserToken failed, error: " + e.getMessage());
        }
    }

    public static JsonArray getAIModels(String vendor,String url) {
        JsonObject params = new JsonObject();
        JsonArray models = new JsonArray();

        if (state.getUserInfo().has("id")) {
            params.addProperty("uid",state.getUserInfo().get("id").getAsString());
            params.addProperty("vendor",vendor.toLowerCase());
            params.addProperty("apiKey","");

            if (url.equals("/ai/aiModels")) {
                params.addProperty("cfId", "");
            }
        }
        try {
            String grants = request(url,"GET",params,0);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
            if (object.get("code").getAsInt() == 0 && object.has("data")) {
                models = object.get("data").getAsJsonArray(); // 解构data
                return models;
            }
        } catch (Exception e) {
            LOG.info("ShineAIUtil GetAIModels: exception={}",e.getMessage());
            MessageDialogBuilder.okCancel("GetAIModels Failed",
                    "GetAIModels failed, error: " + e.getMessage());
        }
        return models;
    }

    public static JsonObject uploadImg(Image image) {
        JsonObject returnData = new JsonObject();

        // 1. 构建 form-data 参数
        Map<String, Object> formData = new HashMap<>();// 表单格式

        String URL =  "/upload/uploadD2";
        if (state.getUserInfo().has("id")) {
            formData.put("uid", state.getUserInfo().get("id").getAsString());
        }
        try {
            File imageFile = ImgUtils.convertImageByThumbnails(image,"png",GeneratorUtil.generateWithUUID() + "_image",0.68f);
            if (imageFile != null) {
                FileResource fileResource = new FileResource(new File(imageFile.getPath()), imageFile.getName());
                returnData.addProperty("fileName",imageFile.getName());
                returnData.addProperty("lastModified",imageFile.lastModified());
                returnData.addProperty("path",imageFile.getPath());
                returnData.addProperty("length",imageFile.length());
                returnData.addProperty("absolutePath",imageFile.getAbsolutePath());
                returnData.addProperty("mimeType", Files.probeContentType(imageFile.toPath()));
                formData.put("file", fileResource);
            }

            String grants = request(URL,"UPLOAD",formData,0);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();

            LOG.info("ShineAIUtil uploadImg: object={}",object);
            System.out.println(object);

            if (object.get("code").getAsInt() == 0 && object.has("data")) {
                JsonObject resData = object.get("data").getAsJsonObject(); // 解构data
                if (resData.has("url")) {
                    System.out.println(resData.get("url").getAsString());
                    returnData.addProperty("url",resData.get("url").getAsString());
                };
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LOG.info("ShineAIUtil uploadImg: exception={}",e.getMessage());
        }
        return returnData;
    }

    public static String getImageUrl(String fileName) {
        JsonObject params = new JsonObject();
        String imageUrl = "";

        String URL = "/upload/getD2file";

        if (state.getUserInfo().has("id")) {
            params.addProperty("uid", state.getUserInfo().get("id").getAsString());
        }
        params.addProperty("fileName", fileName);

        try {
            String grants = request(URL,"GET",params,0);
            System.out.println(grants);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
            if (object.get("code").getAsInt() == 0 && object.has("data")) {
                JsonObject resData = object.get("data").getAsJsonObject(); // 解构data
                if (resData.has("url")) {
                    imageUrl = resData.get("url").getAsString();
                };
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            LOG.info("ShineAIUtil getImageUrl: exception={}",e.getMessage());
        }
        return imageUrl;
    }
}
