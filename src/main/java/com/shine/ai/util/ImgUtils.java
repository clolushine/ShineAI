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

package com.shine.ai.util;

import cn.hutool.core.img.ImgUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ImageUtil;
import com.shine.ai.message.MsgEntryBundle;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.imaging.Imaging;
import org.jetbrains.annotations.NotNull;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ImgUtils extends ImgUtil {

    private static final ConcurrentHashMap<Object, Object>
            imageCache = new ConcurrentHashMap<>();

    private static final Path cachePath = Paths.get(System.getProperty("user.home"), "Documents", "ShineAI","cache");

    public static Image iconToImage(Icon icon) {
        if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        } else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
    }

    public static Image loadImage(String urlString) {
        // 使用URL作为缓存键
        Image image = (Image) imageCache.get(urlString);
        if (image != null) {
            return image;
        }
        try {
            URL urlObj = new URL(urlString);
            image = ImageIO.read(urlObj);
            imageCache.put(urlString, image);
        } catch (IOException e) { // More specific exception
            System.err.println("Error loading image from " + urlString + ": " + e.getMessage());
            return null; // Or a default image
        } catch (Exception e) {
            System.err.println("Unexpected error loading image: " + e.getMessage());
            return null; // Or a default image
        }
        return image;
    }

    public static Image loadImageFromSource(String sourcePath) {
        // 使用URL作为缓存键
        Image image = (Image) imageCache.get(sourcePath);
        if (image != null) {
            return image;
        }
        try {
            ClassLoader classLoader = ImgUtils.class.getClassLoader();
            try (InputStream inputStream = classLoader.getResourceAsStream(sourcePath)) {
                image = ImageIO.read(inputStream);
                imageCache.put(sourcePath, image);
            }
        } catch (IOException e) { // More specific exception
            System.err.println("Error loading image from " + sourcePath + ": " + e.getMessage());
            return null; // Or a default image
        }
        return image;
    }

    public static Image loadImageFormLocalCache(String fileName) {
        Path filePath = Paths.get(String.valueOf(cachePath), fileName);
        // 使用文件路径作为缓存键
        Image image = (Image) imageCache.get(filePath);
        if (image != null) {
            return image;
        }
        try {
            File file = new File(String.valueOf(filePath));
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                return null; // Or a default image
            }
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage != null) {
                image = bufferedImage;
                imageCache.put(filePath, image);
                return image;
            } else {
                System.err.println("Failed to load image from: " + filePath);
                return null; // Or a default image
            }
        } catch (IOException e) {
            System.err.println("Error loading image from " + filePath + ": " + e.getMessage());
            return null; // Or a default image
        }
    }

    public static BufferedImage scaleImage(Image image, int fixedWidth) {
        if (image == null || fixedWidth <= 0) {
            return null; // Handle null or invalid input
        }
        BufferedImage img = (BufferedImage) image;
        int originalWidth = img.getWidth();
        int originalHeight = img.getHeight();
        // Calculate the new height based on the aspect ratio
        double scaleFactor = (double) fixedWidth / originalWidth;
        int newHeight = (int) (originalHeight * scaleFactor);

        BufferedImage scaledImage = ImageUtil.createImage(fixedWidth, newHeight, img.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // Important for smooth scaling
        g2d.drawImage(img, 0, 0, fixedWidth, newHeight, null);
        g2d.dispose();

        return scaledImage;
    }

    public static List<BufferedImage> chooseImage(Project project) {
        List<BufferedImage> imageList = new ArrayList<>();
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, true)
                .withFileFilter(file -> {
                    String ext = file.getExtension();
                    return ext != null && (ext.equalsIgnoreCase("jpg") ||
                            ext.equalsIgnoreCase("jpeg") ||
                            ext.equalsIgnoreCase("png") ||
                            ext.equalsIgnoreCase("gif"));
                });
        descriptor.setTitle("Choose Image");
        descriptor.setDescription("Choose image file.");
        VirtualFile[] files = FileChooser.chooseFiles(descriptor, project, null);
        for (VirtualFile file : files) {
            if (file != null) {
                try {
                  BufferedImage image = Imaging.getBufferedImage(new File(file.getPath()));
                  imageList.add(image);
                } catch (Exception ex) {
                    Notifications.Bus.notify(
                            new Notification(MsgEntryBundle.message("group.id"),
                                    "Load error",
                                    "Cannot load this image.",
                                    NotificationType.ERROR));
                }
            }

        }
        return imageList;
    }

    public static void saveAsToImage(Image image) {
        JFileChooser fileChooser = getJFileChooser();
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String fileName = file.getAbsolutePath();
            // 添加文件扩展名，如果用户没有提供
            if (!fileName.toLowerCase().endsWith(".png") && !fileName.toLowerCase().endsWith(".jpg")) {
                fileName += ".png"; // 默认保存为 PNG
                file = new File(fileName);
            }
            try {
                BufferedImage img = (BufferedImage) image;
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                if (ImageIO.write(img, extension, file)) {
                    JOptionPane.showMessageDialog(null, "Image saved successfully!","Saved success",JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Error saving image.","Saved error",JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static @NotNull JFileChooser getJFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() ||
                        f.getName().toLowerCase().endsWith(".png") ||
                        f.getName().toLowerCase().endsWith(".jpg") ||
                        f.getName().toLowerCase().endsWith(".jpeg") ||
                        f.getName().toLowerCase().endsWith(".gif");
            }
            @Override
            public String getDescription() {
                return "PNG & JPG & GIF Images (*.png, *.jpg, *.jpeg, *.gif)";
            }
        });
        return fileChooser;
    }

    public static Boolean copyImageToClipboard(Image image) {
        if (image == null) {
            return false;
        }
        try {
            BufferedImage img = (BufferedImage) image;
            // 使用DataFlavor.imageFlavor
            Transferable transferableImage = new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{DataFlavor.imageFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.imageFlavor.equals(flavor);
                }

                @Override
                public @NotNull Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (!DataFlavor.imageFlavor.equals(flavor)) {
                        throw new UnsupportedFlavorException(flavor);
                    }
                    return img;
                }
            };

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(transferableImage, null);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static File convertImageToFile(Image image, String fileName) {
        BufferedImage bImage = (BufferedImage) image;
        Graphics2D g2d = bImage.createGraphics();
        g2d.drawImage(bImage, 0, 0, null);
        g2d.dispose();
        File cacheFile = null;
        try {
            cacheFile = File.createTempFile(fileName,".png"); // 使用获取的格式创建临时文件
            ImageIO.write(bImage, "png", cacheFile); // 将 BufferedImage 写入文件
        } catch (IOException e) {
            System.err.println("convertImageToFile error: " + e.getMessage());
        }
        return cacheFile;
    }

    public static File convertImageByThumbnails(Image image,String format,String fileName,float quality) {
        String toPath = String.valueOf(FileUtil.getCachePath( String.format("%s.%s",fileName,format)));
        try (OutputStream outputStream = new FileOutputStream(toPath)) {
            Thumbnails.of((BufferedImage) image)
                    .scale(0.6)
                    .outputFormat(format)
                    .outputQuality(quality)
                    .toOutputStream(outputStream);

            //  文件写入完成，可以安全返回
            return new File(toPath);
        } catch (IOException e) {
            System.err.println("convertImageByThumbnails error: " + e.getMessage());
        }
        return null;
    }

    public static BufferedImage base64ToImage(String base64String) {
        BufferedImage image = null;
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            image = ImageIO.read(bais);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return image;
    }

    public static String imageToBase64(Image image,String formatName,Boolean isAddPrefix) {
        String base64Img = null;
        try {
            BufferedImage img = (BufferedImage) image;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, formatName, baos); // 这里使用 png 格式，你可以根据需要调整
            byte[] imageBytes = baos.toByteArray();
            base64Img = Base64.getEncoder().encodeToString(imageBytes);
            if (isAddPrefix) {
                String imagePrefix = String.format("data:%s;base64,",getImgMimetype(image));
                base64Img = imagePrefix + base64Img;
            }
        } catch (Exception e) {
            Notifications.Bus.notify(
                    new Notification(MsgEntryBundle.message("group.id"),
                            "Convert error",
                            "Cannot convert this image.",
                            NotificationType.ERROR));
            System.out.println(e.getMessage());
        }
        return base64Img;
    }

    public static String getImgMimetype(Image image) {
        String mimeType = "";
        try {
            BufferedImage img = (BufferedImage) image;
            String[] writerFormats = ImageIO.getWriterFormatNames();
            for (String format : writerFormats) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) { // 使用 try-with-resources 自动关闭资源
                    if (ImageIO.write(img, format, baos)) { //尝试写入
                        mimeType = "image/" + format.toLowerCase(); // 构建 MIME 类型
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        if (mimeType.isEmpty()) mimeType = "image/png";
        return mimeType;
    }
}
