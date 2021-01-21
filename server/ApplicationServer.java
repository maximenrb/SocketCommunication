import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ApplicationServer {
    private ServerSocket serverSocket;

    /**
     *  > Constructeur
     *  Prend le numéro de port, crée un SocketServer sur le port
     */
    public ApplicationServer(int port) {
        System.out.println("Create ApplicationServer instance");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Socket server listening on port " + port);

            waitClient();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *	Se met en attente de connexions des clients. Suite aux connexions, elle lit
     *	ce qui est envoyé à travers la Socket, recrée l’objet Command envoyé par
     *	le client, et appellera traiterCommande(Command command)
     */
    public void waitClient() {
        try {
            System.out.println("Waiting for connections...");
            Socket client = serverSocket.accept();

            System.out.println("Accepted a connection from: " + client.getInetAddress());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Prend une Commande dument formattée, et la traite. Dépendant du type de commande,
     *  elle appelle la méthode spécialisée
     */
    public void treatCommand(Command command) { }

    /**
     *  Traite la lecture d’un attribut. Renvoies le résultat par le socket
     */
    public void treatRead(Object objectPointer, String attribute) { }

    /**
     *  Traite l’écriture d’un attribut. Confirmes au client que l’écriture s’est faite correctement.
     */
    public void treatWrite(Object objectPointer, String attribute, Object value) { }

    /**
     *  Traite la création d’un objet. Confirme au client que la création s’est faite correctement.
     */
    public void treatCreate(Class classeDeLobjet, String identificateur) { }

    /**
     *  Traite le chargement d’une classe. Confirmes au client que la création s’est faite correctement.
     */
    public void treatLoad(String nomQualifie) { }

    /**
     *  Traite la compilation d’un fichier source java. Confirme au client que
     *  la compilation s’est faite correctement. Le fichier source est donné par son chemin
     *  relatif par rapport au chemin des fichiers sources.
     */
    public void treatCompilation(String cheminRelatifFichierSource) { }

    /**
     *  Traite l’appel d’une méthode, en prenant comme argument l’objet sur lequel on effectue l'appel,
     *  le nom de la fonction à appeler, un tableau de nom de types des arguments, et un tableau d'arguments
     *  pour la fonction. Le résultat de la fonction est renvoyé par le serveur au client (ou le message que tout s’est bien passé)
     */
    public void treatCall(Object objectPointer, String functionName, String[] types, Object[] values) { }

    /**
     *  > Programme principal.
     *  Prend 4 arguments: 1) numéro de port, 2) répertoire source, 3) répertoire classes,
     *  et 4) nom du fichier de traces (sortie). Cette méthode doit créer une instance de
     *  la classe ApplicationServeur, l’initialiser puis appeler aVosOrdres sur cet objet
     */
    public static void main(String[] args) {
        // Variables declaration
        int port = -1;
        String sourceDirectory = null, classesDirectory = null, logFilePath = null;

        boolean error = false;

        // Verify if number of arguments is ok (equal to 4)
        if (args.length != 4) {
            printCorrectUsageAndExit();

        } else {
            try {
                // Get first argument (port), and verify if argument is a number
                port = Integer.parseInt(args[0]);

            } catch (NumberFormatException numberFormatException) {
                System.out.println("Entered port is not a number !");
                error = true;
            }

            // Get second argument (source directory path)
            sourceDirectory = args[1];

            // Verify if directory exist
            if(!new File(sourceDirectory).isDirectory()) {
                System.out.println("Entered source directory not exist or is not a directory !");
                error = true;
            }

            // Get third argument (classes directory path)
            classesDirectory = args[2];

            // Verify if directory exist
            if(!new File(classesDirectory).isDirectory()) {
                System.out.println("Entered classes directory not exist or is not a directory !");
                error = true;
            }

            // Get last argument (log file path)
            logFilePath = args[3];

            // Print user arguments
            System.out.println("Port: " + port + " || Source directory: " + sourceDirectory + " || Classes directory: "
                    + classesDirectory + " || Log file: " + logFilePath);
        }

        if(error) {
            printCorrectUsageAndExit();

        } else {
            // Create ApplicationServer instance
            ApplicationServer applicationServer = new ApplicationServer(port);
        }
    }

    public static void printCorrectUsageAndExit() {
        System.out.println("Correct usage : java ApplicationServer.java <port> <source directory> <classes directory> <log file>");
        System.exit(-1);
    }
}