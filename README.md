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
│   ├── AnalyseVideo.java         # Détection en temps réel
│   ├── ProjetPanneauxEtapeChoix.java    # Interface avec étapes
│   └── maBibliothequeTraitementImageEtendue.java    # Bibliothèque de traitement
├── lib/
│   └── opencv-2413.jar   # Bibliothèque OpenCV
├── dll/
│   └── opencv_java2413.dll   # DLL OpenCV
├── Images_panneaux/      # Dossier contenant les images de référence
└── README.md
```

## Installation

1. Clonez le repository :
```bash
git clone [URL_DU_REPO]
```

2. Assurez-vous que les DLL d'OpenCV sont présentes dans le dossier `dll/`

## Compilation et Exécution

### Méthode Simple (Recommandée)

Exécutez simplement le fichier `run.bat` qui se trouve à la racine du projet. Ce fichier configure automatiquement les chemins nécessaires et lance l'application.

```bash
.\run.bat
```

### Méthode Manuelle

Si vous souhaitez compiler et exécuter manuellement :

1. Compilation :
```bash
javac -cp "lib/opencv-2413.jar" src/*.java
```

2. Exécution :
   
Pour lancer la détection en temps réel :
```bash
java -Djava.library.path="dll" -cp "src;lib/opencv-2413.jar" AnalyseVideo
```

Pour lancer l'interface avec les étapes de traitement :
```bash
java -Djava.library.path="dll" -cp "src;lib/opencv-2413.jar" ProjetPanneauxEtapeChoix
```

## Fonctionnalités

- **AnalyseVideo** : Détection et reconnaissance en temps réel des panneaux
- **ProjetPanneauxEtapeChoix** : Interface permettant de visualiser les différentes étapes du traitement :
  - Image originale
  - Niveaux de gris
  - Image binaire
  - Espace HSV
  - Masque de détection
  - Contours
  - Détection finale

## Auteurs

- KERRAZI EL YAZID

## Licence


