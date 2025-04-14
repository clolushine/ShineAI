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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.shine.ai.settings.AIAssistantSettingsPanel.SHINE_AI_BASE_URL;


public class ShineAIUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ShineAIUtil.class);

    public static String baseUrl = SHINE_AI_BASE_URL;
    public static Integer timeout = 60000;
    public static AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

    public static boolean isLoginDialogShown = false; // 静态变量，跟踪弹窗状态

    public static String request(String url,String method, Object options, Integer retry) {
        String URL = baseUrl +  url;
        int reCount = retry > 0 ? retry : 0;
        try {
            String response;
            switch (method.toUpperCase()) { // 将 method 转换为大写进行比较
                case "GET":
                    assert options instanceof JsonObject;
                    JsonObject getBody = (JsonObject) options;
                    StringBuilder newUrl = new StringBuilder(baseUrl + url + "?"); // 基础 URL 和路径
                    for (Map.Entry<String, JsonElement> entry : getBody.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue().toString().replace("\"","");
                        newUrl.append(key).append("=").append(value).append("&");  // 直接拼接参数，不使用 urlBuilder
                    }
                    newUrl = new StringBuilder(newUrl.substring(0, newUrl.length() - 1)); // 去除最后一个 "&"
                    response = HttpUtil.createGet(String.valueOf(newUrl))
                            .bearerAuth(state.UserToken)
                            .timeout(timeout)
                            .execute().body();
                    break;
                case "POST":
                    assert options instanceof JsonObject;
                    JsonObject postBody = (JsonObject) options;
                    response = HttpUtil.createPost(URL)
                            .header("Content-Type", "application/json")
                            .bearerAuth(state.UserToken)
                            .body(postBody.toString())
                            .timeout(timeout)
                            .execute().body();
                    break;
                case "UPLOAD":
                    assert options instanceof HashMap<?,?>;
                    Map<String,Object> uploadBody = (Map<String, Object>) options;
                    response = HttpUtil.createPost(URL)
                            .bearerAuth(state.UserToken)
                            .form(uploadBody)
                            .timeout(timeout)
                            .execute().body();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
            if (reCount<3) {
                if (response.contains("JwtTokenInvalid") || response.contains("JwtTokenSignatureMismatched") || response.contains("JwtTokenExpired")) {
                    ++reCount;
                    refreshUserToken(reCount);
                    return request(url,method,options,reCount);
                }
            }else {
                state.setUserInfo(new JsonObject());
                state.UserToken = "";
                if (!isLoginDialogShown) { // 检查弹窗是否已经显示
                    isLoginDialogShown = true; // 设置弹窗状态为已显示
                    Notifications.Bus.notify(
                            new Notification(MsgEntryBundle.message("group.id"),
                                    "Token expired",
                                    "Login info expired,Please login and try again.",
                                    NotificationType.ERROR));
                    new LoginDialog().openDialog("Token expired");
                }
                return "{\"status\":\"err\"}";
            }
            JsonObject object = JsonParser.parseString(response).getAsJsonObject();
            if (!object.keySet().isEmpty()) {
                if (Objects.equals(object.get("status").getAsString(), "err")) {
                    String errorMessage = "";
                    if (object.keySet().contains("message")) {
                        errorMessage = object.get("message").getAsString();
                    }
                    LOG.info("ShineAIUtil Request: question={}",errorMessage);
                }
            }
            return response;
        } catch (Exception e) {
            System.out.println("ShineAIUtil Request: exception" + e.getMessage());
            LOG.info("ShineAIUtil Request: exception={}",e.getMessage());
            return "{\"status\":\"err\"}";
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
            }
        } catch (Exception e) {
            LOG.info("ShineAIUtil RefreshUserToken: exception={}",e.getMessage());
            MessageDialogBuilder.okCancel("RefreshUserToken Failed","RefreshUserToken failed, error: " + e.getMessage());
        }
    }

    public static String[] getAIModels(String vendor,JComponent component) {
        JsonObject params = new JsonObject();
        String[] models = {};
        String URL = switch (vendor) {
            case "GoogleAI" -> "/gem/gems";
            case "GroqAI" -> "/gpt/models";
            default -> "/ai/aiModels";
        };
        if (state.getUserInfo().has("id")) {
            params.addProperty("uid",state.getUserInfo().get("id").getAsString());
        }
        try {
            String grants = request(URL,"GET",params,0);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
            if (object.get("code").getAsInt() == 0 && object.has("data")) {
                JsonArray resData = object.get("data").getAsJsonArray(); // 解构data
                models = resData.asList().stream()
                        .map(JsonElement::getAsString)
                        .toArray(String[]::new);
                return models;
            }
        } catch (Exception e) {
            LOG.info("ShineAIUtil GetAIModels: exception={}",e.getMessage());
            MessageDialogBuilder.okCancel("GetAIModels Failed",
                    "GetAIModels failed, error: " + e.getMessage());
        }
        return models;
    }

    public static JsonObject uploadImg(Image image, JComponent component) {
        JsonObject returnData = new JsonObject();

        // 1. 构建 form-data 参数
        Map<String, Object> formData = new HashMap<>();// 表单格式

        String URL =  "/upload/uploadD2";
        if (state.getUserInfo().has("id")) {
            formData.put("uid", state.getUserInfo().get("id").getAsString());
        }
        try {
            File imageFile = ImgUtils.convertImageByThumbnails(image,"jpg",GeneratorUtil.generateWithUUID() + "_image",0.8f);
            if (imageFile != null) {
                FileResource fileResource = new FileResource(new File(imageFile.getPath()), imageFile.getName());
                returnData.addProperty("fileName",imageFile.getName());
                formData.put("file", fileResource);
            }

            String grants = request(URL,"UPLOAD",formData,0);
            JsonObject object = JsonParser.parseString(grants).getAsJsonObject();
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

    public static String getImageUrl(String fileName, JComponent component) {
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
