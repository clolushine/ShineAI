package com.shine.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shine.ai.settings.AIAssistantSettingsState;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MessageComponent;
import com.shine.ai.util.JsonUtil;
import com.shine.ai.util.ShineAIUtil;
import com.shine.ai.util.StringUtil;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class AIHandler extends AbstractHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AIHandler.class);
    private final AIAssistantSettingsState state = AIAssistantSettingsState.getInstance();

    private boolean requestRetry = false; // 添加一个标志来防止无限重试
    private final Object requestLock = new Object(); // 锁，用于同步请求重试
    private Call requestCall = null;
    private EventSource requestEvent = null;

    public Request createRequest(MainPanel mainPanel, JsonObject messageMy) {
        RequestProvider provider = new RequestProvider().create(mainPanel, messageMy, mainPanel.getAIApi());
        return new Request.Builder()
                .url(provider.getUrl())
                .headers(Headers.of(provider.getHeader()))
                .post(RequestBody.create(provider.getData().getBytes(StandardCharsets.UTF_8),
                        MediaType.parse("application/json")))
                .build();
    }

    public OkHttpClient createHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                // 防止 NAT/防火墙在空闲时切断，开启心跳 (服务器如果不发送心跳，这里会自己发 ping)
                .pingInterval(30, TimeUnit.SECONDS)
                .connectTimeout(state.requestTimeout,TimeUnit.MILLISECONDS)
                .readTimeout(state.requestTimeout,TimeUnit.MILLISECONDS)
                .writeTimeout(state.requestTimeout,TimeUnit.MILLISECONDS);
        builder.hostnameVerifier(getHostNameVerifier());
        builder.sslSocketFactory(getSslContext().getSocketFactory(), (X509TrustManager) getTrustAllManager());
        return builder.build();
    }

    public Call handle(MainPanel mainPanel, MessageComponent component, JsonObject messageMy) {
        try {
            Request request = createRequest(mainPanel,messageMy);
            OkHttpClient httpClient = createHttpClient();
            requestCall = httpClient.newCall(request);
            requestCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println(e.getMessage());
                    handleErrorData(component,mainPanel);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    handleResponse(response,component,mainPanel,messageMy);
                    response.close();
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
            handleErrorData(component,mainPanel);
        } finally {
            mainPanel.getExecutorService().shutdown();
        }
        return requestCall;
    }

    private void handleResponse (@NotNull Response response, MessageComponent component, MainPanel mainPanel,JsonObject messageMy) throws IOException {
        if (response.code() == 401) {
            synchronized (requestLock) { // 同步代码块，防止并发刷新 Token
                if (!requestRetry) {  // 双重检查锁，确保只刷新一次 Token
                    requestRetry = true;
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            System.out.println("refreshUserToken");
                            ShineAIUtil.refreshUserToken(0); // 在后台线程刷新 Token
                            return null;
                        }
                        @Override
                        protected void done() {
                            try {
                                get(); // 获取 doInBackground() 的结果，如果发生异常，会在这里抛出
                                System.out.println("done");
                                requestCall = handle(mainPanel, component, messageMy); // 刷新 Token 成功后，重试请求
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }.execute();
                }else {
                    requestRetry = false; // 重置标志
                    handleErrorData(component,mainPanel);
                }
            }
        }else if (response.code() >= 200 && response.code() < 300) {
            handleResData(response,component,mainPanel);
        }else {
            handleErrorData(component,mainPanel);
        }
    }

    private void handleResData(Response response, MessageComponent component,MainPanel mainPanel) throws IOException {
        JsonObject resBody = JsonParser.parseString(response.body().string()).getAsJsonObject();
        int code = resBody.get("code").getAsInt();
        JsonObject data = resBody.get("data").getAsJsonObject();
        int msgCode = 0;
        if (data.get("message").getAsJsonObject().has("code")) {
            msgCode = data.get("message").getAsJsonObject().get("code").getAsInt();
        }
        JsonObject event = new JsonObject();
        event.addProperty("event", String.format("message:%s",msgCode == 0 && code == 0 ? "done" : "err"));
        JsonObject dck = JsonUtil.mergeJsonObject(event,data);
        mainPanel.aroundRequest(false);
        component.updateMessage(dck);
    }

    private void handleErrorData(MessageComponent component,MainPanel mainPanel) {
        mainPanel.aroundRequest(false);
        JsonObject event = new JsonObject();
        event.addProperty("event", "message:err");
        component.updateMessage(event);
    }


    public EventSource handleStream(MainPanel mainPanel, MessageComponent component, JsonObject messageMy) {
        try {
            Request request = createRequest(mainPanel,messageMy);

            OkHttpClient httpClient = createHttpClient();
            EventSource.Factory factory = EventSources.createFactory(httpClient);
            EventSourceListener listener = new EventSourceListener() {
                boolean handler = false;
                @Override
                public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {}
                @Override
                public void onClosed(@NotNull EventSource eventSource) {
                    if (!handler) {
                        handleStreamErrorData(component,mainPanel);
                    }else mainPanel.aroundRequest(false);
                }
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    handler = true;
                    try {
                        if (StringUtil.isEmpty(data)) {
                            return;
                        }
                        component.updateMessage(JsonParser.parseString(data).getAsJsonObject());
                    } catch (Exception e) {
                        handleStreamErrorData(component,mainPanel);
                    } finally {
                        mainPanel.getExecutorService().shutdown();
                    }
                }

                @Override
                public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                    try {
                        if (t != null) {
                            if (t.getMessage().contains("CANCEL")) {
                                handleStreamAbort(component,mainPanel);
                            }else {
                                handleStreamErrorData(component,mainPanel);
                            }
                        }else {
                            handleStreamResponse(response,component,mainPanel,messageMy);
                            response.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            requestEvent = factory.newEventSource(request, listener);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            handleStreamErrorData(component,mainPanel);
        } finally {
            mainPanel.getExecutorService().shutdown();
        }
        return requestEvent;
    }

    private void handleStreamErrorData(MessageComponent component,MainPanel mainPanel) {
        mainPanel.aroundRequest(false);
        JsonObject event = new JsonObject();
        event.addProperty("event", "error");
        component.updateMessage(event);
    }

    private void handleStreamAbort(MessageComponent component,MainPanel mainPanel) {
        mainPanel.aroundRequest(false);
        JsonObject event = new JsonObject();
        event.addProperty("event", "abort");
        component.updateMessage(event);
    }

    private void handleStreamResponse (@NotNull Response response, MessageComponent component, MainPanel mainPanel,JsonObject messageMy) throws IOException {
        if (response.code() == 401) {
            synchronized (requestLock) { // 同步代码块，防止并发刷新 Token
                if (!requestRetry) {  // 双重检查锁，确保只刷新一次 Token
                    requestRetry = true;
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            System.out.println("refreshUserToken");
                            ShineAIUtil.refreshUserToken(0); // 在后台线程刷新 Token
                            return null;
                        }
                        @Override
                        protected void done() {
                            try {
                                get(); // 获取 doInBackground() 的结果，如果发生异常，会在这里抛出
                                System.out.println("done");
                                handleStream(mainPanel, component, messageMy); // 刷新 Token 成功后，重试请求
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }.execute();
                }else {
                    requestRetry = false; // 重置标志
                    handleErrorData(component,mainPanel);
                }
            }
        }else if (response.code() >= 200 && response.code() < 300) {
            handleResData(response,component,mainPanel);
        }else {
            handleErrorData(component,mainPanel);
        }
    }
}
