import org.opencv.core.*;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class ProjetPanneauxEnchaine {
    private static final String[] REFERENCE_IMAGES = {
        "Images_panneaux/panneau_30.jpg",
        "Images_panneaux/panneau_50.jpg",
        "Images_panneaux/panneau_70.jpg",
        "Images_panneaux/panneau_90.jpg",
        "Images_panneaux/panneau_110.jpg",
        "Images_panneaux/panneau_voiture.jpg"
    };
    private static final maBibliothequeTraitementImageEtendue imageProcessor = new maBibliothequeTraitementImageEtendue();
    private static String lastDetectedSign = "";

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProjetPanneauxEnchaine::runPipeline);
    }

    private static void runPipeline() {
        JFrame frame = new JFrame("Pipeline Détection de Panneaux");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 700);
        frame.setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        JLabel[] labels = new JLabel[7];
        String[] titles = {"Originale", "Gris", "Noir/Blanc", "HSV", "Masque Rouge", "Contours", "Détection"};
        for (int i = 0; i < labels.length; i++) {
            JPanel p = new JPanel(new BorderLayout());
            JLabel title = new JLabel(titles[i], SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 14));
            labels[i] = new JLabel();
            labels[i].setHorizontalAlignment(SwingConstants.CENTER);
            p.add(title, BorderLayout.NORTH);
            p.add(labels[i], BorderLayout.CENTER);
            gridPanel.add(p);
        }
        // Ajouter un panneau vide pour compléter la grille 2x4
        gridPanel.add(new JPanel());

        frame.add(gridPanel, BorderLayout.CENTER);
        JButton quitBtn = new JButton("Quitter");
        quitBtn.addActionListener(e -> System.exit(0));
        JPanel btnPanel = new JPanel();
        btnPanel.add(quitBtn);
        frame.add(btnPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        VideoCapture capture = new VideoCapture(0);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(null, "Erreur: Impossible d'ouvrir la webcam");
            return;
        }
        capture.set(3, 640);
        capture.set(4, 480);

        Mat frameMat = new Mat();
        while (capture.read(frameMat)) {
            if (frameMat.empty()) break;
            // 1. Originale
            labels[0].setIcon(new ImageIcon(matToBufferedImage(frameMat)));
            // 2. Gris
            Mat gray = new Mat();
            Imgproc.cvtColor(frameMat, gray, Imgproc.COLOR_BGR2GRAY);
            labels[1].setIcon(new ImageIcon(matToBufferedImage(gray)));
            // 3. Noir/Blanc
            Mat bw = new Mat();
            Imgproc.threshold(gray, bw, 100, 255, Imgproc.THRESH_BINARY);
            labels[2].setIcon(new ImageIcon(matToBufferedImage(bw)));
            // 4. HSV
            Mat hsv = new Mat();
            Imgproc.cvtColor(frameMat, hsv, Imgproc.COLOR_BGR2HSV);
            labels[3].setIcon(new ImageIcon(matToBufferedImage(hsv)));
            // 5. Masque Rouge
            Scalar lowerRed1 = new Scalar(0, 100, 100);
            Scalar upperRed1 = new Scalar(10, 255, 255);
            Scalar lowerRed2 = new Scalar(170, 100, 100);
            Scalar upperRed2 = new Scalar(180, 255, 255);
            Mat mask1 = new Mat();
            Mat mask2 = new Mat();
            Core.inRange(hsv, lowerRed1, upperRed1, mask1);
            Core.inRange(hsv, lowerRed2, upperRed2, mask2);
            Mat redMask = new Mat();
            Core.addWeighted(mask1, 1.0, mask2, 1.0, 0.0, redMask);
            labels[4].setIcon(new ImageIcon(matToBufferedImage(redMask)));
            // 6. Contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(redMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            Mat contourImg = frameMat.clone();
            Imgproc.drawContours(contourImg, contours, -1, new Scalar(0,255,0), 2);
            labels[5].setIcon(new ImageIcon(matToBufferedImage(contourImg)));
            // 7. Détection
            Mat detectionImg = frameMat.clone();
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area < 1000) continue;
                double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
                double circularity = 4 * Math.PI * area / (perimeter * perimeter);
                if (circularity > 0.8) {
                    Rect boundingRect = Imgproc.boundingRect(contour);
                    Mat extractedSign = new Mat(frameMat, boundingRect);
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
                    }
                    Core.rectangle(detectionImg, 
                        new org.opencv.core.Point(boundingRect.x, boundingRect.y),
                        new org.opencv.core.Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height),
                        new Scalar(0,255,0), 2);
                    Core.putText(detectionImg, signName,
                        new org.opencv.core.Point(boundingRect.x, boundingRect.y - 10),
                        Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0,255,0), 2);
                }
            }
            labels[6].setIcon(new ImageIcon(matToBufferedImage(detectionImg)));
            // Libération mémoire
            gray.release(); bw.release(); hsv.release(); mask1.release(); mask2.release(); redMask.release(); hierarchy.release(); contourImg.release(); detectionImg.release();
            try { Thread.sleep(20); } catch (InterruptedException e) { e.printStackTrace(); }
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
} 