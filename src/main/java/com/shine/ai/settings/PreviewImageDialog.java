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

package com.shine.ai.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.shine.ai.ui.PreviewImagePane;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.ImgUtils;
import com.shine.ai.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class PreviewImageDialog extends JDialog {
    private JPanel contentPane;

    private PreviewImagePane imagePane;
    private JBScrollPane ScrollPane;

    private BufferedImage thisImage;
    private String thisImageName;
    private JsonArray thisImageGroup;
    private JLabel countsLabel;

    public PreviewImageDialog() {
        init();
    }

    private void init() {
        setContentPane(contentPane);
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // 设置关闭操作

        setTitle("Preview Image");

        setWindowLayout();

        pack(); //  先调用 pack()
        setVisible(false);

        contentPane.setLayout(new BorderLayout());
        contentPane.setBorder(JBUI.Borders.empty(12,32,32,32));

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hideDialog();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 创建控制按钮面板
        imagePane = new PreviewImagePane();
        ScrollPane = new JBScrollPane(imagePane);
        ScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(ScrollPane, BorderLayout.CENTER);

        // 创建控制按钮面板
        JPanel controlPanel = getControlPanel();
        contentPane.add(controlPanel, BorderLayout.NORTH);

        // 创建切换按钮面板
        JPanel arrowPanel = getArrowPanel();
        contentPane.add(arrowPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                hideDialog();
            }
        });


        // --- 注册键盘事件 ---

        // 1. 定义动作 (Action)
        // Action for Previous Image
        Action prevImageAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setPrevImage(); // 调用实际的逻辑
            }
        };

        // Action for Next Image
        Action nextImageAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNextImage(); // 调用实际的逻辑
            }
        };

        // Action for Previous Image
        Action zoomInAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomIn(); // 调用实际的逻辑
            }
        };

        // Action for Next Image
        Action zoomOutAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomOut(); // 调用实际的逻辑
            }
        };

        // 2. 获取 InputMap 和 ActionMap
        // 使用 WHEN_IN_FOCUSED_WINDOW 表示当此组件所在的顶层窗口有焦点时即可触发
        // 如果只想在按钮有焦点时触发，可以用 WHEN_FOCUSED
        // 如果想在任何组件有焦点时触发，可以用 WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        InputMap inputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = contentPane.getActionMap();

        // 3. 创建 KeyStroke 并关联到 Action
        // Prev: Left Arrow
        KeyStroke leftArrow = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
        inputMap.put(leftArrow, "prevImage"); // "prevImage" 是一个任意的字符串键
        actionMap.put("prevImage", prevImageAction);

        KeyStroke upArrow = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        inputMap.put(upArrow, "zoomIn");
        actionMap.put("zoomIn", zoomInAction);

        KeyStroke downArrow = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        inputMap.put(downArrow, "zoomOut");
        actionMap.put("zoomOut", zoomOutAction);

        // Next: Right Arrow
        KeyStroke rightArrow = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
        inputMap.put(rightArrow, "nextImage"); // "nextImage" 是一个任意的字符串键
        actionMap.put("nextImage", nextImageAction);

        // Prev: Page Up
        KeyStroke pageUp = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0);
        inputMap.put(pageUp, "prevImage"); // 将 Page Up 也映射到 "prevImage" 动作
        actionMap.put("prevImage", prevImageAction);

        // Next: Page Down
        KeyStroke pageDown = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0);
        inputMap.put(pageDown, "nextImage"); // 将 Page Down 也映射到 "nextImage" 动作
        actionMap.put("nextImage", nextImageAction);
    }

    /**
     * 从文件加载并显示图片。
     * @param imageFile 要加载的图片文件。
     */
    public void setImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            JOptionPane.showMessageDialog(this, "文件不存在或无效: " + (imageFile != null ? imageFile.getAbsolutePath() : "null"), "错误", JOptionPane.ERROR_MESSAGE);
            imagePane.setImage(null); // 清除现有图片
            return;
        }
        try {
            BufferedImage img = ImageIO.read(imageFile);
            if (img == null) {
                JOptionPane.showMessageDialog(this, "无法读取图片文件: " + imageFile.getName() + "\n请确认文件是有效的图片格式。", "错误", JOptionPane.ERROR_MESSAGE);
                imagePane.setImage(null);
                return;
            }
            imagePane.setImage(img);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "加载图片时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            imagePane.setImage(null);
        }
    }

    /**
     * 设置当前 BufferedImage 对象。
     */
    public void setImage(int index) {
        if (index < 0) {
            BalloonUtil.showBalloon("Image load error!", MessageType.ERROR, contentPane);
            return;
        }

        JsonObject imageItem = thisImageGroup.get(index).getAsJsonObject();
        thisImageName = imageItem.get("fileName").getAsString();
        thisImage = (BufferedImage) ImgUtils.loadImageFormLocalCache(thisImageName);
        imagePane.setImage(thisImage);

        // 更新显示当前数量
        updateCountsLabel(index);
    }

    private void setPrevImage() {
        int idx = getImageCurrentIndex();
        if (idx>0) {
            setImage(idx-1);
        }else {
            BalloonUtil.showBalloon("It's already the start", MessageType.WARNING, contentPane);
        }
    }

    private void setNextImage() {
        int idx = getImageCurrentIndex();
        if (idx<thisImageGroup.size() - 1) {
            setImage(idx+1);
        }else {
            BalloonUtil.showBalloon("It's already the end", MessageType.WARNING, contentPane);
        }
    }

    private void zoomIn() {
        imagePane.setZoomFactor(imagePane.getZoomFactor() + 0.2);
    }

    private void zoomOut() {
        imagePane.setZoomFactor(imagePane.getZoomFactor() - 0.2);
    }

    private int getImageCurrentIndex() {
        int currentIndex = 0;
        for (int i = 0; i < thisImageGroup.size(); i++) {
            JsonObject imageObj = thisImageGroup.get(i).getAsJsonObject();
            if (imageObj.has("fileName") && StringUtil.equals(imageObj.get("fileName").getAsString(), thisImageName)) {
                currentIndex = i;
                break;
            }
        }
        return currentIndex;
    }

    private @NotNull JPanel getControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5)); // 居中对齐，按钮间距

        JButton zoomInButton = new JButton("Zoom In (+)");
        zoomInButton.addActionListener(e -> zoomIn());

        JButton zoomOutButton = new JButton("Zoom Out (-)");
        zoomOutButton.addActionListener(e -> zoomOut());

        JButton fitButton = new JButton("Fit Window");
        fitButton.addActionListener(e -> imagePane.fitImageToPanel());

        JButton resetButton = new JButton("Reset (1:1)");
        resetButton.addActionListener(e -> imagePane.resetZoom());

        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(fitButton);
        controlPanel.add(resetButton);
        return controlPanel;
    }

    private @NotNull JPanel getArrowPanel() {
        JPanel arrowPanel = new JPanel();
        arrowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5)); // 居中对齐，按钮间距

        JButton arrowLeftButton = new JButton("← Prev");
        arrowLeftButton.addActionListener(e -> setPrevImage());

        countsLabel = new JLabel("0 / 0");

        JButton arrowRightButton = new JButton("Next →");
        arrowRightButton.addActionListener(e -> setNextImage());

        arrowPanel.add(arrowLeftButton);
        arrowPanel.add(countsLabel);
        arrowPanel.add(arrowRightButton);
        return arrowPanel;
    }

    private void updateCountsLabel(int currentIdx) {
        String countsLabelStr = (currentIdx + 1) + " / " + thisImageGroup.size();
        countsLabel.setText(countsLabelStr);
    }

    private void setWindowLayout() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Calculate dialog dimensions
        int dialogWidth = (int) (screenWidth * 0.66);
        int dialogHeight = (int) (screenHeight * 0.82);

        setPreferredSize(new Dimension(dialogWidth, dialogHeight));

        // Calculate dialog location for centering
        int x = (screenWidth - dialogWidth) / 2;
        int y = (screenHeight - dialogHeight) / 2;
        setLocation(new Point(x, y));
    }

    public void showDialog(String imageName, JsonArray imageGroup) {
        thisImageName = imageName;
        thisImageGroup = imageGroup;

        setImage(getImageCurrentIndex());

        SwingUtilities.invokeLater(() -> {
            setWindowLayout();
            setVisible(true);
        });
    }

    public void hideDialog() {
        SwingUtilities.invokeLater(() -> {
            setVisible(false);
        });
    }
}
