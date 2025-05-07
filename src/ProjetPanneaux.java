import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjetPanneaux {
    // Constantes pour l'interface graphique
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Chemins des images de référence
    private static final String[] REFERENCE_IMAGES = {
        "Images_panneaux/panneau_30.jpg",
        "Images_panneaux/panneau_50.jpg",
        "Images_panneaux/panneau_70.jpg",
        "Images_panneaux/panneau_90.jpg",
        "Images_panneaux/panneau_110.jpg",
        "Images_panneaux/panneau_voiture.jpg"
    };

    // Variables pour la détection
    private static String lastDetectedSign = "";
    private static int consecutiveDetections = 0;
    private static final maBibliothequeTraitementImageEtendue imageProcessor = new maBibliothequeTraitementImageEtendue();

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV chargé avec succès : " + Core.VERSION);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Erreur de chargement d'OpenCV : " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // Création de la fenêtre principale
        JFrame mainFrame = new JFrame("Détection de Panneaux de Signalisation");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 800);
        mainFrame.setBackground(BACKGROUND_COLOR);

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Titre
        JLabel titleLabel = new JLabel("Détection de Panneaux de Signalisation", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel central pour les images
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Panels pour l'image d'entrée et de sortie
        ImagePanel inputImagePanel = createImagePanel("Image d'entrée");
        ImagePanel outputImagePanel = createImagePanel("Résultat de la détection");
        centerPanel.add(inputImagePanel.panel);
        centerPanel.add(outputImagePanel.panel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel pour les boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Bouton pour l'activité 1 (Détection de couleur)
        JButton btnActivite1 = createButton("Activité 1: Détection de Couleur");
        btnActivite1.addActionListener(e -> runActivite1(inputImagePanel.imageLabel, outputImagePanel.imageLabel));
        buttonPanel.add(btnActivite1);

        // Bouton pour l'activité 3 (Affichage HSV)
        JButton btnActivite3 = createButton("Activité 3: Affichage HSV");
        btnActivite3.addActionListener(e -> runActivite3(inputImagePanel.imageLabel, outputImagePanel.imageLabel));
        buttonPanel.add(btnActivite3);

        // Bouton pour l'analyse vidéo
        JButton btnVideo = createButton("Analyse Vidéo");
        btnVideo.addActionListener(e -> runAnalyseVideo());
        buttonPanel.add(btnVideo);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Ajouter le panel principal à la fenêtre
        mainFrame.add(mainPanel);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    private static ImagePanel createImagePanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        return new ImagePanel(panel, imageLabel);
    }

    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private static void runActivite1(JLabel inputLabel, JLabel outputLabel) {
        // Charger l'image
        Mat image = Highgui.imread("Images_panneaux/30.jpg");
        if (image.empty()) {
            JOptionPane.showMessageDialog(null, "Erreur: Impossible de charger l'image");
            return;
        }
        inputLabel.setIcon(new ImageIcon(matToBufferedImage(image)));
        // Convertir en HSV et créer le masque rouge
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        Scalar lowerRed1 = new Scalar(0, 100, 100);
        Scalar upperRed1 = new Scalar(10, 255, 255);
        Scalar lowerRed2 = new Scalar(170, 100, 100);
        Scalar upperRed2 = new Scalar(180, 255, 255);
        Mat mask1 = new Mat();
        Mat mask2 = new Mat();
        Core.inRange(hsvImage, lowerRed1, upperRed1, mask1);
        Core.inRange(hsvImage, lowerRed2, upperRed2, mask2);
        Mat redMask = new Mat();
        Core.addWeighted(mask1, 1.0, mask2, 1.0, 0.0, redMask);
        // Appliquer le masque à l'image originale
        Mat result = new Mat();
        Core.bitwise_and(image, image, result, redMask);
        outputLabel.setIcon(new ImageIcon(matToBufferedImage(result)));
        image.release();
        hsvImage.release();
        mask1.release();
        mask2.release();
        redMask.release();
        result.release();
    }

    private static void runActivite3(JLabel inputLabel, JLabel outputLabel) {
        // Charger l'image
        Mat image = Highgui.imread("Images_panneaux/30.jpg");
        if (image.empty()) {
            JOptionPane.showMessageDialog(null, "Erreur: Impossible de charger l'image");
            return;
        }
        inputLabel.setIcon(new ImageIcon(matToBufferedImage(image)));
        // Convertir en HSV
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        outputLabel.setIcon(new ImageIcon(matToBufferedImage(hsvImage)));
        image.release();
        hsvImage.release();
    }

    private static void runAnalyseVideo() {
        // Créer la fenêtre pour la vidéo
        JFrame frame = new JFrame("Détection de Panneaux");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel imageView = new JLabel();
        frame.add(imageView);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Ouvrir la webcam
        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(null, "Erreur: Impossible d'ouvrir la webcam");
            return;
        }

        // Configurer la résolution
        capture.set(3, 640);
        capture.set(4, 480);

        Mat videoFrame = new Mat();
        while (capture.read(videoFrame)) {
            if (videoFrame.empty()) break;

            // Convertir en HSV et détecter les panneaux
            Mat hsvImage = new Mat();
            Imgproc.cvtColor(videoFrame, hsvImage, Imgproc.COLOR_BGR2HSV);

            Scalar lowerRed1 = new Scalar(0, 100, 100);
            Scalar upperRed1 = new Scalar(10, 255, 255);
            Scalar lowerRed2 = new Scalar(170, 100, 100);
            Scalar upperRed2 = new Scalar(180, 255, 255);

            Mat mask1 = new Mat();
            Mat mask2 = new Mat();
            Core.inRange(hsvImage, lowerRed1, upperRed1, mask1);
            Core.inRange(hsvImage, lowerRed2, upperRed2, mask2);

            Mat redMask = new Mat();
            Core.addWeighted(mask1, 1.0, mask2, 1.0, 0.0, redMask);

            Imgproc.GaussianBlur(redMask, redMask, new Size(5, 5), 0);

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(redMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area < 1000) continue;

                double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
                double circularity = 4 * Math.PI * area / (perimeter * perimeter);

                if (circularity > 0.8) {
                    Rect boundingRect = Imgproc.boundingRect(contour);
                    Mat extractedSign = new Mat(videoFrame, boundingRect);
                    Mat resizedSign = new Mat();
                    Imgproc.resize(extractedSign, resizedSign, new Size(100, 100));

                    String bestMatch = "";
                    double bestScore = Double.MAX_VALUE;

                    for (String refImage : REFERENCE_IMAGES) {
                        double score = imageProcessor.Similitude(resizedSign, refImage);
                        if (score < bestScore) {
                            bestScore = score;
                            bestMatch = refImage;
                        }
                    }

                    String signName = bestMatch.substring(bestMatch.lastIndexOf('/') + 1);
                    signName = signName.substring(0, signName.lastIndexOf('.'));

                    if (!signName.equals(lastDetectedSign)) {
                        System.out.println("Panneau détecté : " + signName);
                        lastDetectedSign = signName;
                        consecutiveDetections = 1;

                        JFrame signFrame = new JFrame("Panneau Détecté: " + signName);
                        signFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        JLabel signLabel = new JLabel(new ImageIcon(matToBufferedImage(extractedSign)));
                        signFrame.add(signLabel);
                        signFrame.pack();
                        signFrame.setLocationRelativeTo(null);
                        signFrame.setVisible(true);
                    } else {
                        consecutiveDetections++;
                    }

                    Core.rectangle(videoFrame, 
                                 new org.opencv.core.Point(boundingRect.x, boundingRect.y),
                                 new org.opencv.core.Point(boundingRect.x + boundingRect.width,
                                         boundingRect.y + boundingRect.height),
                                 new Scalar(0, 255, 0), 2);

                    Core.putText(videoFrame, signName,
                               new org.opencv.core.Point(boundingRect.x, boundingRect.y - 10),
                               Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                }
            }

            imageView.setIcon(new ImageIcon(matToBufferedImage(videoFrame)));
            frame.repaint();

            hsvImage.release();
            mask1.release();
            mask2.release();
            redMask.release();
            hierarchy.release();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        capture.release();
    }

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

    // Ajout d'une classe utilitaire pour regrouper panel et label
    private static class ImagePanel {
        JPanel panel;
        JLabel imageLabel;
        ImagePanel(JPanel panel, JLabel imageLabel) {
            this.panel = panel;
            this.imageLabel = imageLabel;
        }
    }
} 