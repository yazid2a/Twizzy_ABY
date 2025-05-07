import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class Activite8 {
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

        // Créer une image pour le résultat final
        Mat result = image.clone();
        
        // Pour chaque contour, vérifier s'il est circulaire
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            double circularity = 4 * Math.PI * area / (perimeter * perimeter);
            
            if (circularity > 0.8 && area > 1000) {
                // Obtenir le rectangle englobant
                Rect boundingRect = Imgproc.boundingRect(contour);
                
                // Extraire la région du panneau
                Mat extractedSign = new Mat(image, boundingRect);
                
                // Convertir en niveaux de gris
                Mat graySign = new Mat();
                Imgproc.cvtColor(extractedSign, graySign, Imgproc.COLOR_BGR2GRAY);
                
                // Appliquer un seuillage adaptatif
                Mat thresholdSign = new Mat();
                Imgproc.adaptiveThreshold(graySign, thresholdSign, 255,
                                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                                        Imgproc.THRESH_BINARY_INV, 11, 2);
                
                // Trouver les contours des chiffres
                List<MatOfPoint> digitContours = new ArrayList<>();
                Mat digitHierarchy = new Mat();
                Imgproc.findContours(thresholdSign, digitContours, digitHierarchy,
                                   Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                
                // Dessiner le rectangle sur l'image résultat
                Core.rectangle(result, 
                             new org.opencv.core.Point(boundingRect.x, boundingRect.y),
                             new org.opencv.core.Point(boundingRect.x + boundingRect.width,
                                                     boundingRect.y + boundingRect.height),
                             new Scalar(0, 255, 0), 2);
                
                // Analyser chaque contour de chiffre
                for (MatOfPoint digitContour : digitContours) {
                    double digitArea = Imgproc.contourArea(digitContour);
                    if (digitArea > 100) { // Filtrer les petits contours
                        Rect digitRect = Imgproc.boundingRect(digitContour);
                        
                        // Dessiner le rectangle autour du chiffre
                        Core.rectangle(thresholdSign,
                                     new org.opencv.core.Point(digitRect.x, digitRect.y),
                                     new org.opencv.core.Point(digitRect.x + digitRect.width,
                                                             digitRect.y + digitRect.height),
                                     new Scalar(255, 255, 255), 1);
                    }
                }
                
                // Créer les fenêtres pour afficher les images
                JFrame frameOriginal = new JFrame("Image Originale");
                JFrame frameResult = new JFrame("Détection");
                JFrame frameDigits = new JFrame("Chiffres Détectés");
                
                frameOriginal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frameResult.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frameDigits.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Convertir les Mat en BufferedImage
                BufferedImage bufferedImageOriginal = matToBufferedImage(image);
                BufferedImage bufferedImageResult = matToBufferedImage(result);
                BufferedImage bufferedImageDigits = matToBufferedImage(thresholdSign);
                
                // Créer et ajouter les composants aux fenêtres
                frameOriginal.add(new JLabel(new ImageIcon(bufferedImageOriginal)));
                frameResult.add(new JLabel(new ImageIcon(bufferedImageResult)));
                frameDigits.add(new JLabel(new ImageIcon(bufferedImageDigits)));
                
                // Ajuster et positionner les fenêtres
                frameOriginal.pack();
                frameResult.pack();
                frameDigits.pack();
                
                frameOriginal.setLocation(100, 100);
                frameResult.setLocation(100 + frameOriginal.getWidth() + 20, 100);
                frameDigits.setLocation(100 + frameOriginal.getWidth() + frameResult.getWidth() + 40, 100);
                
                // Afficher les fenêtres
                frameOriginal.setVisible(true);
                frameResult.setVisible(true);
                frameDigits.setVisible(true);
                
                break; // On ne traite que le premier panneau trouvé
            }
        }
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