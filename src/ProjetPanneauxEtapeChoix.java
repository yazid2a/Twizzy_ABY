import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProjetPanneauxEtapeChoix extends JFrame {
    static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    private VideoCapture capture;
    private Mat frame;
    private JLabel imageLabel;
    private JComboBox<String> stepComboBox;
    private volatile String currentStep = "Original";
    private boolean running = true;

    public ProjetPanneauxEtapeChoix() {
        super("Reconnaissance de Panneaux - Visualisation par étape");

        // Interface
        imageLabel = new JLabel();
        stepComboBox = new JComboBox<>(new String[]{
                "Original",
                "Grayscale",
                "Binary",
                "HSV",
                "Mask",
                "Contours",
                "Detection"
        });
        stepComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentStep = (String) stepComboBox.getSelectedItem();
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Étape de traitement :"));
        topPanel.add(stepComboBox);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(imageLabel, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);

        // Capture webcam
        capture = new VideoCapture(0); // 0 = webcam par défaut
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir la webcam !");
            System.exit(1);
        }

        // Thread d'affichage vidéo
        new Thread(new Runnable() {
            public void run() {
                frame = new Mat();
                while (running) {
                    if (capture.read(frame)) {
                        Mat processed = processFrame(frame, currentStep);
                        Image img = toBufferedImage(processed);
                        if (img != null) {
                            ImageIcon icon = new ImageIcon(img);
                            imageLabel.setIcon(icon);
                        }
                    }
                    try { Thread.sleep(30); } catch (InterruptedException e) {}
                }
                capture.release();
            }
        }).start();
    }

    private Mat processFrame(Mat input, String step) {
        Mat result = new Mat();
        switch (step) {
            case "Original":
                input.copyTo(result);
                break;
            case "Grayscale":
                Imgproc.cvtColor(input, result, Imgproc.COLOR_BGR2GRAY);
                Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2BGR); // Pour affichage couleur
                break;
            case "Binary":
                Mat gray = new Mat();
                Imgproc.cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(gray, result, 100, 255, Imgproc.THRESH_BINARY);
                Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2BGR);
                break;
            case "HSV":
                Imgproc.cvtColor(input, result, Imgproc.COLOR_BGR2HSV);
                Imgproc.cvtColor(result, result, Imgproc.COLOR_HSV2BGR); // Pour affichage
                break;
            case "Mask":
                Mat hsv = new Mat();
                Imgproc.cvtColor(input, hsv, Imgproc.COLOR_BGR2HSV);
                // Détection du rouge (exemple)
                Scalar lowerRed1 = new Scalar(0, 100, 100);
                Scalar upperRed1 = new Scalar(10, 255, 255);
                Scalar lowerRed2 = new Scalar(160, 100, 100);
                Scalar upperRed2 = new Scalar(179, 255, 255);
                Mat mask1 = new Mat(), mask2 = new Mat(), mask = new Mat();
                Core.inRange(hsv, lowerRed1, upperRed1, mask1);
                Core.inRange(hsv, lowerRed2, upperRed2, mask2);
                Core.add(mask1, mask2, mask);
                Imgproc.cvtColor(mask, result, Imgproc.COLOR_GRAY2BGR);
                break;
            case "Contours":
                Mat gray2 = new Mat();
                Imgproc.cvtColor(input, gray2, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(gray2, gray2, new Size(5,5), 0);
                Imgproc.Canny(gray2, result, 100, 200);
                Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2BGR);
                break;
            case "Detection":
                input.copyTo(result);
                // Détection de panneaux circulaires rouges
                Mat hsv2 = new Mat();
                Imgproc.cvtColor(input, hsv2, Imgproc.COLOR_BGR2HSV);
                Scalar lowerRed3 = new Scalar(0, 100, 100);
                Scalar upperRed3 = new Scalar(10, 255, 255);
                Scalar lowerRed4 = new Scalar(160, 100, 100);
                Scalar upperRed4 = new Scalar(179, 255, 255);
                Mat mask3 = new Mat(), mask4 = new Mat(), maskRed = new Mat();
                Core.inRange(hsv2, lowerRed3, upperRed3, mask3);
                Core.inRange(hsv2, lowerRed4, upperRed4, mask4);
                Core.add(mask3, mask4, maskRed);

                // Trouver les contours
                java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(maskRed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

                for (int i = 0; i < contours.size(); i++) {
                    double area = Imgproc.contourArea(contours.get(i));
                    if (area > 500) { // Seuil d'aire
                        // Approximation du cercle
                        org.opencv.core.Point center = new org.opencv.core.Point();
                        float[] radius = new float[1];
                        MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
                        Imgproc.minEnclosingCircle(contour2f, center, radius);
                        if (radius[0] > 20) {
                            Core.circle(result, center, (int)radius[0], new Scalar(0,255,0), 3);
                        }
                    }
                }
                break;
            default:
                input.copyTo(result);
        }
        return result;
    }

    private Image toBufferedImage(Mat m) {
        if (m.empty()) return null;
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (m.channels() == 1) type = BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0,0,b);
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ProjetPanneauxEtapeChoix();
            }
        });
    }
} 