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
