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

package com.shine.ai;

import com.google.gson.JsonObject;
import com.shine.ai.ui.MainPanel;
import com.shine.ai.ui.MessageComponent;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


public abstract class AbstractHandler {

    protected X509TrustManager trustManager;

    public abstract  <T> T handle(MainPanel mainPanel, MessageComponent component, JsonObject messageMy);

    public abstract  <T> T handleStream(MainPanel mainPanel, MessageComponent component, JsonObject messageMy);

    public HostnameVerifier getHostNameVerifier() {
        return (hostname, session) -> true;
    }

    public SSLContext getSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { getTrustAllManager() }, new java.security.SecureRandom());
        return sslContext;
    }

    public TrustManager getTrustAllManager() {
        if (trustManager != null) {
            return trustManager;
        }
        trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
        };
        return trustManager;
    }
}
