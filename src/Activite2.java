import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Activite2 {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Charger l'image
        Mat image = Highgui.imread("Images_panneaux/30.jpg");
        if (image.empty()) {
            System.out.println("Erreur: Impossible de charger l'image");
            return;
        }

        // Créer une matrice pour stocker la composante verte
        Mat greenChannel = new Mat();
        
        // Extraire les canaux de couleur
        java.util.List<Mat> channels = new java.util.ArrayList<>();
        Core.split(image, channels);
        
        // Récupérer uniquement le canal vert (index 1 car BGR)
        greenChannel = channels.get(1);

        // Créer une fenêtre pour afficher l'image
        JFrame frame = new JFrame("Composante Verte");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Convertir Mat en BufferedImage
        BufferedImage bufferedImage = matToBufferedImage(greenChannel);
        
        // Créer un JLabel pour afficher l'image
        JLabel label = new JLabel(new ImageIcon(bufferedImage));
        frame.add(label);
        
        // Ajuster la taille de la fenêtre à l'image
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Libérer les ressources
        image.release();
        greenChannel.release();
        for (Mat channel : channels) {
            channel.release();
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