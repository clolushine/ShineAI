package com.shine.ai.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class StringUtil extends com.intellij.openapi.util.text.StringUtil {

    public static boolean isNumber(String s) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static @NotNull String stripHtml(@NotNull String html, @Nullable String breaks) {
        if (breaks != null) {
            html = html.replaceAll("<br/?>", breaks);
        }

        return html.replaceAll("<(.|\n)*?>", "");
    }

    public static String stringEllipsis(String str,int maxLeng) {
        boolean isLonger = str.length() > maxLeng;

        return isLonger ? str.substring(0, maxLeng - 1) + "…" : str;
    }
}
