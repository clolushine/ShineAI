/*
 * ShineAI - An IntelliJ IDEA plugin.
 * Copyright (C) 2026 Shine Zhong
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.shine.ai.ui;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import java.awt.*;


public class MyScrollPane extends JBScrollPane {

    public MyScrollPane (Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        setBorder(JBUI.Borders.empty());
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    public void setCorner(String key, Component corner) {
        super.setCorner(key, corner);
    }
}
