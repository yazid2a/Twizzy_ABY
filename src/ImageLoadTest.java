import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImageLoadTest {
    public static void main(String[] args) {
        // Create a simple test image programmatically
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics g = testImage.getGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        
        // Create a frame to display the test image
        JFrame frame = new JFrame("Image Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        
        // Display the test image
        JLabel testLabel = new JLabel(new ImageIcon(testImage));
        frame.add(testLabel, BorderLayout.NORTH);
        
        // Try to save the test image
        try {
            File outputFile = new File("test_image.png");
            ImageIO.write(testImage, "png", outputFile);
            System.out.println("Test image saved to: " + outputFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to save test image: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Now try to load the saved image
        try {
            File inputFile = new File("test_image.png");
            if (inputFile.exists() && inputFile.canRead()) {
                BufferedImage loadedImage = ImageIO.read(inputFile);
                if (loadedImage != null) {
                    JLabel loadedLabel = new JLabel(new ImageIcon(loadedImage));
                    frame.add(loadedLabel, BorderLayout.SOUTH);
                    System.out.println("Successfully loaded test image");
                } else {
                    System.err.println("ImageIO.read returned null");
                }
            } else {
                System.err.println("Cannot read test image file: " + inputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Failed to load test image: " + e.getMessage());
            e.printStackTrace();
        }
        
        frame.setVisible(true);
    }
}
