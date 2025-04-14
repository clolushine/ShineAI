package com.shine.ai.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ImageLoader;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.util.BalloonUtil;
import com.shine.ai.util.ImgUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ImageViewInMessage extends JPanel {
    private Image image;
    private int fixedW = 64;
    private int fixedH = 64;
    private Boolean isError = false;

    private final JPopupMenu popupMenu;

    private RoundImage imageLabel; // 将 imageLabel 提升为成员变量
    public ImageViewInMessage(Image image,String fileName,int fixedWidth) {
        if (image != null) {
            this.image = image;
        }else if (!fileName.isEmpty()) {
            this.image = ImgUtils.loadImageFormLocalCache(fileName);
        }

        // 兜底显示个错误图片
        if (this.image == null) {
            this.image = ImgUtils.loadImageFromSource(AIAssistantIcons.IMAGE_ERROR_PATH);
            this.isError = true;
        }

        this.fixedW = Math.max(fixedWidth, 64);
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
                }
            }
        });
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

        JMenuItem itemPreview = new JMenuItem("Preview");
        itemPreview.addActionListener(e -> {
            System.out.println("Image Preview");
        });
        popupMenu.add(itemCopy);
        popupMenu.add(itemSave);
        popupMenu.add(itemPreview);

        return popupMenu;
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
