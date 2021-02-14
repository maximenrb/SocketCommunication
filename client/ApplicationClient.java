import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Title:       Application Client class for TP1
 * Copyright:   Copyright (c) 2021
 * @author Maxime NARBAUD
 * @version 1.0
 */

public class ApplicationClient {
    BufferedReader commandBuffer = null;
    String hostname, outFilePath;
    int port;

    /**
     *  ApplicationClient Constructor: defines the hostname and the port for the client socket
     *
     *  @param hostname server hostname
     *  @param port server port
     */
    public ApplicationClient(String hostname, int port) {
        System.out.println("Create ApplicationClient instance");

        this.hostname = hostname;
        this.port = port;
    }

    /**
     *  Takes the file containing the list of commands, and loads it into a variable of type Command which is returned
     *
     *  @return the Command read in the next file line, else returns null
     */
    public Command getNextCommand() {
        if (commandBuffer != null) {
            try {
                String currentCommand;

                // Get the next command until the file is finished
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
     *  Opens command and output files
     *
     *  @param commandFile command file path
     *  @param outputFile output file path
     */
    public void initialise(String commandFile, String outputFile) {
        outFilePath = outputFile;
        
        try {
            commandBuffer = new BufferedReader(new FileReader(commandFile));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Command file not found");
            System.exit(-1);
        }
    }

    /**
     *  Writes result in the output file, creates a new file if not exist, else adds at the end
     *
     *  @param result the result of the executed Command that you want to write in output file
     */
    public void writeResultInFile(String result) {
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
     *  Takes a Command, and execute it on the server. The result of the execution is returned.
     *  If the command does not return a result, we return null. Each call must open a connection,
     *  execute, and close the connection.
     *
     *  @param command takes the Command read in the file
     *  @return the result of the executed Command, else returns null
     */
    public Object treatCommand(Command command) {
        try {
            // 1. Creating a new socket
            // System.out.println("Opening socket at " + hostname + ":" + port);
            Socket socket = new Socket(hostname, port);

            // 2. Get an OutputStream from the socket
            // 3. Create an ObjectOutputStream from the OutputStream
            ObjectOutput outputStream = new ObjectOutputStream(socket.getOutputStream());

            // 2. Get an InputStream from the socket server
            // 3. Create an ObjectInputStream from the InputStream
            ObjectInput inputStream = new ObjectInputStream(socket.getInputStream());

            Object result = null;

            // 4. Writing object
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
     *  It contains the sequence of steps to be performed for the test.
     *  It will make successive calls to getNextCommand() and treatCommand(Command command).
     */
    public void scenario() {
        // Get the first command on the file
        Command command = getNextCommand();

        while (command != null) {
            Object resultObj = treatCommand(command);
            writeResultInFile(String.valueOf(resultObj));
            System.out.println("Result: " + resultObj + "\n");

            command = getNextCommand();
        }
    }


    /**
     *  Allows user to enter commands from prompt after file execution
     */
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
     *  Main program
     *  Take 4 args:    1. Server hostname
     *                  2. Server port
     *                  3. Command file path
     *                  4. Output file path
     *  This method create an instance of the ApplicationClient class, initialize it, then apply the scenario
     *
     *  @param args arguments of the main program
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

    /**
     *  Prints correct program arguments usage
     */
    public static void printCorrectUsageAndExit() {
        System.out.println("Correct usage : java ApplicationClient.java <hostname> <port> <command file> <output file>");
        System.exit(-1);
    }
}