package Controller;

import Model.Base.DBStore;
import Model.Base.DataStore;
import Model.DataTypes.Account;
import Model.DataTypes.User;
import Model.Tools.HashMaker;
import View.LoginDialog;
import View.MainForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vasily on 09.06.15.
 */
public class Controller {
    final private static Logger LOGGER = LoggerFactory.getLogger(DataStore.class);
    DataStore dataStore;
    volatile LoginDialog loginDialog = null;
    volatile MainForm mainForm = null;
    User loggedUser = null;

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
            Thread loginDialogThread = new Thread(loginDialog);
            loginDialogThread.start();
        }
    }

    public boolean enter(String login, char[] password) {
        if (userExist(login) && dataStore.getUser(login).checkPassword(password)) {
            loggedIn(login);
            LOGGER.info("Пользователь {} вошел", login);
            if (mainForm == null) {
                mainForm = new MainForm(this);
                Thread mainFormThread = new Thread(mainForm);
                mainFormThread.start();
            }
            return true;
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

    //    public Account[] getAccounts(){
//        Set<Account> accounts = dataStore.getAccounts(loggedUser);
//        return (Account[])accounts.toArray();
//    }
//
//    public Record[] getRecords(Account account){
//        Set<Record> records = dataStore.getRecords(account);
//        return (Record[])records.toArray();
//    }
    public void loggedIn(String userName) {
        loggedUser = dataStore.getUser(userName);
        loggedUser.addAccounts(dataStore.getAccounts(loggedUser));
        for (Account account : loggedUser.getAccounts()) {
            account.addRecords(dataStore.getRecords(account));
        }
    }
}
