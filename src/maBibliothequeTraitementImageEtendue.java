import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import java.util.Arrays;

public class maBibliothequeTraitementImageEtendue {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Calcule la similarité entre deux images
     * @param object L'image du panneau détecté
     * @param signfile Le nom du fichier du panneau de référence
     * @return Un score de similarité (plus la valeur est basse, plus les images sont similaires)
     */
    public static double Similitude(Mat object, String signfile) {
        // Charger l'image de référence
        Mat reference = Highgui.imread(signfile);
        if (reference.empty()) {
            return Double.MAX_VALUE;
        }

        try {
            // Redimensionner les deux images à la même taille
            Mat resizedObject = new Mat();
            Mat resizedReference = new Mat();
            Size size = new Size(100, 100);
            Imgproc.resize(object, resizedObject, size);
            Imgproc.resize(reference, resizedReference, size);

            // Convertir les images en niveaux de gris
            Mat grayObject = new Mat();
            Mat grayReference = new Mat();
            Imgproc.cvtColor(resizedObject, grayObject, Imgproc.COLOR_BGR2GRAY);
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

            // Calculer les histogrammes
            MatOfInt histSize = new MatOfInt(256);
            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt channels = new MatOfInt(0);
            
            Mat histObject = new Mat();
            Mat histReference = new Mat();
            
            Imgproc.calcHist(Arrays.asList(grayObject), channels, new Mat(), histObject, histSize, ranges);
            Imgproc.calcHist(Arrays.asList(grayReference), channels, new Mat(), histReference, histSize, ranges);

            // Normaliser les histogrammes
            Core.normalize(histObject, histObject, 0, 1, Core.NORM_MINMAX, -1, new Mat());
            Core.normalize(histReference, histReference, 0, 1, Core.NORM_MINMAX, -1, new Mat());

            // Calculer la différence entre les histogrammes
            double histScore = Imgproc.compareHist(histObject, histReference, Imgproc.CV_COMP_CORREL);
            histScore = 1.0 - Math.abs(histScore); // Convertir en distance (0 = identique, 1 = différent)

            // Calculer la différence absolue des pixels
            Mat diff = new Mat();
            Core.absdiff(thresholdObject, thresholdReference, diff);
            double pixelDiff = 0;
            for (int i = 0; i < diff.rows(); i++) {
                for (int j = 0; j < diff.cols(); j++) {
                    double[] pixel = diff.get(i, j);
                    pixelDiff += pixel[0];
                }
            }
            pixelDiff = pixelDiff / (diff.rows() * diff.cols() * 255.0); // Normalisation entre 0 et 1

            // Combiner les deux scores avec des poids
            double score = 0.5 * histScore + 0.5 * pixelDiff;

            // Libérer les ressources
            resizedObject.release();
            resizedReference.release();
            grayObject.release();
            grayReference.release();
            thresholdObject.release();
            thresholdReference.release();
            histObject.release();
            histReference.release();
            diff.release();
            histSize.release();
            ranges.release();
            channels.release();

            return score;

        } finally {
            reference.release();
        }
    }
} 