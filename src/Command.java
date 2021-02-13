import java.io.Serializable;

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
