package Controller;

import Model.Base.DBStore;
import Model.Base.DataStore;
import Model.DataTypes.User;
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

    public boolean checkLoginPassword(String login, char[] password) {
        User user = dataStore.getUser(login);
        if (user == null) {
            return false;
        }
        return user.checkPassword(password);
    }
}
