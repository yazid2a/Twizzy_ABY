import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Activite4 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Charger l'image
        String imagePath = "Images_panneaux/30.jpg";
        Mat image = Highgui.imread(imagePath);
        if (image.empty()) {
            System.out.println("Erreur: Impossible de charger l'image");
            return;
        }

        // Convertir en niveaux de gris
        Mat gray = new Mat();
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un flou gaussien pour réduire le bruit
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Détecter les contours avec Canny
        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, 50, 150);

        // Trouver les contours
        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Dessiner les contours sur une copie de l'image
        Mat result = image.clone();
        Imgproc.drawContours(result, contours, -1, new Scalar(0, 255, 0), 2);

        // Créer les fenêtres pour afficher les images
        JFrame frameOriginal = new JFrame("Image Originale");
        JFrame frameContours = new JFrame("Contours détectés");
        frameOriginal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameContours.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Convertir les Mat en BufferedImage
        BufferedImage bufferedImageOriginal = matToBufferedImage(image);
        BufferedImage bufferedImageContours = matToBufferedImage(result);
        
        // Créer les JLabels pour afficher les images
        JLabel labelOriginal = new JLabel(new ImageIcon(bufferedImageOriginal));
        JLabel labelContours = new JLabel(new ImageIcon(bufferedImageContours));
        
        // Ajouter les labels aux fenêtres
        frameOriginal.add(labelOriginal);
        frameContours.add(labelContours);
        
        // Ajuster la taille des fenêtres aux images
        frameOriginal.pack();
        frameContours.pack();
        
        // Positionner les fenêtres
        frameOriginal.setLocation(100, 100);
        frameContours.setLocation(100 + frameOriginal.getWidth() + 20, 100);
        
        // Afficher les fenêtres
        frameOriginal.setVisible(true);
        frameContours.setVisible(true);
    }

    // Méthode pour convertir une Mat en BufferedImage
    private static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
} 