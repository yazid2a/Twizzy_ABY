import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class Activite6 {
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

        // Convertir l'image en HSV
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Définir les seuils pour la couleur rouge (en HSV)
        Scalar lowerRed1 = new Scalar(0, 100, 100);
        Scalar upperRed1 = new Scalar(10, 255, 255);
        Scalar lowerRed2 = new Scalar(170, 100, 100);
        Scalar upperRed2 = new Scalar(180, 255, 255);

        // Créer les masques pour le rouge
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Core.inRange(hsvImage, lowerRed1, upperRed1, mask1);
        Core.inRange(hsvImage, lowerRed2, upperRed2, mask2);

        // Combiner les masques
        Mat redMask = new Mat();
        Core.addWeighted(mask1, 1.0, mask2, 1.0, 0.0, redMask);

        // Appliquer un flou gaussien pour réduire le bruit
        Imgproc.GaussianBlur(redMask, redMask, new Size(5, 5), 0);

        // Trouver les contours dans le masque rouge
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(redMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Dessiner les contours sur une copie de l'image
        Mat result = image.clone();
        
        // Pour chaque contour, vérifier s'il est circulaire
        for (MatOfPoint contour : contours) {
            // Calculer la circularité
            double area = Imgproc.contourArea(contour);
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            double circularity = 4 * Math.PI * area / (perimeter * perimeter);
            
            // Si la forme est suffisamment circulaire (circularité > 0.8)
            if (circularity > 0.8 && area > 1000) { // Ajout d'un seuil de taille minimale
                // Dessiner le contour en vert
                Imgproc.drawContours(result, contours, contours.indexOf(contour), 
                                   new Scalar(0, 255, 0), 2);
            }
        }

        // Créer les fenêtres pour afficher les images
        JFrame frameOriginal = new JFrame("Image Originale");
        JFrame frameMask = new JFrame("Masque Rouge");
        JFrame frameResult = new JFrame("Résultat");
        frameOriginal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameMask.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameResult.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Convertir les Mat en BufferedImage
        BufferedImage bufferedImageOriginal = matToBufferedImage(image);
        BufferedImage bufferedImageMask = matToBufferedImage(redMask);
        BufferedImage bufferedImageResult = matToBufferedImage(result);
        
        // Créer les JLabels pour afficher les images
        JLabel labelOriginal = new JLabel(new ImageIcon(bufferedImageOriginal));
        JLabel labelMask = new JLabel(new ImageIcon(bufferedImageMask));
        JLabel labelResult = new JLabel(new ImageIcon(bufferedImageResult));
        
        // Ajouter les labels aux fenêtres
        frameOriginal.add(labelOriginal);
        frameMask.add(labelMask);
        frameResult.add(labelResult);
        
        // Ajuster la taille des fenêtres aux images
        frameOriginal.pack();
        frameMask.pack();
        frameResult.pack();
        
        // Positionner les fenêtres
        frameOriginal.setLocation(100, 100);
        frameMask.setLocation(100 + frameOriginal.getWidth() + 20, 100);
        frameResult.setLocation(100 + frameOriginal.getWidth() + frameMask.getWidth() + 40, 100);
        
        // Afficher les fenêtres
        frameOriginal.setVisible(true);
        frameMask.setVisible(true);
        frameResult.setVisible(true);
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