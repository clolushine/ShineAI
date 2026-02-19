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

package com.shine.ai.ui.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.shine.ai.settings.AIAssistantSettingsPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;


public class SettingAction extends DumbAwareAction {

  public SettingAction(@NotNull @Nls String text) {
    super(() -> text,AllIcons.General.Settings);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), AIAssistantSettingsPanel.class);
  }
}
