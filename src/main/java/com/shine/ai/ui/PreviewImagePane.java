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

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

public class PreviewImagePane extends JPanel {
    private BufferedImage image;
    private double zoomFactor = 1.0; // 缩放因子
    private Point initialClick; // 用于拖动的初始点击位置
    private Point dragStartMouse; // 拖动起始鼠标点
    private Point dragStartViewPos; // 拖动起始视口位置

    public PreviewImagePane() {
        setBackground(new JBColor(Color.GRAY, Color.LIGHT_GRAY)); // 设置背景色，以区分图片边界

        // 添加鼠标滚轮监听器，用于缩放
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (image == null) return;
                double zoomBase = 1.08; // 缩放基数，越接近1越慢
                double oldZoom = zoomFactor;
                double newZoom = zoomFactor * Math.pow(zoomBase, -e.getPreciseWheelRotation());
                setZoomFactor(newZoom);

                // 缩放时保持鼠标指针下的图像点不变
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, PreviewImagePane.this);
                if (viewport != null) {
                    Point mouse = e.getPoint();
                    double scale = newZoom / oldZoom;
                    Point viewPos = viewport.getViewPosition();
                    int newViewX = (int) ((mouse.x + viewPos.x) * scale - mouse.x);
                    int newViewY = (int) ((mouse.y + viewPos.y) * scale - mouse.y);
                    newViewX = Math.max(0, Math.min(newViewX, getPreferredSize().width - viewport.getWidth()));
                    newViewY = Math.max(0, Math.min(newViewY, getPreferredSize().height - viewport.getHeight()));
                    viewport.setViewPosition(new Point(newViewX, newViewY));
                }
            }
        });

        // 添加鼠标监听器，用于拖动
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 鼠标点转换为视口坐标
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, PreviewImagePane.this);
                if (viewport != null) {
                    dragStartMouse = SwingUtilities.convertPoint(PreviewImagePane.this, e.getPoint(), viewport);
                    dragStartViewPos = viewport.getViewPosition();
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStartMouse = null;
                dragStartViewPos = null;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        // 添加鼠标拖动监听器
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, PreviewImagePane.this);
                if (viewport != null && dragStartMouse != null && dragStartViewPos != null) {
                    // 当前鼠标点转换为视口坐标
                    Point currentMouse = SwingUtilities.convertPoint(PreviewImagePane.this, e.getPoint(), viewport);
                    int dx = dragStartMouse.x - currentMouse.x;
                    int dy = dragStartMouse.y - currentMouse.y;

                    int maxX = Math.max(0, getPreferredSize().width - viewport.getWidth());
                    int maxY = Math.max(0, getPreferredSize().height - viewport.getHeight());

                    int newX = Math.max(0, Math.min(dragStartViewPos.x + dx, maxX));
                    int newY = Math.max(0, Math.min(dragStartViewPos.y + dy, maxY));

                    // 只有图片大于视口时才允许拖动
                    if (getPreferredSize().width > viewport.getWidth() || getPreferredSize().height > viewport.getHeight()) {
                        viewport.setViewPosition(new Point(newX, newY));
                    }
                }
            }
        });
    }

    /**
     * 设置要显示的图片。
     * @param image 要显示的 BufferedImage 对象。
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        resetZoom(); // 设置新图片时，通常重置缩放
    }

    /**
     * 设置缩放因子并更新视图。
     * @param newZoomFactor 新的缩放因子。
     */
    public void setZoomFactor(double newZoomFactor) {
        // 限制缩放范围，例如0.1到10倍
        this.zoomFactor = Math.max(0.1, Math.min(newZoomFactor, 10.0));

        revalidate(); // 重新验证组件布局，会触发 getPreferredSize()
        repaint();    // 重新绘制组件
    }

    /**
     * 获取当前缩放因子。
     */
    public double getZoomFactor() {
        return zoomFactor;
    }

    /**
     * 将图片缩放至实际尺寸 (zoomFactor = 1.0)。
     */
    public void resetZoom() {
        setZoomFactor(1.0);
        // 重置视口位置到左上角
        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, PreviewImagePane.this);
        if (viewport != null) {
            viewport.setViewPosition(new Point(0, 0));
        }
    }

    /**
     * 根据当前面板大小调整图片，使其完全可见。
     */
    public void fitImageToPanel() {
        if (image == null) return;

        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (viewport == null) return; // 必须有视口才能计算适配

        int viewportWidth = viewport.getWidth();
        int viewportHeight = viewport.getHeight();

        if (viewportWidth == 0 || viewportHeight == 0) return; // 视口尚未初始化完成

        double hRatio = (double) viewportWidth / image.getWidth();
        double vRatio = (double) viewportHeight / image.getHeight();

        // 取较小比例，确保图片能完全放入
        setZoomFactor(Math.min(hRatio, vRatio));

        // 尝试居中图片 (如果图片比视口小)
        repaint(); // 确保重绘后图片居中

        SwingUtilities.invokeLater(() -> {
            Dimension currentPanelSize = getPreferredSize();
            int scrollX = Math.max(0, (currentPanelSize.width - viewportWidth) / 2);
            int scrollY = Math.max(0, (currentPanelSize.height - viewportHeight) / 2);
            viewport.setViewPosition(new Point(scrollX, scrollY));
        });
    }

    /**
     * 重写此方法，返回图片按当前缩放因子计算后的尺寸，JScrollPane会根据此尺寸创建滚动条。
     */
    @Override
    public Dimension getPreferredSize() {
        if (image == null) {
            return new Dimension(400, 300); // 默认大小，如果无图片
        }
        int scaledWidth = (int) (image.getWidth() * zoomFactor);
        int scaledHeight = (int) (image.getHeight() * zoomFactor);
        return new Dimension(scaledWidth, scaledHeight);
    }

    /**
     * 绘制图片。
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;

            // 启用高质量渲染
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int imgWidth = (int) (image.getWidth() * zoomFactor);
            int imgHeight = (int) (image.getHeight() * zoomFactor);

            // 获取视口大小（如果存在）
            JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
            int viewWidth = viewport != null ? viewport.getWidth() : getWidth();
            int viewHeight = viewport != null ? viewport.getHeight() : getHeight();

            // 计算图片绘制的起始位置，使其在视口中居中
            // 只有当图片小于视口时才居中，否则由 JScrollPane 负责滚动
            int x = (imgWidth < viewWidth) ? (viewWidth - imgWidth) / 2 : 0;
            int y = (imgHeight < viewHeight) ? (viewHeight - imgHeight) / 2 : 0;

            g2d.drawImage(image, x, y, imgWidth, imgHeight, this);
        } else {
            // 如果没有图片，绘制提示信息
            String noImageText = "No Image Loaded";
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(noImageText);
            int textHeight = fm.getAscent();
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + textHeight;
            g.drawString(noImageText, x, y);
        }
    }
}

