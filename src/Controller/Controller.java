package Controller;

import Model.Base.DBStore;
import Model.Base.DataStore;
import Model.DataTypes.Account;
import Model.DataTypes.Category;
import Model.DataTypes.Record;
import Model.DataTypes.User;
import Model.Tools.HashMaker;
import View.LoginDialog;
import View.MainForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by vasily on 09.06.15.
 */
public class Controller {
    final private static Logger LOGGER = LoggerFactory.getLogger(DataStore.class);
    static SimpleDateFormat simpleDateFormat;
    public User loggedUser = null;
    DataStore dataStore;
    volatile LoginDialog loginDialog = null;
    volatile MainForm mainForm = null;
    private List<Record> mainTable;
    private int selectedAccountIndex = 0;
    private List<Account> accounts;
    private List<Category> categories;

    public Controller() {
        dataStore = new DBStore();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
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

    public List<Record> getMainTable() {
        return mainTable;
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
        accounts = new ArrayList<>();
        for (Account account : loggedUser.getAccounts()) {
            accounts.add(account);
        }
        categories = new ArrayList<>();
        for (Category category : dataStore.getCategories()) {
            categories.add(category);
        }
        mainTable = new ArrayList<>();
        if (accounts.size() > 0) {
            fillTableByAccount(accounts.get(0));
        }
    }

    public void fillTableByAccount(int accountID) {
        fillTableByAccount((Account) dataStore.getDataByID(Account.class, String.valueOf(accountID)));
    }

    public void fillTableByAccount(Account account) {
        mainTable.clear();
        for (Account account1 : loggedUser.getAccounts()) {
            if (account1.equals(account)) {
                for (Record record : account1.getRecords()) {
                    mainTable.add(record);
                }
            }
        }
    }

    public void fillTableByCategory(String categoryName) {
        fillTableByCategory(dataStore.getCategory(categoryName));
    }

    public void fillTableByCategory(Category category) {
        mainTable.clear();
        for (Account account : loggedUser.getAccounts()) {
            for (Record record : account.getRecords()) {
                if (record.getCategory().equals(category)) {
                    mainTable.add(record);
                }
            }
        }
    }
//    public String[][] getTableData(){
//        String[][] result = new String[mainTable.size()][COLUMNS_COUNT];
//        for (int i = 0; i < result.length; i++) {
//            String[] row = result[i];
//            Record record = mainTable.get(i);
//            row[0] = String.valueOf(record.getId());
//            row[1] = simpleDateFormat.format(record.getCreateTime());
//            row[2] = String.valueOf(record.getAmount());
//            row[3] = record.getCategory().getName();
//            row[4] = record.getDescription();
//        }
//        return result;
//    }

    public String[] getAccounts() {
        Set<Account> accounts = loggedUser.getAccounts();
        String[] result = new String[accounts.size()];
        int i = 0;
        for (Account account : loggedUser.getAccounts()) {
            long balance = getBalance(account.getRecords());
            result[i] = String.format("%s   %s", account.getID(), (balance > 0) ? "+" + balance : balance);
            ++i;
        }
        return result;
    }

    public String[] getCategories() {
        Set<Category> categories = dataStore.getCategories();
        String[] result = new String[categories.size()];
        int i = 0;
        for (Category category : categories) {
            result[i] = category.getName();
            ++i;
        }
        return result;
    }

    private long getSpend(Collection<Record> records) {
        long result = 0;
        for (Record record : records) {
            if (record.getAmount() < 0) {
                result += record.getAmount();
            }
        }
        return result;
    }

    private long getIncome(Collection<Record> records) {
        long result = 0;
        for (Record record : records) {
            if (record.getAmount() > 0) {
                result += record.getAmount();
            }
        }
        return result;
    }

    private long getBalance(Collection<Record> records) {
        long result = 0;
        for (Record record : records) {
            result += record.getAmount();
        }
        return result;
    }
}
