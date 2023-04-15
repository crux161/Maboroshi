import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Maboroshi {
    private static final int OFFSET_TICK_INTERVAL = 128;
    private static final int LINE_TICK_INTERVAL = 128;
    private static final int BINLINE_LENGTH = 1024;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Maboroshi <file_path>");
            System.exit(1);
        }

        String file_path = args[0];
        byte[] b = readFlashImage(file_path);
        if (b != null) {
            renderPlot(b);
        }
    }

    private static byte[] readFlashImage(String file_path) {
        Path path = Paths.get(file_path);
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read image file: " + file_path);
            e.printStackTrace();
        }
        return null;
    }

    private static void renderPlot(byte[] b) {
        int pre_pad = (int) Math.ceil(b.length / (double) BINLINE_LENGTH) * BINLINE_LENGTH;
        byte[] post_pad = padding(b, pre_pad, (byte) 0);

        BufferedImage img = createImage(post_pad, BINLINE_LENGTH);

        drawPlot(img);
    }

    private static byte[] padding(byte[] b, int length, byte pad_val) {
        byte[] p = Arrays.copyOf(b, length);
        for (int i = b.length; i < length; i++) {
            p[i] = pad_val;
        }
        return p;
    }

    private static BufferedImage createImage(byte[] b, int binline_length) {
        int width = binline_length;
        int height = (int) Math.ceil(b.length / (double) binline_length);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                byte value = b[y * binline_length + x];
                int rgb = value & 0xFF;
                Color color = new Color(rgb, rgb, rgb);
                img.setRGB(x, y, color.getRGB());
            }
        }
        return img;
    }

    private static void drawPlot(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            String offset = String.format("%3x", y);
            System.out.printf("%-10s ", offset);
            for (int x = 0; x < width; x += LINE_TICK_INTERVAL) {
                String index = String.format("%6x", x * BINLINE_LENGTH);
                System.out.printf("%-10s ", index);
            }
            System.out.println();
        }

        // Display the image using Java's built-in image viewer
        try {
            ImageIO.write(img, "png", new java.io.File("output.png"));
            System.out.println("Image saved to output.png");
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
