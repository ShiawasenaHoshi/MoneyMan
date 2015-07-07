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
    private SortParams lastSortParams;
    private volatile long minAmount = 0;
    private volatile long maxAmount = 0;

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
            fillTableBy(new SortParams().setAccountIndex(0));
        }
        calculateMinMaxAmounts();
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

    public void fillTableBy() {
        fillTableBy(lastSortParams);
    }

    public void fillTableBy(SortParams sortParams) {
        //fixme нужна проверка на существовании категории и аккаунта, что в params
        lastSortParams = sortParams;
        mainTable.clear();
        if (sortParams.getAccountIndex() >= 0) {
            mainTable.addAll(dataStore.getRecords(accounts.get(sortParams.getAccountIndex())));
        } else {
            for (Account account : dataStore.getAccounts(loggedUser)) {
                for (Record record : dataStore.getRecords(account)) {
                    mainTable.add(record);
                }
            }
        }
        if (sortParams.getCategoryIndex() >= 0) {
            for (int i = mainTable.size() - 1; i >= 0; i--) {
                if (!mainTable.get(i).getCategory().equals(categories.get(sortParams.getCategoryIndex()))) {
                    mainTable.remove(i);
                }
            }
        }
        if (sortParams.isAmountRestricted()) {
            for (int i = mainTable.size() - 1; i >= 0; i--) {
                if (mainTable.get(i).getAmount() < sortParams.getAmountFrom() || mainTable.get(i).getAmount() > sortParams.getAmountTo()) {
                    mainTable.remove(i);
                }
            }
        }
        if (sortParams.isDateTimeRestricted()) {
            for (int i = mainTable.size() - 1; i >= 0; i--) {
                if (mainTable.get(i).getCreateTime() < sortParams.getDateTimeFrom() && mainTable.get(i).getAmount() > sortParams.getDateTimeTo()) {
                    mainTable.remove(i);
                }
            }
        }
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
        for (Account account : dataStore.getAccounts(loggedUser)) {
            for (Record record : dataStore.getRecords(account)) {
                if (record.getId() == id) {
                    if (dataStore.removeRecord(account, record) != null) {
                        calculateMinMaxAmountsAfterChanges(record);
                        return;
                    } else {
                        try {
                            throw new Exception("Запись не удалена");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void saveNewRecord(int id, long amount, String description, int categoryIndex, long createTime, int accountIndex) {
        Record record = new Record(id, amount, description, categories.get(categoryIndex), createTime);
        dataStore.addRecord(accounts.get(accountIndex), record);
        calculateMinMaxAmountsAfterChanges(record);
        fillTableBy(lastSortParams);
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
            calculateMinMaxAmountsAfterChanges(record);
            fillTableBy(lastSortParams);
        }
    }

    public boolean removeCategory(int categoryIndex) {
        //todo не забудь, что удалить NO_CATEGORY нельзя
        return false;
    }

    public void calculateMinMaxAmountsAfterChanges(Record editedRecord) {
        if (editedRecord.getAmount() >= maxAmount || editedRecord.getAmount() <= minAmount) {
            calculateMinMaxAmounts();
            System.out.println("Итого:" + minAmount + " " + maxAmount);
        }
    }

    public void calculateMinMaxAmounts() {
        List<Record> allRecords = new ArrayList<>();
        for (Account account : dataStore.getAccounts(loggedUser)) {
            for (Record record : dataStore.getRecords(account)) {
                allRecords.add(record);
            }
        }
        calculateMinMaxAmounts(allRecords);
    }

    public void calculateMinMaxAmounts(Collection<? extends Record> allRecords) {
        for (Record record : allRecords) {
            if (record.getAmount() < minAmount) {
                minAmount = record.getAmount();
                continue;
            } else if (record.getAmount() > maxAmount) {
                maxAmount = record.getAmount();
            }
        }
    }

    public long getMaxAmount() {
        return maxAmount;
    }

    public long getMinAmount() {
        return minAmount;
    }
}
