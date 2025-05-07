import javax.swing.*; // Importe les classes Swing pour l'interface graphique
import javax.swing.border.*;
import java.awt.*; // Importe des classes AWT (utilisées par Swing)
import java.awt.event.*; // Importe les classes pour gérer les événements
import java.io.File;

public class SimpleGui {
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    // Création et affichage l'interface graphique.
     
    private static void createAndShowGUI() {
        try {
            // Création de la fenêtre principale
            JFrame frame = new JFrame("Détection de Panneaux de Signalisation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setBackground(BACKGROUND_COLOR);

            // Panel principal avec un fond coloré
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(null);
            mainPanel.setBackground(BACKGROUND_COLOR);

            // Titre
            JLabel titleLabel = new JLabel("Détection de Panneaux de Signalisation", SwingConstants.CENTER);
            titleLabel.setFont(TITLE_FONT);
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setBounds(0, 20, 1200, 40);
            mainPanel.add(titleLabel);

            // Panel pour l'image d'entrée
            JPanel inputPanel = createImagePanel("Image d'entrée");
            inputPanel.setBounds(50, 80, 500, 400);
            mainPanel.add(inputPanel);

            // Panel pour l'image de résultat
            JPanel outputPanel = createImagePanel("Résultat de la détection");
            outputPanel.setBounds(600, 80, 500, 400);
            mainPanel.add(outputPanel);

            // Obtenir le chemin absolu du répertoire du projet
            String projectPath = new File("").getAbsolutePath();
            
            // Chargement de l'image d'entrée
            File imageFile = new File(projectPath, "Images_panneaux/30.jpg");
            if (!imageFile.exists()) {
                throw new RuntimeException("Image non trouvée: " + imageFile.getAbsolutePath());
            }
            
            ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
            
            // Modification de la taille de l'image
            double facteur = 0.8;
            int l = set_dimension(icon.getIconWidth(), facteur);
            int L = set_dimension(icon.getIconHeight(), facteur);
            Image image = icon.getImage().getScaledInstance(l, L, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(image);
            
            // Ajout de l'image dans le panel d'entrée
            JLabel inputImageLabel = new JLabel(scaledIcon);
            inputImageLabel.setBounds(20, 40, l, L);
            inputPanel.add(inputImageLabel);

            // Création du bouton stylisé
            JButton bouton = new JButton("Détecter les panneaux");
            styleButton(bouton);
            bouton.setBounds(450, 500, 300, 50);
            mainPanel.add(bouton);

            // Action du bouton
            bouton.addActionListener(e -> {
                try {
                    File resultatFile = new File(projectPath, "Images_panneaux/panneau_30.jpg");
                    if (!resultatFile.exists()) {
                        JOptionPane.showMessageDialog(frame, 
                            "Image résultat non trouvée: " + resultatFile.getAbsolutePath(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    ImageIcon resultat = new ImageIcon(resultatFile.getAbsolutePath());
                    int l_res = set_dimension(resultat.getIconWidth(), 0.8);
                    int L_res = set_dimension(resultat.getIconHeight(), 0.8);
                    Image image_res = resultat.getImage().getScaledInstance(l_res, L_res, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon_res = new ImageIcon(image_res);
                    
                    // Ajout de l'image résultat dans le panel de sortie
                    JLabel outputImageLabel = new JLabel(scaledIcon_res);
                    outputImageLabel.setBounds(20, 40, l_res, L_res);
                    outputPanel.removeAll();
                    outputPanel.add(outputImageLabel);
                    outputPanel.repaint();
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, 
                        "Erreur lors du chargement de l'image résultat: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                }
            });

            // Ajout du panel principal à la fenêtre
            frame.add(mainPanel);
            
            // Centrer la fenêtre sur l'écran
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Erreur lors du démarrage: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel createImagePanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBounds(0, 10, 480, 20);
        panel.add(titleLabel);

        return panel;
    }

    private static void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Effet de survol
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }

    public static int set_dimension(int mesure, double facteur) {
        return (int) Math.round(mesure * facteur);
    }
}
