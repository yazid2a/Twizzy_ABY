import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Activite1 {
    static {
        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Charger l'image
        Mat image = Highgui.imread("Images_panneaux/30.jpg");
        if (image.empty()) {
            System.out.println("Erreur: Impossible de charger l'image");
            return;
        }

        // Convertir l'image en HSV
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Définir les seuils pour la couleur rouge (en HSV)
        // Le rouge en HSV est autour de 0° ou 180° (0-10 ou 170-180)
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
        Imgproc.GaussianBlur(redMask, redMask, new Size(3, 3), 0);

        // Afficher le résultat
        System.out.println("Visualisation de la détection du rouge (+ = pixel rouge, . = autre) :");
        for (int y = 0; y < redMask.rows(); y += 2) { // On saute une ligne sur 2 pour une meilleure visualisation
            for (int x = 0; x < redMask.cols(); x += 2) { // On saute une colonne sur 2
                double[] pixel = redMask.get(y, x);
                if (pixel[0] > 0) {
                    System.out.print("+");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }

        // Calculer le pourcentage de pixels rouges
        int totalPixels = redMask.rows() * redMask.cols();
        int redPixels = Core.countNonZero(redMask);
        double percentage = (redPixels * 100.0) / totalPixels;
        System.out.printf("\nPourcentage de pixels rouges détectés : %.2f%%\n", percentage);

        // Libérer les ressources
        image.release();
        hsvImage.release();
        mask1.release();
        mask2.release();
        redMask.release();
    }
} 