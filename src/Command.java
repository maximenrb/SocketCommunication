import java.io.Serializable;

/**
 * Title:       Implementation of Command for TP1 (Contain a String)
 * Copyright:   Copyright (c) 2021
 * @author Maxime NARBAUD
 * @version 1.0
 */

public class Command implements Serializable {
    String commandDescription;

    public Command(String commandDescription) {
        this.commandDescription = commandDescription;
    }

    public String getCommandDescription() {
        return commandDescription;
    }

    public void setCommandDescription(String commandDescription) {
        this.commandDescription = commandDescription;
    }
}
