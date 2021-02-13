import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ApplicationClient {
    BufferedReader commandBuffer = null;
    String outFilePath;

    String hostname;
    int port;

    public ApplicationClient(String hostname, int port) {
        System.out.println("Create ApplicationClient instance");

        this.hostname = hostname;
        this.port = port;
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
                    System.out.println("Command: " + currentCommand);
                    return new Command(currentCommand);
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
        outFilePath = OutputFile;
        
        try {
            commandBuffer = new BufferedReader(new FileReader(commandFile));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Command file not found");
            System.exit(-1);
        }
    }

    public void writeInFile(String result) {
        try {
            FileWriter fw = new FileWriter(outFilePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(result);
            bw.newLine();
            bw.close();
            fw.close();

            System.out.println("Successfully wrote to the file.");

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
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
        try {
            // 1. Creating a new socket
            // System.out.println("Opening socket at " + hostname + ":" + port);
            Socket socket = new Socket(hostname, port);

            // 2. Getting an OutputStream from the socket
            // 3. Creating an ObjectOutputStream from the OutputStream
            ObjectOutput outputStream = new ObjectOutputStream(socket.getOutputStream());

            // 2. Get an InputStream from the socket server
            // 3. Create an ObjectInputStream from the InputStream
            ObjectInput inputStream = new ObjectInputStream(socket.getInputStream());

            Object result = null;

            // 4. Writing object
            // System.out.println("Writing command object");
            outputStream.writeObject(command);

            try {
                result = inputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            // 5. Close Stream and socket
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            socket.close();

            return result;

        } catch (IOException e) {
            e.printStackTrace();

            System.out.println("Error with socket, check stack trace");
            System.exit(-2);
        }

        return null;
    }

    /**
     *  Cette méthode vous sera fournie plus tard. Elle indiquera la séquence d’étapes à exécuter
     *  pour le test. Elle fera des appels successifs à saisisCommande(BufferedReader fichier) et
     *  treatCommand(Commande uneCommande).
     */
    public void scenario() {
        System.out.println("Starting scenario:");
        Command command = getNextCommand();

        while (command != null) {
            //System.out.println("\tTreatment of: " + command);
            Object resultObj = treatCommand(command);
            writeInFile(String.valueOf(resultObj));
            System.out.println("Result: " + resultObj + "\n");

            command = getNextCommand();
        }

        System.out.println("End of scenario");
    }


    public void commandFromPrompt() {
        boolean userExit = false;
        System.out.println("You can enter command in prompt. Do -e to exit, or -h to show some help");

        while (!userExit) {
            Scanner scan = new Scanner(System.in);

            System.out.print("> ");
            String userInput = scan.next();

            if (userInput.contains("-h") || userInput.contains("--help")) {
                System.out.println("-h, --help \t\t Show some help\n" +
                        "-e, --exit \t\t Exit the program\n" +
                        "-f, --file \t\t Send command to server from a file, use like this: -f <relative path> \t\t Ex: -f client/input/commandes.txt");

            } else if (userInput.contains("-e") || userInput.contains("--exit")) {
                userExit = true;

            } else if (userInput.contains("-f") || userInput.contains("--file")) {

            } else {
                Object result = treatCommand(new Command(userInput));
                System.out.println("Result: " + result + "\n");
            }
        }
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
            ApplicationClient applicationClient = new ApplicationClient(hostname, port);
            applicationClient.initialise(commandFilePath, outputFilePath);
            applicationClient.scenario();

            applicationClient.commandFromPrompt();
        }
    }

    public static void printCorrectUsageAndExit() {
        System.out.println("Correct usage : java ApplicationClient.java <hostname> <port> <command file> <output file>");
        System.exit(-1);
    }
}