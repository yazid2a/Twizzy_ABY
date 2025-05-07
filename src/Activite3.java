import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Activite3 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.out.println("Bibliothèque OpenCV chargée avec succès");
    }

    public static void main(String[] args) {
        System.out.println("Démarrage du programme...");
        
        // Charger l'image
        String imagePath = "Images_panneaux/30.jpg";
        System.out.println("Chargement de l'image : " + imagePath);
        Mat image = Highgui.imread(imagePath);
        
        if (image.empty()) {
            System.out.println("Erreur: Impossible de charger l'image");
            return;
        }
        System.out.println("Image chargée avec succès. Dimensions : " + image.width() + "x" + image.height());

        // Convertir l'image en HSV
        System.out.println("Conversion en HSV...");
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        System.out.println("Conversion HSV terminée");

        // Créer les fenêtres pour afficher les images
        System.out.println("Création des fenêtres...");
        JFrame frameOriginal = new JFrame("Image Originale");
        JFrame frameHSV = new JFrame("Image HSV");
        frameOriginal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameHSV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Convertir les Mat en BufferedImage
        System.out.println("Conversion des images pour l'affichage...");
        BufferedImage bufferedImageOriginal = matToBufferedImage(image);
        BufferedImage bufferedImageHSV = matToBufferedImage(hsvImage);
        
        // Créer les JLabels pour afficher les images
        JLabel labelOriginal = new JLabel(new ImageIcon(bufferedImageOriginal));
        JLabel labelHSV = new JLabel(new ImageIcon(bufferedImageHSV));
        
        // Ajouter les labels aux fenêtres
        frameOriginal.add(labelOriginal);
        frameHSV.add(labelHSV);
        
        // Ajuster la taille des fenêtres aux images
        frameOriginal.pack();
        frameHSV.pack();
        
        // Positionner les fenêtres
        frameOriginal.setLocation(100, 100);
        frameHSV.setLocation(100 + frameOriginal.getWidth() + 20, 100);
        
        // Afficher les fenêtres
        System.out.println("Affichage des fenêtres...");
        frameOriginal.setVisible(true);
        frameHSV.setVisible(true);
        System.out.println("Programme terminé avec succès");

        // Libérer les ressources
        image.release();
        hsvImage.release();
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