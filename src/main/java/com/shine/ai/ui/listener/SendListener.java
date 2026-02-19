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

package com.shine.ai.ui.listener;

import com.shine.ai.core.SendAction;
import com.shine.ai.ui.MainPanel;

import java.awt.event.*;
import java.io.IOException;


public class SendListener implements ActionListener,KeyListener {

    private final MainPanel mainPanel;

    public SendListener(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            doActionPerformed();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void doActionPerformed() throws IOException {
        String content = mainPanel.getInputTextArea().getContent();
        if (content == null || content.isBlank()) {
            return;
        }
        SendAction sendAction = mainPanel.getProject().getService(SendAction.class);
        sendAction.doActionPerformed(mainPanel,content,null);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER && !e.isControlDown() && !e.isShiftDown()){
            e.consume();
            mainPanel.getButton().doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
