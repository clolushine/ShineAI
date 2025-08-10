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

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.Random;

public class OtherUtil {
    public static boolean isValidModelInComboBox(@NotNull JComboBox<String> comboBox, @Nullable Object item) {
        if (item == null) return false;
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (StringUtil.equals(model.getElementAt(i),item.toString())) {
                return true;
            }
        }
        return false;
    }

    public static JsonObject weightedRandomTarget(List<JsonObject> targets) {
        if (targets == null || targets.isEmpty()) {
            return null;
        }

        // 计算权重总和
        int totalWeight = 0;
        for (JsonObject target : targets) {
            if (target.has("weight")) {
                totalWeight += target.get("weight").getAsInt(); // 使用 getWeight()
            }
        }

        if (totalWeight <= 0) {
            return null;
        }

        // 生成 1 到 totalWeight 的随机整数
        // Random().nextInt(n) 生成的是 0 到 n-1，所以需要 +1
        Random random = new Random();
        int rand = random.nextInt(totalWeight) + 1;

        // 遍历累积权重，找到命中目标
        int cumulative = 0;
        for (JsonObject target : targets) {
            if (target.has("weight")) {
                cumulative += target.get("weight").getAsInt(); // 使用 getWeight()
            }
            if (rand <= cumulative) {
                return target;
            }
        }

        // 理论上不会执行到这里，作为兜底
        return null;
    }
}
