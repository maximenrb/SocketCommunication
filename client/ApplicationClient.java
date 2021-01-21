import java.io.*;
import java.net.Socket;

public class ApplicationClient {
    BufferedReader commandBuffer = null;

    public ApplicationClient() {
        System.out.println("Create ApplicationClient instance");
    }

    /**
     *  Prend le fichier contenant la liste des commandes, et le charge dans une
     *  variable du type Command qui est retournée
     */
    public Command getNextCommand() {
        if (commandBuffer != null) {
            try {
                String currentCommand;

                if ((currentCommand = commandBuffer.readLine()) != null) {
                    System.out.println("Command line: " + currentCommand);
                    return new Command();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     *  Initialise : ouvre les différents fichiers de lecture et écriture
     */
    public void initialise(String commandFile, String OutputFile) {
        try {
            commandBuffer = new BufferedReader(new FileReader(commandFile));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Command file not found");
            System.exit(-1);
        }


    }

    /**
     *  Prend une Command dûment formatée, et la fait exécuter par le serveur. Le résultat de
     *  l’exécution est retournée. Si la commande ne retourne pas de résultat, on retourne null.
     *	Chaque appel doit ouvrir une connexion, exécuter, et fermer la connexion. Si vous le
     *  souhaitez, vous pourriez écrire six fonctions spécialisées, une par type de commande
     *  décrit plus haut, qui seront appelées par  traiteCommande(Command command)
     */
    public Object treatCommand(Command command) {
        return null;
    }

    /**
     *  Cette méthode vous sera fournie plus tard. Elle indiquera la séquence d’étapes à exécuter
     *  pour le test. Elle fera des appels successifs à saisisCommande(BufferedReader fichier) et
     *  treatCommand(Commande uneCommande).
     */
    public void scenario() {
        System.out.println("Starting treatments:");
        Command command = getNextCommand();

        while (command != null) {
            System.out.println("\tTreatment of: " + command);
            Object result = treatCommand(command);

            System.out.println("\t\tResult: " + result);
            command = getNextCommand();
        }

        System.out.println("End of treatments");
    }


    /**
     *  > Programme principal
     *  Prend 4 arguments: 1) “hostname” du serveur, 2) numéro de port, 3) nom fichier commandes,
     *  et 4) nom fichier sortie. Cette méthode doit créer une instance de la classe ApplicationClient,
     *  l’initialiser, puis exécuter le scénario
     */
    public static void main(String[] args) {
        // Variables declaration
        String hostname = null, commandFilePath = null, outputFilePath = null;
        int port = -1;

        boolean error = false;

        // Verify if number of arguments is ok (equal to 4)
        if (args.length != 4) {
            printCorrectUsageAndExit();

        } else {
            // Get first argument (hostname)
            hostname = args[0];

            try {
                // Get second argument (port), and verify if argument is a number
                port = Integer.parseInt(args[1]);

            } catch (NumberFormatException numberFormatException) {
                System.out.println("Entered port is not a number !");
                error = true;
            }

            // Get third argument (command file path)
            commandFilePath = args[2];

            // Verify if command file exist
            if(!new File(commandFilePath).exists()) {
                System.out.println("Command file not exist !");
                error = true;
            }

            // Get last argument (output file path)
            outputFilePath = args[3];

            // Print user arguments
            System.out.println("Hostname: " + hostname + " || Port: " + port + " || Command file: " + commandFilePath
                    + " || Output file: " + outputFilePath);

        }

        if(error) {
            printCorrectUsageAndExit();

        } else {
            // Create ApplicationClient instance, and initialise
            ApplicationClient applicationClient = new ApplicationClient();
            applicationClient.initialise(commandFilePath, outputFilePath);
            applicationClient.scenario();

//            try {
//                Socket socket = new Socket(hostname, port);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public static void printCorrectUsageAndExit() {
        System.out.println("Correct usage : java ApplicationClient.java <hostname> <port> <command file> <output file>");
        System.exit(-1);
    }
}