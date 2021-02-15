import com.sun.tools.javac.Main;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Title:       Application Server class for TP1
 * Copyright:   Copyright (c) 2021
 * @author Maxime NARBAUD
 * @version 1.0
 */
public class ApplicationServer {
    private ServerSocket serverSocket;
    private String outputFilePath;
    private final HashMap<String, Object> objectMap = new HashMap<>();
    private final HashMap<String, Class<?>> loadedClassMap = new HashMap<>();

    /**
     *  ApplicationServer constructor: creates a SocketServer on the indicated port
     *
     *  @param port server port
     *  @param outputFilePath log file relative path
     */
    public ApplicationServer(int port, String outputFilePath) {
        this.outputFilePath = outputFilePath;

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
     *	Wait connection from clients. Following a connection, it reads what is sent through the Socket,
     *	recreates the Command object sent by the client, and will call treatCommand(Command command)
     */
    public void waitClient() {
        while (true) try {
            Socket client = serverSocket.accept();

            // 2. Get an InputStream from the client socket
            // 3. Create an ObjectInputStream from the InputStream
            ObjectInput inputStream = new ObjectInputStream(client.getInputStream());

            // 2. Get an OutputStream from the client socket
            // 3. Create an ObjectOutputStream from the OutputStream
            ObjectOutput outputStream = new ObjectOutputStream(client.getOutputStream());

            Command command = null;

            // 4. Reading object
            try {
                command = (Command) inputStream.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            // 5. Show objet
            if (command != null) {
                System.out.println(ConsoleUtils.GREEN + "Received command: " + command.getCommandDescription() + ConsoleUtils.RESET);
                ClassUtils.writeResultInFile("Command: " + command.getCommandDescription(), outputFilePath);

                // Treating the command and send result to client
                Object result = treatCommand(command);
                System.out.println("Result: " + result);
                ClassUtils.writeResultInFile(">> " + result, outputFilePath);
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
    }

    /**
     *  Takes a Command, and process it. Depending on the Command type, it calls the specified method
     *
     *  @param command takes a Command send by the client
     *  @return the result of the executed Command
     */
    public Object treatCommand(Command command) {
        String[] commandStr = command.getCommandDescription().split("#");

        switch (commandStr[0]) {
            case "compilation":
                if (commandStr.length < 2) {
                    return "No source file path found";
                }

                // Split sources file path
                List<String> sourcePathList = new ArrayList<>(Arrays.asList(commandStr[1].split(",")));

                // Call treatCompilation(..) for all source file
                return treatCompilation(sourcePathList, commandStr[2]);

            case "load":
                if (commandStr.length < 3) {
                    return "Load Command => Missing argument(s)";

                } else {
                    return treatLoad(commandStr[1], commandStr[2]);
                }

            case "create":
                try {
                    if (commandStr.length == 1) {
                        return "Create command => Missing class name and object id";

                    } else if (commandStr.length == 2) {
                        return "Create command => Missing object id";

                    } else if (commandStr.length == 3) {
                        if (loadedClassMap.containsKey(commandStr[1])) {
                            return treatCreate(loadedClassMap.get(commandStr[1]), commandStr[2]);
                        }

                    } else {
                        return "Create command => Too many arguments";
                    }

                } catch (IllegalArgumentException exception) {
                    return "Create error => " + exception.getMessage();
                }

            case "write":
                if (commandStr.length < 4) {
                    return "Write Command => Missing argument(s)";

                } else {
                    return treatWrite(objectMap.get(commandStr[1]), commandStr[2], commandStr[3]);
                }

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
                // Treats command without argument
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
     *  Treats the compilation of a java source file. Confirms to the client that the compilation was done correctly.
     *  The source file is given by its relative path.
     *
     *  @param sourceFilesList list of source files to compile
     *  @param outDirectoryPath path of the output directory
     *  @return "0" if the compilation is correctly done, else returns another number
     */
    public String treatCompilation(List<String> sourceFilesList, String outDirectoryPath) {
        // Add output arguments and directory in arguments list
        sourceFilesList.add("-d");
        sourceFilesList.add(Path.of(outDirectoryPath).toFile().getAbsolutePath());

        // Convert List to Array
        String[] compilationArgs = sourceFilesList.toArray(new String[0]);

        // Compile files and return compilation code
        return "Compilation result: " + ToolProvider.getSystemJavaCompiler().run(null, null, null, compilationArgs);
    }

    /**
     *  Treats the loading of a class. Confirms to the client that the creation was done correctly.
     *
     *  @param classPackage qualified class name (ex: classes.Student)
     *  @param outClassesDirectory relative path of the output directory for .class files
     *  @return "Successfully loading..." if class loading is correctly done, else return an error message
     */
    public String treatLoad(String classPackage, String outClassesDirectory) {
        // Giving the path of the class directory where class file is generated..
        File classesDir = new File(outClassesDirectory).getAbsoluteFile();

        // Load and instantiate compiled class.
        URLClassLoader classLoader;

        try {
            // Loading the class
            classLoader = URLClassLoader.newInstance(new URL[]{classesDir.toURI().toURL()});
            loadedClassMap.put(classPackage, Class.forName(classPackage, true, classLoader));

            return "Successfully loading " + classPackage;

        } catch (Exception e) {
            e.printStackTrace();
            return "Loading error => Exception: " + e.getMessage();
        }
    }

    /**
     *  Treats the creation of an object. Confirms to the customer that the creation was done correctly.
     *
     *  @param objectClass the class for the object that you want to instantiate
     *  @param objectId the id for the object that you want to create
     *  @return "Successfully creating..." if creation is correctly done, else returns an error message
     */
    public Object treatCreate(Class<?> objectClass, String objectId) {
        System.out.println("Creating class " + objectClass);

        try {
            Constructor<?> constructor = objectClass.getConstructor();
            objectMap.put(objectId, constructor.newInstance());

            return "Successfully creating " + objectClass + " with id " + objectId;

        } catch (Exception exception) {
            exception.printStackTrace();
            return "Create error => Exception with constructor: " + exception.getMessage();
        }
    }

    /**
     *  Treats reading of an attribute. Return the result by the socket
     *
     *  @param objectPointer pointer on the object that you want you to read the attribute
     *  @param attribute the attribute name that you want to read
     *  @return the value of the attribute, else returns an error message
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
     *  Treats writing of an attribute. Confirms to the customer that the entry was done correctly
     *
     *  @param objectPointer pointer on the object that you want you to write the attribute
     *  @param attribute the attribute name that you want to write
     *  @param value the value that you want to write
     *  @return "Successfully writing..." if the entry is correctly done, else returns an error message
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

        } catch (Exception exception) {
            exception.printStackTrace();
            return "Write error => Attribute or set method not exist: " + exception.getMessage();
        }
    }

    /**
     *  Treats the call of a method. Confirms to the client if the call was correctly done or not.
     *
     *  @param objectPointer the pointer on the object on which the call is made
     *  @param functionName the name of the function to call
     *  @param types an array of arguments types names
     *  @param argValues an array of arguments for the function (corresponding the types array)
     *  @return the return of the called function, else returns an error message if the function not exist
     */
    public Object treatCall(Object objectPointer, String functionName, String[] types, Object[] argValues) {
        // Giving the path of the class directory where class file is generated..
        File classesDir = new File("server/out/").getAbsoluteFile();

        // Load and instantiate compiled class.
        URLClassLoader classLoader= null;

        List<Class<?>> classList = new ArrayList<>();

        try {
            classLoader = URLClassLoader.newInstance(new URL[]{classesDir.toURI().toURL()});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Class<?>[] classArray = null;

        if (types != null) {
            for (String type : types) {
                if (ClassUtils.typeMap.containsKey(type)) {
                    classList.add(ClassUtils.typeMap.get(type));

                } else {
                    try {
                        classList.add(Class.forName(type, true, classLoader));

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            classArray = classList.toArray(new Class<?>[0]);
        }


        try {
            try {
                Method method = objectPointer.getClass().getDeclaredMethod(functionName, classArray);
                return "Call success => Function return: " + method.invoke(objectPointer, argValues);

            } catch (Exception e1) {
                e1.printStackTrace();
                return "Call error => Method reflection exception: " + e1.getMessage();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return exception.getMessage();
        }
    }

    /**
     *  Main Program
     *  Take 4 args:    1. Server port
     *                  2. General sources repertory
     *                  3. Classes repertory
     *                  4. Output file path
     *  This method creates an instance of ApplicationServer class, initialize it and then wait clients
     *
     *  @param args arguments of the main program
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

            // Print user arguments
            System.out.println("Port: " + port + " || Source directory: " + sourceDirectory + " || Classes directory: "
                    + classesDirectory + " || Log file: " + args[3]);
        }

        if(error) {
            printCorrectUsageAndExit();

        } else {
            // Create ApplicationServer instance
            new ApplicationServer(port, args[3]);
        }
    }

    /**
     *  Prints correct program arguments usage
     */
    public static void printCorrectUsageAndExit() {
        System.out.println("Correct usage : java ApplicationServer.java <port> <source directory> <classes directory> <log file>");
        System.exit(-1);
    }
}