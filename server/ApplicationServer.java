import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.HashMap;

public class ApplicationServer {
    private ServerSocket serverSocket;
    private final HashMap<String, Object> objectMap = new HashMap<>();

    /**
     *  > Constructeur
     *  Prend le numéro de port, crée un SocketServer sur le port
     */
    public ApplicationServer(int port) {
        try {
            // 1. Creating a new server socket
            serverSocket = new ServerSocket(port);
            System.out.println(ConsoleUtils.GREEN + "Socket server listening on port " + port + ConsoleUtils.RESET);

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


        while (true) try {
            //System.out.println("Waiting for connections...");
            Socket client = serverSocket.accept();

            //System.out.println("Accepted a connection from: " + client.getInetAddress());

            // 2. Get an InputStream from the client socket
            // 3. Create an ObjectInputStream from the InputStream
            ObjectInput inputStream = new ObjectInputStream(client.getInputStream());

            // 2. Getting an OutputStream from the client socket
            // 3. Creating an ObjectOutputStream from the OutputStream
            ObjectOutput outputStream = new ObjectOutputStream(client.getOutputStream());

            Command command = null;

            // 4. Reading object
            try {
                //System.out.println("Reading object");
                command = (Command) inputStream.readObject();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            // 5. Show objet
            if (command != null) {
                System.out.println(ConsoleUtils.GREEN + "Received command: " + command.getCommandDescription() + ConsoleUtils.RESET);

                // Treating the command and send result to client
                Object result = treatCommand(command);
                System.out.println("Result: " + result);
                outputStream.writeObject(result);
            }

            // 6. Close Stream and client socket
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            client.close();

            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            Class<?> classTest = Class.forName("Etudiant");
//            System.out.println(classTest);
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        String[] argTypes = new String[1];
//        argTypes[0] = "int";
//        //argTypes[1] = "float";
//
//        Object[] argValues = new Object[1];
//        argValues[0] = 12;
//
//        treatCall(this, "testeur1", argTypes, argValues);
//
//        argTypes = new String[0];
//        argValues = new Object[0];
//        treatCall(this, "testeur2", argTypes, argValues);
//
//        argTypes = new String[1];
//        argTypes[0] = "Etudiant";
//
//        argValues = new Object[1];
//        Etudiant etudiant = new Etudiant();
//        etudiant.setNom("Jean Testeur");
//        argValues[0] = etudiant;
//
//        treatCall(this, "testeurStudent", argTypes, argValues);



    }

    /**
     *  Prend une Commande dument formattée, et la traite. Dépendant du type de commande,
     *  elle appelle la méthode spécialisée
     */
    public Object treatCommand(Command command) {

        String[] commandStr = command.getCommandDescription().split("#");

        // TODO Verify commandStr length
        switch (commandStr[0]) {
            case "compilation":
                if (commandStr.length < 2) {
                    return "No source file path found";
                }

                // Split sources file path
                String[] sourcePathArray = commandStr[1].split(",");

                StringBuilder resultStr = new StringBuilder();

                // Call treatCompilation(..) for all source file
                for(String sourcePath : sourcePathArray) {
                    resultStr.append("\n\t").append(sourcePath).append(" => ").append(treatCompilation(sourcePath, commandStr[2]));
                }

                return resultStr.toString();

            case "load":
                treatLoad(commandStr[1]);

                break;

            case "create":
                try {
                    if (commandStr.length == 1) {
                        return "Create command => Missing class name and object id";

                    } else if (commandStr.length == 2) {
                        return "Create command => Missing object id";

                    } else if (commandStr.length == 3) {
                        return treatCreate(ClassUtils.getClassFromString(commandStr[1]), commandStr[2]);

                    } else {
                        return "Create command => Too many arguments";
                    }

                } catch (IllegalArgumentException exception) {
                    return "Create error => " + exception.getMessage();
                }

            case "write":
                // TODO : Verify arg number
                return treatWrite(objectMap.get(commandStr[1]), commandStr[2], commandStr[3]);

            case "read":
                if (commandStr.length == 1) {
                    return "Read command => Missing object id and attribute name ";

                } else if (commandStr.length == 2) {
                    if (objectMap.get(commandStr[1]) == null) {
                        return "Read command => Object instance does not exist and missing attribute name";
                    } else {
                        return "Read command => Missing attribute name";
                    }

                } else if (commandStr.length == 3) {
                    if (objectMap.get(commandStr[1]) == null) {
                        return "Read command => Object instance does not exist";
                    } else {
                        return treatRead(objectMap.get(commandStr[1]), commandStr[2]);
                    }

                } else {
                    return "Read command => Too many arguments";
                }

            case "function":
                // Treat command without argument
                if (commandStr.length == 3) {
                    System.out.println("Treat function command without argument | Command name: " + commandStr[2] + "()");

                    Object objectPtr = objectMap.get(commandStr[1]);
                    return treatCall(objectPtr, commandStr[2], null, null);

                } else if (commandStr.length == 4) {
                    String[] args = commandStr[3].split(",");

                    String[] argTypes = new String[args.length];
                    Object[] argValues = new Object[args.length];

                    for (int i = 0; i < args.length; i++) {
                        String[] tempArray = args[i].split(":");

                        argTypes[i] = tempArray[0];

                        if (tempArray[1].matches("ID\\((.*)\\)")) {
                            String id = tempArray[1].replaceAll("^ID\\(|\\)", "");
                            System.out.println("Regex Match: " + tempArray[1] + " | ID: " + id);

                            argValues[i] = objectMap.get(id);

                        } else {
                            if (argTypes[i].equals("float")) {
                                argValues[i] = Float.parseFloat(tempArray[1]);
                            } else {
                                argValues[i] = tempArray[1];
                            }
                        }
                    }

                    Object objectPtr = objectMap.get(commandStr[1]);
                    return treatCall(objectPtr, commandStr[2], argTypes, argValues);
                }

                break;

            default:
                return "Command not found";
        }

        return null;
    }

    /**
     *  Traite la lecture d’un attribut. Renvoies le résultat par le socket
     */
    public Object treatRead(Object objectPointer, String attribute) {
        try {
            Field field = objectPointer.getClass().getDeclaredField(attribute);
            System.out.println("\tRead field: " + attribute + " | Can access: " + field.canAccess(objectPointer));

            // If true, attribute is public, else attribute is private
            if (field.canAccess(objectPointer)) {
                return field.get(objectPointer);

            } else {
                String methodName = "get" + attribute.substring(0, 1).toUpperCase() + attribute.substring(1);

                Method method = objectPointer.getClass().getMethod(methodName);
                return method.invoke(objectPointer);
            }

        } catch (NoSuchFieldException e) {
            return "Reading error => Attribute not exist: " + e.getMessage();

        } catch (NoSuchMethodException e) {
            return "Reading error => Private attribute and get methode not exist: " + e.getMessage();

        } catch (Exception e) {
            e.printStackTrace();
            return "Reading error: " + e.getMessage();
        }
    }

    /**
     *  Traite l’écriture d’un attribut. Confirmes au client que l’écriture s’est faite correctement.
     */
    public Object treatWrite(Object objectPointer, String attribute, Object value) {
        try {
            Field field = objectPointer.getClass().getDeclaredField(attribute);
            System.out.println("\tWrite field: " + attribute + " | Can access: " + field.canAccess(objectPointer));

            // If true, attribute is public, else attribute is private
            if (field.canAccess(objectPointer)) {
                field.set(objectPointer, value);

            } else {
                String methodName = "set" + attribute.substring(0, 1).toUpperCase() + attribute.substring(1);

                Method method = objectPointer.getClass().getMethod(methodName, value.getClass());
                method.invoke(objectPointer, value);
            }

            return "Successfully writing attribute " + attribute + " with value " + value;

        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return "Write error => Attribute or set method not exist: " + e.getMessage();
        }
    }

    /**
     *  Traite la création d’un objet. Confirme au client que la création s’est faite correctement.
     */
    public Object treatCreate(Class<?> objectClass, String objectId) {
        System.out.println("Creating class " + objectClass);

        try {
            Constructor<?> constructor = objectClass.getConstructor();
            objectMap.put(objectId, constructor.newInstance());

            return "Successfully creating " + objectClass + " with id " + objectId;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return "Create error => Exception with constructor: " + e.getMessage();
        }
    }

    /**
     *  Traite le chargement d’une classe. Confirmes au client que la création s’est faite correctement.
     */
    public void treatLoad(String nomQualifie) {
        System.out.println("\tLoading class " + nomQualifie);
    }

    /**
     *  Traite la compilation d’un fichier source java. Confirme au client que
     *  la compilation s’est faite correctement. Le fichier source est donné par son chemin
     *  relatif par rapport au chemin des fichiers sources.
     */
    public int treatCompilation(String sourceFilePath, String outDirectoryPath) {
        String absoluteSourcePath = Path.of(sourceFilePath).toFile().getAbsolutePath();
        String absoluteOutPath = Path.of(outDirectoryPath).toFile().getAbsolutePath();

        // System.out.println("\tCompiling " + absoluteSourcePath + " in " + absoluteOutPath);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        return compiler.run(null, null, null, absoluteSourcePath, "-d", absoluteOutPath);
    }

    /**
     *  Traite l’appel d’une méthode, en prenant comme argument l’objet sur lequel on effectue l'appel,
     *  le nom de la fonction à appeler, un tableau de nom de types des arguments, et un tableau d'arguments
     *  pour la fonction. Le résultat de la fonction est renvoyé par le serveur au client (ou le message que tout s’est bien passé)
     */
    public Object treatCall(Object objectPointer, String functionName, String[] types, Object[] argValues) {
        Class<?>[] classArray;

        try {
             classArray = ClassUtils.getClassArray(types);

            try {
                Method method = objectPointer.getClass().getMethod(functionName, classArray);
                return "Call success => Function return: " + method.invoke(objectPointer, argValues);

            } catch (Exception e1) {
                e1.printStackTrace();
                return "Call error => Method reflection exception: " + e1.getMessage();
            }

        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
            return exception.getMessage();
        }
    }

    /**
     *  > Programme principal.
     *  Prend 4 arguments: 1) numéro de port, 2) répertoire source, 3) répertoire classes,
     *  et 4) nom du fichier de traces (sortie). Cette méthode doit créer une instance de
     *  la classe ApplicationServeur, l’initialiser puis appeler aVosOrdres sur cet objet
     */
    public static void main(String[] args) {
        // Variables declaration
        int port = -1;
        String sourceDirectory, classesDirectory, logFilePath;

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