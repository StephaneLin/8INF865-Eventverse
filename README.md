# 8INF865 - Projet de session - EventVerse

![Eventverse logo](/app/src/main/res/drawable/logo.png)
## Setup

### Ajout de l'application au projet Firebase

Pour que la google Authentification marche et donc que l'application marche, il faut ajouter la signature de l'application android dans le projet Firebase. Pour cela :

 1. Obtenir la signature de l'application : voir la section [Self-signing Your Application](https://developers.google.com/android/guides/client-auth#self-signing_your_application) ou directement taper la commande  `./gradlew signingReport` et récupérer la clé dans la liste variant **debug** et prendre la clé la clé **SHA1** 
 2. La rentrer dans l'application Firebase : `pages paramètres > Vos applications > Applications Android > Eventverse (com.boulin.eventverse) > Empreintes de certificat SHA`

### Création du compte organisateur (admin)

Pour créer un compte admin, il faut utiliser un des codes suivants (attention : chaque code est utilisable une seule fois !) :

 - DgCngR7siIOt6TgOhxrHZsxzrRe0c4
 - ZpcGx10AV8nX2RmAwrUNLaoUm5dlBt
 - DnsgLcIvNfHfe9KkdfeN1pBelKZ6PW

**Note :** En cas de nécessité, les codes peuvent être obtenus dans le projet Firebase : `Firestore database > organizer_codes > CODES` et en prendre un des dix

## BUGS CONNUS

Récapitulatif des bugs et fonctionnalités non terminées :

1. Erreur *NullPointerException* en cliquant sur le bouton de localisation

Si vous recevez cette erreur, allez dans **Extended Controls (...)** -> **Location**. <br>
Choisissez un point dans sur la carte, puis **SAVE POINT** la position, ensuite **SET LOCATION**.<br>
Allez dans **GoogleMap**s pour voir si la position a bien été sauvegardée.

**OU**

Allez dans les **Paramètre** de votre téléphone. <br>
Supprimez l'autorisation de la localisation pour l'application <br>
Relancez l'application.

2. Erreur "Check README" en utilisant le bouton de localisation

Si, en essayant d'utiliser le bouton de localisation, l'erreur "Check README" apparait, un correctif à chaud consiste à supprimer la permission de la localisation et à relancer l'application.

3. Crash lors de l'utilisation des boutons liés aux images dans le formulaire de création / modification d'événement

Si un crash survient lors de :
 - la demande de permission d'utilisation de la caméra
 - la demande de permission d'utilisation d'accès à la galerie d'images
un correctif à chaud consiste à relancer l'application (les permissions auront bien été enregistrées et les fonctionnalités seront donc utilisables).
