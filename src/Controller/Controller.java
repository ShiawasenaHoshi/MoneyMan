package Controller;

import Model.Base.DBStore;
import Model.Base.DataStore;
import Model.DataTypes.User;
import Model.Tools.HashMaker;
import View.LoginDialog;

/**
 * Created by vasily on 09.06.15.
 */
public class Controller {
    DataStore dataStore;
    volatile LoginDialog loginDialog = null;

    public Controller() {
        dataStore = new DBStore();
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.start();
    }

    void start() {
        if (loginDialog == null) {
            loginDialog = new LoginDialog(this);
            loginDialog.run();
        }
    }

    public boolean enter(String login, char[] password) {
        if (userExist(login)) {
            return dataStore.getUser(login).checkPassword(password);
        } else return false;
    }

    public boolean userExist(String login) {
        User user = dataStore.getUser(login);
        return user != null;
    }

    public void createNewUser(String login, char[] password) {
        User newUser = new User(login, HashMaker.getHash(password));
        dataStore.addUser(newUser);
    }
}
