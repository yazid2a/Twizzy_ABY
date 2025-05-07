import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import java.io.File;

public class TestSimilitude {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // Obtenir le chemin absolu du projet
        String projectPath = new File("").getAbsolutePath();
        
        // Charger une image de test
        String testImagePath = projectPath + "/Images_panneaux/30.jpg";
        System.out.println("Chargement de l'image : " + testImagePath);
        
        Mat testImage = Highgui.imread(testImagePath);
        if (testImage.empty()) {
            System.out.println("Erreur: Impossible de charger l'image de test");
            return;
        }

        // Tableau des images de référence
        String[] referenceImages = {
            projectPath + "/Images_panneaux/panneau_30.jpg",
            projectPath + "/Images_panneaux/panneau_50.jpg",
            projectPath + "/Images_panneaux/panneau_70.jpg",
            projectPath + "/Images_panneaux/panneau_90.jpg",
            projectPath + "/Images_panneaux/panneau_110.jpg",
            projectPath + "/Images_panneaux/panneau_voiture.jpg"
        };

        // Tester la similarité avec chaque image de référence
        String bestMatch = "";
        double bestScore = Double.MAX_VALUE;

        System.out.println("Test de similarité pour " + testImagePath);
        System.out.println("----------------------------------------");

        for (String refImage : referenceImages) {
            System.out.println("Comparaison avec : " + refImage);
            double score = maBibliothequeTraitementImageEtendue.Similitude(testImage, refImage);
            System.out.printf("Score : %.2f%n", score);

            if (score < bestScore) {
                bestScore = score;
                bestMatch = refImage;
            }
        }

        System.out.println("----------------------------------------");
        System.out.println("Meilleure correspondance : " + bestMatch);
        System.out.printf("Score : %.2f%n", bestScore);

        // Libérer les ressources
        testImage.release();
    }
} 