# Projet de Reconnaissance de Panneaux de Signalisation

Ce projet est une application Java qui utilise OpenCV pour la détection et la reconnaissance de panneaux de signalisation routière.

## Prérequis

- Java JDK (version 8 ou supérieure)
- OpenCV 2.4.13
- IDE Java (recommandé : IntelliJ IDEA ou Eclipse)

## Structure du Projet

```
Projet/
├── src/
│   ├── Activite1.java    # Détection des pixels rouges
│   ├── Activite3.java    # Conversion et affichage HSV
│   └── SimpleGui.java    # Interface graphique principale
├── lib/
│   └── opencv-2413.jar   # Bibliothèque OpenCV
├── Images_panneaux/      # Dossier contenant les images de test
└── README.md
```

## Installation

1. Clonez le repository :
```bash
git clone [URL_DU_REPO]
```

2. Téléchargez OpenCV 2.4.13 depuis le site officiel d'OpenCV

3. Extrayez les fichiers OpenCV et copiez :
   - `opencv-2413.jar` dans le dossier `lib/`
   - Les DLL natives dans un dossier accessible (par exemple : `C:\opencv\build\java\x64\`)

## Compilation et Exécution

### Compilation

```bash
javac -cp "lib/opencv-2413.jar" src/*.java
```

### Exécution

Pour exécuter l'interface graphique principale :
```bash
java -Djava.library.path="[CHEMIN_VERS_DLL_OPENCV]" -cp "src;lib/opencv-2413.jar" SimpleGui
```

Pour exécuter l'activité 1 (détection des pixels rouges) :
```bash
java -Djava.library.path="[CHEMIN_VERS_DLL_OPENCV]" -cp "src;lib/opencv-2413.jar" Activite1
```

Pour exécuter l'activité 3 (conversion HSV) :
```bash
java -Djava.library.path="[CHEMIN_VERS_DLL_OPENCV]" -cp "src;lib/opencv-2413.jar" Activite3
```

Remplacez `[CHEMIN_VERS_DLL_OPENCV]` par le chemin vers vos DLL OpenCV (exemple : `C:\opencv\build\java\x64`)

## Fonctionnalités

- **Activite1** : Détection des pixels rouges dans une image
- **Activite3** : Conversion et affichage d'images dans l'espace colorimétrique HSV
- **SimpleGui** : Interface graphique pour la visualisation et le traitement des images


## Auteurs

- KERRAZI EL YAZID

## Licence


