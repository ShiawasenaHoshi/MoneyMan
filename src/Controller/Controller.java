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

    public void loggedIn(String userName) {
        loggedUser = dataStore.getUser(userName);
//        loggedUser.addAccounts(dataStore.getAccounts(loggedUser));
//        for (Account account : loggedUser.getAccounts()) {
//            account.addRecords(dataStore.getRecords(account));
//        }
        accounts = new ArrayList<>();
        for (Account account : dataStore.getAccounts(loggedUser)) {
            accounts.add(account);
        }
        categories = new ArrayList<>();
        for (Category category : dataStore.getCategories()) {
            categories.add(category);
        }
        mainTable = new ArrayList<>();
        if (accounts.size() > 0) {
            fillTableByAccount(0);
        }
    }

    public void fillTableByAccount(int accountIndex) {
        mainTable.clear();
        Account account = accounts.get(accountIndex);
        mainTable.addAll(dataStore.getRecords(account));
    }

    public void fillTableByCategory(int categoryIndex) {
        mainTable.clear();
        Category category = categories.get(categoryIndex);
        for (Account account : accounts) {
            for (Record record : dataStore.getRecords(account)) {
                if (record.getCategory().equals(category)) {
                    mainTable.add(record);
                }
            }
        }
    }

    public String[] getAccounts() {
        String[] result = new String[accounts.size()];
        int i = 0;
        for (Account account : accounts) {
            long balance = getBalance(dataStore.getRecords(account));
            result[i] = String.format("%s   %s", account.getID(), (balance > 0) ? "+" + balance : balance);
            ++i;
        }
        return result;
    }

    public String[] getCategories() {
        String[] result = new String[categories.size()];
        int i = 0;
        for (Category category : categories) {
            result[i] = category.getName();
            ++i;
        }
        return result;
    }

    private void updateAccountsList() {
        accounts.clear();
        accounts.addAll(dataStore.getAccounts(loggedUser));
    }

    private void updateCategoriesList() {
        categories.clear();
        categories.addAll(dataStore.getCategories());
    }

    public long getSpend() {
        return getSpend(mainTable);
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

    public long getIncome() {
        return getIncome(mainTable);
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

    public long getBalance() {
        return getBalance(mainTable);
    }

    private long getBalance(Collection<Record> records) {
        long result = 0;
        for (Record record : records) {
            result += record.getAmount();
        }
        return result;
    }

    public void removeRecord(int id) {
        for (Account account : loggedUser.getAccounts()) {
            for (Record record : account.getRecords()) {
                if (record.getId() == id) {
                    account.getRecords().remove(record);
                    return;
                }
            }
        }
    }

//    public void addRecord(int accountID, Record record) {
//
//    }

    public void saveNewRecord(int id, long amount, String description, int categoryIndex, long createTime, int accountIndex) {
        Record record = new Record(id, amount, description, categories.get(categoryIndex), createTime);
        dataStore.addRecord(accounts.get(accountIndex), record);
    }

    public void saveEditedRecord(int id, long amount, String description, int categoryIndex, long createTime) {
        Account accountTo = null;
        Record record = new Record(id, amount, description, categories.get(categoryIndex), createTime);
        if (record.getId() == Record.NO_ID) {
            throw new NullPointerException("У сохраняемой записи нет идентификатора и она не привязана к счету");
        } else {
            for (Account account : dataStore.getAccounts(loggedUser)) {
                for (Record existingRecord : account.getRecords()) {
                    if (existingRecord.getId() == record.getId()) {
                        accountTo = account;
                        break;
                    }
                }
                if (accountTo != null) {
                    break;
                }
            }
        }
        if (accountTo == null) {
            throw new NullPointerException("Нет записи с таким ID в базе");
        } else {
            dataStore.addRecord(accountTo, record);
        }
    }

    public boolean removeCategory(int categoryIndex) {
        //todo не забудь, что удалить NO_CATEGORY нельзя
        return false;
    }

    public long getMaxAmount() {
        return 0l;
    }

    public long getMinAmount() {
        return 0l;
    }
}
