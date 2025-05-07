import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AnalyseVideo {
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV chargé avec succès : " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Erreur de chargement d'OpenCV : " + e.getMessage());
            System.exit(1);
        }
    }

    // Chemins des images de référence
    private static final String[] REFERENCE_IMAGES = {
        "Images_panneaux/panneau_30.jpg",
        "Images_panneaux/panneau_50.jpg",
        "Images_panneaux/panneau_70.jpg",
        "Images_panneaux/panneau_90.jpg",
        "Images_panneaux/panneau_110.jpg",
        "Images_panneaux/panneau_voiture.jpg"
    };

    // Dernier panneau détecté
    private static String lastDetectedSign = "";
    private static int consecutiveDetections = 0;
    private static final maBibliothequeTraitementImageEtendue imageProcessor = new maBibliothequeTraitementImageEtendue();

    public static void main(String[] args) {
        System.out.println("Démarrage du programme...");
        
        // Créer la fenêtre pour la vidéo
        JFrame frame = new JFrame("Détection de Panneaux");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel imageView = new JLabel();
        frame.add(imageView);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Ouvrir la webcam
        System.out.println("Tentative d'ouverture de la webcam...");
        VideoCapture capture = new VideoCapture(0); // 0 = webcam par défaut
        System.out.println("Capture créée : " + capture);
        System.out.println("Capture ouverte : " + capture.isOpened());
        
        if (!capture.isOpened()) {
            System.out.println("Erreur: Impossible d'ouvrir la webcam");
            return;
        }

        // Obtenir les propriétés de la vidéo
        double fps = capture.get(5); // CV_CAP_PROP_FPS
        System.out.println("FPS : " + fps);

        // Configurer la résolution de la webcam
        capture.set(3, 640); // Largeur
        capture.set(4, 480); // Hauteur

        Mat videoFrame = new Mat();
        while (capture.read(videoFrame)) {
            if (videoFrame.empty()) {
                System.out.println("Erreur: Impossible de lire la trame");
                break;
            }

            // Convertir l'image en HSV
            Mat hsvImage = new Mat();
            Imgproc.cvtColor(videoFrame, hsvImage, Imgproc.COLOR_BGR2HSV);

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
                    Mat extractedSign = new Mat(videoFrame, boundingRect);
                    
                    // Redimensionner l'image extraite à une taille standard
                    Mat resizedSign = new Mat();
                    Imgproc.resize(extractedSign, resizedSign, new Size(100, 100));
                    
                    // Comparer avec les images de référence
                    String bestMatch = "";
                    double bestScore = Double.MAX_VALUE;
                    
                    for (String refImage : REFERENCE_IMAGES) {
                        double score = imageProcessor.Similitude(resizedSign, refImage);
                        if (score < bestScore) {
                            bestScore = score;
                            bestMatch = refImage;
                        }
                    }

                    // Extraire le nom du panneau de référence
                    String signName = bestMatch.substring(bestMatch.lastIndexOf('/') + 1);
                    signName = signName.substring(0, signName.lastIndexOf('.'));

                    // Vérifier si c'est un nouveau panneau
                    if (!signName.equals(lastDetectedSign)) {
                        System.out.println("Panneau détecté : " + signName);
                        lastDetectedSign = signName;
                        consecutiveDetections = 1;
                    } else {
                        consecutiveDetections++;
                    }

                    // Créer une fenêtre pour afficher le panneau détecté
                    JFrame signFrame = new JFrame("Panneau Détecté: " + signName);
                    signFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    JLabel signLabel = new JLabel(new ImageIcon(matToBufferedImage(extractedSign)));
                    signFrame.add(signLabel);
                    signFrame.pack();
                    signFrame.setLocationRelativeTo(null);
                    signFrame.setVisible(true);

                    // Dessiner le rectangle sur l'image
                    Core.rectangle(videoFrame, 
                                 new org.opencv.core.Point(boundingRect.x, boundingRect.y),
                                 new org.opencv.core.Point(boundingRect.x + boundingRect.width,
                                         boundingRect.y + boundingRect.height),
                                 new Scalar(0, 255, 0), 2);

                    // Ajouter le texte
                    Core.putText(videoFrame, signName,
                               new org.opencv.core.Point(boundingRect.x, boundingRect.y - 10),
                               Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                }
            }

            // Afficher l'image
            imageView.setIcon(new ImageIcon(matToBufferedImage(videoFrame)));
            frame.repaint();

            // Libérer les ressources
            hsvImage.release();
            mask1.release();
            mask2.release();
            redMask.release();
            hierarchy.release();

            // Ajouter un petit délai pour éviter les problèmes de buffer
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Libérer les ressources
        capture.release();
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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString();
    }
} 