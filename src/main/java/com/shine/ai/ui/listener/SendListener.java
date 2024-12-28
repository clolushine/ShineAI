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
        sendAction.doActionPerformed(mainPanel,content);
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
