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

package com.shine.ai.ui;

import com.google.gson.JsonArray;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ImageLoader;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.ImgUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static com.shine.ai.MyToolWindowFactory.previewImageDialog;

public class ImageViewInMessage extends JPanel {
    private Image image;
    private String imageName;
    private JsonArray imageGroup;
    private int fixedW = 64;
    private int fixedH = 64;
    private int fixedSize = 288;
    private Boolean isError = false;

    private final JPopupMenu popupMenu;

    private RoundImage imageLabel; // 将 imageLabel 提升为成员变量
    public ImageViewInMessage(Image image, String fileName, JsonArray imageGroup) {
        if (image != null) {
            this.image = image;
        }else if (!fileName.isBlank()) {
            this.imageName = fileName;
            this.image = ImgUtils.loadImageFormLocalCache(fileName);
        }

        this.imageGroup = imageGroup; // 接收一个图片组数据

        // 兜底显示个错误图片
        if (this.image == null) {
            this.image = ImgUtils.loadImageFromSource(AIAssistantIcons.IMAGE_ERROR_PATH);
            this.isError = true;
        }

        this.fixedW = Math.max(fixedSize, 64);
        this.fixedH = getFixedH(this.image,this.fixedW);
        this.popupMenu = createPopupMenu();

        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        setOpaque(true);

        JPanel buttonPanel = new JPanel(null); // 用于固定位置组件的面板
        buttonPanel.setOpaque(false); // 使面板透明

        // 缩放图像以适应固定大小
        imageLabel = new RoundImage(ImageLoader.scaleImage(this.image,fixedW,fixedH));
        add(imageLabel,BorderLayout.CENTER);

        setMaximumSize(new Dimension(fixedW, getPreferredSize().height));

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 && !isError) {
                    popupMenu.show(imageLabel, e.getX(), e.getY());
                }else if (e.getButton() == MouseEvent.BUTTON1 && !isError) {
                    previewImageDialog.showDialog(getImageName(),getImageGroup());
                }
            }
        });
    }

    public BufferedImage getImage() {
        return (BufferedImage) this.image;
    }

    public String getImageName() {
        return this.imageName;
    }

    public JsonArray getImageGroup() {
        return this.imageGroup;
    }

    private int getFixedH(Image image,int fixedWidth) {
        if (image == null) return 0;
        BufferedImage img = (BufferedImage) image;
        int originalWidth = img.getWidth();
        int originalHeight = img.getHeight();
        // Calculate the new height based on the aspect ratio
        double scaleFactor = (double) fixedWidth / originalWidth;
        return (int) (originalHeight * scaleFactor);
    }

    private JPopupMenu createPopupMenu() {
        // 创建 JPopupMenu
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem itemCopy = new JMenuItem("Copy",AllIcons.Actions.Copy);
        itemCopy.addActionListener(e -> {
            Boolean isSetClipboard = ImgUtils.copyImageToClipboard(this.image);
            String balloonStr = isSetClipboard ? "Copy successfully" : "Copy failed";
            MessageType balloonType = isSetClipboard ? MessageType.INFO : MessageType.ERROR;
            BalloonUtil.showBalloon(balloonStr,balloonType,this);
        });

        JMenuItem itemSave = new JMenuItem("Save as to",AllIcons.Actions.Download);
        itemSave.addActionListener(e -> {
            ImgUtils.saveAsToImage(this.image);
        });

        JMenuItem itemPreview = getJMenuItem();

        popupMenu.add(itemCopy);
        popupMenu.add(itemSave);
        popupMenu.add(itemPreview);

        return popupMenu;
    }

    private @NotNull JMenuItem getJMenuItem() {
        JMenuItem itemPreview = new JMenuItem("Preview",AllIcons.Actions.Preview);
        itemPreview.addActionListener(e -> {
            previewImageDialog.showDialog(getImageName(),getImageGroup());
        });
        return itemPreview;
    }

    public void setImage(String url,int fixedWidth) {
        Image img = null;
        if (!(url == null || url.isEmpty())) {
            img = ImgUtils.loadImage(url);
        }

        // 兜底显示个错误图片
        if (img == null) {
            img = ImgUtils.loadImageFromSource(AIAssistantIcons.IMAGE_ERROR_PATH);
            this.isError = true;
        }

        this.image = img;
        this.fixedW = fixedWidth;
        this.fixedH = getFixedH(this.image,this.fixedW);
        remove(imageLabel);
        if (this.image != null) {
            imageLabel = new RoundImage(ImageLoader.scaleImage(this.image,fixedW,fixedH));
            add(imageLabel,BorderLayout.CENTER);
        }
    }
}
