package com.shine.ai;

import com.intellij.openapi.project.Project;
import com.shine.ai.message.MsgEntryBundle;
import com.shine.ai.settings.CFAISettingPanel;
import com.shine.ai.ui.MainPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


public class CloudflareAIToolWindow {

  private final MainPanel panel;

  private final String panelName = MsgEntryBundle.message("ui.setting.server.cloudflare.key");

  private final String AIName = MsgEntryBundle.message("ui.setting.server.cloudflare.name");

  public CloudflareAIToolWindow(@NotNull Project project,@NotNull Class<CFAISettingPanel> settingPanel) {
    panel = new MainPanel(project, settingPanel);
  }

  public JPanel getContent() {
    return panel.init();
  }

  public MainPanel getPanel() {
    return panel;
  }

  public String getPanelName() {
    return panelName;
  }

  public String getAIName() {
    return AIName;
  }

  public void refreshInfo() {
    panel.refreshInfo();
  }

  public void refreshMessages() {
    panel.refreshMessages();
  }
}
