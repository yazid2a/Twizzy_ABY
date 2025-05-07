import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class Activite11 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Chemins des images de référence
    private static final String[] REFERENCE_IMAGES = {
        "Images_panneaux/ref30.jpg",
        "Images_panneaux/ref50.jpg",
        "Images_panneaux/ref70.jpg",
        "Images_panneaux/ref90.jpg",
        "Images_panneaux/ref110.jpg",
        "Images_panneaux/refdouble.jpg"
    };

    public static void main(String[] args) {
        // Charger l'image de test
        String testImagePath = "Images_panneaux/p1.jpg";
        Mat image = Highgui.imread(testImagePath);
        if (image.empty()) {
            System.out.println("Erreur: Impossible de charger l'image");
            return;
        }

        // Convertir l'image en HSV
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Définir les seuils pour la couleur rouge
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

        // Appliquer un flou gaussien
        Imgproc.GaussianBlur(redMask, redMask, new Size(5, 5), 0);

        // Trouver les contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(redMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Créer une image pour le résultat
        Mat result = image.clone();

        // Pour chaque contour, analyser sa forme
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area < 1000) continue;

            // Calculer la circularité
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            double circularity = 4 * Math.PI * area / (perimeter * perimeter);

            if (circularity > 0.8) { // Si c'est un panneau rond
                // Obtenir le rectangle englobant
                Rect boundingRect = Imgproc.boundingRect(contour);
                
                // Extraire la région du panneau
                Mat extractedSign = new Mat(image, boundingRect);
                
                // Redimensionner l'image extraite à une taille standard
                Mat resizedSign = new Mat();
                Imgproc.resize(extractedSign, resizedSign, new Size(100, 100));
                
                // Comparer avec les images de référence
                String bestMatch = "";
                double bestScore = Double.MAX_VALUE;
                
                for (String refImage : REFERENCE_IMAGES) {
                    double score = calculateSimilarity(resizedSign, refImage);
                    if (score < bestScore) {
                        bestScore = score;
                        bestMatch = refImage;
                    }
                }
                
                // Dessiner le rectangle sur l'image résultat
                Core.rectangle(result, 
                             new org.opencv.core.Point(boundingRect.x, boundingRect.y),
                             new org.opencv.core.Point(boundingRect.x + boundingRect.width,
                                                     boundingRect.y + boundingRect.height),
                             new Scalar(0, 255, 0), 2);
                
                // Extraire le nom du panneau de référence
                String signName = bestMatch.substring(bestMatch.lastIndexOf('/') + 1);
                signName = signName.substring(0, signName.lastIndexOf('.'));
                
                // Ajouter les informations sur l'image
                Core.putText(result, "Panneau: " + signName,
                           new org.opencv.core.Point(boundingRect.x, boundingRect.y - 10),
                           Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                
                // Créer les fenêtres pour afficher les images
                JFrame frameOriginal = new JFrame("Image Originale");
                JFrame frameResult = new JFrame("Reconnaissance du Panneau");
                
                frameOriginal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frameResult.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // Convertir les Mat en BufferedImage
                BufferedImage bufferedImageOriginal = matToBufferedImage(image);
                BufferedImage bufferedImageResult = matToBufferedImage(result);
                
                // Créer et ajouter les composants aux fenêtres
                frameOriginal.add(new JLabel(new ImageIcon(bufferedImageOriginal)));
                frameResult.add(new JLabel(new ImageIcon(bufferedImageResult)));
                
                // Ajuster et positionner les fenêtres
                frameOriginal.pack();
                frameResult.pack();
                
                frameOriginal.setLocation(100, 100);
                frameResult.setLocation(100 + frameOriginal.getWidth() + 20, 100);
                
                // Afficher les fenêtres
                frameOriginal.setVisible(true);
                frameResult.setVisible(true);
                
                break; // On ne traite que le premier panneau trouvé
            }
        }
    }

    // Méthode pour calculer la similarité entre deux images
    private static double calculateSimilarity(Mat object, String signfile) {
        // Charger l'image de référence
        Mat reference = Highgui.imread(signfile);
        if (reference.empty()) {
            return Double.MAX_VALUE;
        }

        // Redimensionner l'image de référence à la même taille que l'objet
        Mat resizedReference = new Mat();
        Imgproc.resize(reference, resizedReference, new Size(object.width(), object.height()));

        // Convertir les deux images en niveaux de gris
        Mat grayObject = new Mat();
        Mat grayReference = new Mat();
        Imgproc.cvtColor(object, grayObject, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(resizedReference, grayReference, Imgproc.COLOR_BGR2GRAY);

        // Appliquer un seuillage adaptatif
        Mat thresholdObject = new Mat();
        Mat thresholdReference = new Mat();
        Imgproc.adaptiveThreshold(grayObject, thresholdObject, 255,
                                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                                Imgproc.THRESH_BINARY_INV, 11, 2);
        Imgproc.adaptiveThreshold(grayReference, thresholdReference, 255,
                                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                                Imgproc.THRESH_BINARY_INV, 11, 2);

        // Calculer la différence absolue
        Mat diff = new Mat();
        Core.absdiff(thresholdObject, thresholdReference, diff);

        // Calculer le score de similarité (plus le score est bas, plus les images sont similaires)
        double score = 0;
        for (int i = 0; i < diff.rows(); i++) {
            for (int j = 0; j < diff.cols(); j++) {
                double[] pixel = diff.get(i, j);
                score += pixel[0];
            }
        }
        score = score / (diff.rows() * diff.cols());

        // Libérer les ressources
        reference.release();
        resizedReference.release();
        grayObject.release();
        grayReference.release();
        thresholdObject.release();
        thresholdReference.release();
        diff.release();

        return score;
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