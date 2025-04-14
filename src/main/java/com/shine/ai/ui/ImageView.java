package com.shine.ai.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ImageLoader;
import com.shine.ai.icons.AIAssistantIcons;
import com.shine.ai.util.ImgUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImageView extends JPanel {
    private Timer loadingTimer;

    private final Icon[] stepIcons = {
            AllIcons.Process.Step_1,
            AllIcons.Process.Step_2,
            AllIcons.Process.Step_3,
            AllIcons.Process.Step_4,
            AllIcons.Process.Step_5,
            AllIcons.Process.Step_6,
            AllIcons.Process.Step_7,
            AllIcons.Process.Step_8,
    };

    private Image image;
    private String name;
    private String url;
    private final int fixedWidth = 64;
    private final int fixedHeight = 64;
    private final IconButton deleteButton;
    private final IconButton reUploadButton;

    private RoundImage imageLabel; // 将 imageLabel 提升为成员变量
    private final RoundPanel loadingPanel; // 用于显示 loading 动画的面板
    private final JLabel loadingLabel;

    public boolean isUploaded = false;

    public ImageView(Image image) {
        this.image = image;

        setLayout(new OverlayLayout(this));
        setOpaque(false);

        // Loading Panel
        loadingPanel = new RoundPanel(new BorderLayout());
        loadingPanel.setPreferredSize(new Dimension(fixedWidth, fixedHeight));
        loadingPanel.setOpaque(false);
        loadingLabel = new JLabel(); // 使用合适的 loading 图标
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        loadingPanel.setVisible(false); // 初始状态不显示 loading
        add(loadingPanel);

        JPanel buttonPanel = new JPanel(null); // 用于固定位置组件的面板
        buttonPanel.setOpaque(false); // 使面板透明

        deleteButton = new IconButton("delete", IconLoader.getIcon("/icons/delete.svg",this.getClass()));
        deleteButton.setBounds(fixedWidth - 28, - 6, 32, 32);
        buttonPanel.add(deleteButton);

        reUploadButton = new IconButton("reUpload", AllIcons.Actions.Refresh);
        reUploadButton.setBounds(fixedWidth / 2, fixedHeight / 2, fixedWidth / 2, fixedHeight / 2);
        reUploadButton.setVisible(false);
        buttonPanel.add(reUploadButton);
        add(buttonPanel);

        // 缩放图像以适应固定大小
        imageLabel = new RoundImage(ImageLoader.scaleImage(image,fixedWidth, fixedHeight));
        add(imageLabel,BorderLayout.CENTER);

        setPreferredSize(new Dimension(fixedWidth, fixedHeight));
    }

    public void setImage(String url,String name) {
        Image img = null;
        if (url == null || url.isEmpty()) {
            img = ImgUtils.iconToImage(AIAssistantIcons.UPLOAD_ERROR);
            reUploadButton.setVisible(true);
        } else {
            this.isUploaded = true;
            img = ImgUtils.loadImageFormLocalCache(name);
        }

        this.image = img;
        this.name = name;
        this.url = url;
        remove(imageLabel);
        if (img != null) {
            imageLabel = new RoundImage(ImageLoader.scaleImage(img,fixedWidth, fixedHeight));
            add(imageLabel,BorderLayout.CENTER);
        }
    }

    public IconButton getDeleteButton() {
        return this.deleteButton;
    }

    public IconButton getReUploadButton() {
        return this.reUploadButton;
    }

    public Image getImage() {
        return this.image;
    }

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setLoading(boolean isLoading) {
        loadingPanel.setVisible(isLoading);
        // 切换其他组件的可见性
        for (Component component : getComponents()) {
            if (component != loadingPanel) {
                component.setVisible(!isLoading);
            }
        }
        if (isLoading) {
            loadingTimer = new Timer(50, new ActionListener() {
                private int currentStep = 0; // 声明为字段
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadingLabel.setIcon(stepIcons[currentStep % stepIcons.length]);
                    loadingLabel.revalidate();
                    loadingLabel.repaint();
                    currentStep++;
                }
            });
            loadingTimer.start();
        } else {
            if (loadingTimer != null) {
                loadingTimer.stop();
                loadingTimer = null;
            }
        }
    }
}
