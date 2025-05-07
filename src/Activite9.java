import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class Activite9 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    // Modèles de base pour les chiffres (0-9)
    private static final int[][] DIGIT_MODELS = {
        // 0
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        // 1
        {0,0,1,0,0,0,1,1,0,0,0,0,1,0,0,0,0,1,0,0,0,1,1,1,0},
        // 2
        {1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1},
        // 3
        {1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1},
        // 4
        {1,0,0,0,1,1,0,0,0,1,1,1,1,1,1,0,0,0,0,1,0,0,0,0,1},
        // 5
        {1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1},
        // 6
        {1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1},
        // 7
        {1,1,1,1,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1},
        // 8
        {1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,1},
        // 9
        {1,1,1,1,1,1,0,0,0,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1}
    };

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
        StringBuilder detectedNumber = new StringBuilder();

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

                // Trier les contours de gauche à droite
                digitContours.sort((c1, c2) -> {
                    Rect r1 = Imgproc.boundingRect(c1);
                    Rect r2 = Imgproc.boundingRect(c2);
                    return Double.compare(r1.x, r2.x);
                });

                // Analyser chaque contour de chiffre
                for (MatOfPoint digitContour : digitContours) {
                    double digitArea = Imgproc.contourArea(digitContour);
                    if (digitArea > 100) {
                        Rect digitRect = Imgproc.boundingRect(digitContour);
                        
                        // Extraire le chiffre
                        Mat digit = new Mat(thresholdSign, digitRect);
                        
                        // Redimensionner à 5x5 pour la comparaison
                        Mat resizedDigit = new Mat();
                        Imgproc.resize(digit, resizedDigit, new Size(5, 5));
                        
                        // Convertir en tableau binaire
                        int[] digitArray = new int[25];
                        for (int i = 0; i < 5; i++) {
                            for (int j = 0; j < 5; j++) {
                                double[] pixel = resizedDigit.get(i, j);
                                digitArray[i * 5 + j] = (pixel[0] > 127) ? 1 : 0;
                            }
                        }
                        
                        // Comparer avec les modèles
                        int bestMatch = -1;
                        double bestScore = Double.MAX_VALUE;
                        
                        for (int model = 0; model < DIGIT_MODELS.length; model++) {
                            double score = 0;
                            for (int i = 0; i < 25; i++) {
                                score += Math.abs(digitArray[i] - DIGIT_MODELS[model][i]);
                            }
                            if (score < bestScore) {
                                bestScore = score;
                                bestMatch = model;
                            }
                        }
                        
                        if (bestMatch != -1) {
                            detectedNumber.append(bestMatch);
                            
                            // Dessiner le rectangle et le chiffre reconnu
                            Core.rectangle(result,
                                         new org.opencv.core.Point(boundingRect.x + digitRect.x,
                                                 boundingRect.y + digitRect.y),
                                         new org.opencv.core.Point(boundingRect.x + digitRect.x + digitRect.width,
                                                 boundingRect.y + digitRect.y + digitRect.height),
                                         new Scalar(255, 255, 255), 1);
                        }
                    }
                }
                
                // Ajouter le numéro détecté sur l'image
                if (detectedNumber.length() > 0) {
                    Core.putText(result, detectedNumber.toString(),
                               new org.opencv.core.Point(boundingRect.x, boundingRect.y - 10),
                               Core.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(0, 255, 0), 2);
                }
                
                // Créer les fenêtres pour afficher les images
                JFrame frameOriginal = new JFrame("Image Originale");
                JFrame frameResult = new JFrame("Reconnaissance des Chiffres");
                
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