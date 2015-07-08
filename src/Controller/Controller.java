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
import java.util.*;

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
    private SelectParams lastSelectParams;
    private long minAmount = 0;
    private long maxAmount = 0;
    private long minDateTime = 0;
    private long maxDateTime = 0;
    private Comparator<Record> sorter;

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
        sorter = new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                if (o1.getCreateTime() < o2.getCreateTime()) {
                    return -1;
                }
                if (o1.getCreateTime() > o2.getCreateTime()) {
                    return 1;
                }
                return 0;
            }
        };
        loggedUser = dataStore.getUser(userName);

        accounts = new ArrayList<>();
        updateAccountsList();
        categories = new ArrayList<>();
        updateCategoriesList();
        mainTable = new ArrayList<>();
        if (accounts.size() > 0) {
            fillTableBy(new SelectParams().setAccountIndex(0));
        }
        calculateMinMax();
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
        fillTableBy(lastSelectParams);
    }

    public void fillTableBy(SelectParams selectParams) {
        //fixme нужна проверка на существовании категории и аккаунта, что в params
        lastSelectParams = selectParams;
        mainTable.clear();
        if (selectParams.getAccountIndex() >= 0) {
            mainTable.addAll(dataStore.getRecords(accounts.get(selectParams.getAccountIndex())));
        } else {
            for (Account account : dataStore.getAccounts(loggedUser)) {
                for (Record record : dataStore.getRecords(account)) {
                    mainTable.add(record);
                }
            }
        }
        if (selectParams.getCategoryIndex() >= 0) {
            for (int i = mainTable.size() - 1; i >= 0; i--) {
                if (!mainTable.get(i).getCategory().equals(categories.get(selectParams.getCategoryIndex()))) {
                    mainTable.remove(i);
                }
            }
        }
        if (selectParams.isAmountRestricted()) {
            for (int i = mainTable.size() - 1; i >= 0; i--) {
                if (mainTable.get(i).getAmount() < selectParams.getAmountFrom() || mainTable.get(i).getAmount() > selectParams.getAmountTo()) {
                    mainTable.remove(i);
                }
            }
        }
        if (selectParams.isDateTimeRestricted()) {
            for (int i = mainTable.size() - 1; i >= 0; i--) {
                if (mainTable.get(i).getCreateTime() < selectParams.getDateTimeFrom() || mainTable.get(i).getAmount() > selectParams.getDateTimeTo()) {
                    mainTable.remove(i);
                }
            }
        }
        sortBy();
    }

    public void sortBy() {
        sortBy(mainTable, sorter);
    }

    public void sortBy(List<Record> records, Comparator<Record> sorter) {
        this.sorter = sorter;
        Collections.sort(records, sorter);
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
        fillTableBy(lastSelectParams);
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
            fillTableBy(lastSelectParams);
        }
    }

    public boolean removeCategory(int categoryIndex) {
        //todo не забудь, что удалить NO_CATEGORY нельзя
        return false;
    }

    public void calculateMinMaxAmountsAfterChanges(Record editedRecord) {
        if (editedRecord.getAmount() >= maxAmount || editedRecord.getAmount() <= minAmount) {
            calculateMinMax();
            System.out.println("Итого:" + minAmount + " " + maxAmount);
        }
    }

    public void calculateMinMax() {
        List<Record> allRecords = new ArrayList<>();
        for (Account account : dataStore.getAccounts(loggedUser)) {
            for (Record record : dataStore.getRecords(account)) {
                allRecords.add(record);
            }
        }
        calculateMinMax(allRecords);
    }

    public void calculateMinMax(Collection<? extends Record> allRecords) {
        minDateTime = 0;
        for (Record record : allRecords) {
            if (record.getAmount() < minAmount) {
                minAmount = record.getAmount();
            } else if (record.getAmount() > maxAmount) {
                maxAmount = record.getAmount();
            }
            if (minDateTime == 0) {
                minDateTime = record.getCreateTime();
            }
            if (record.getCreateTime() < minDateTime) {
                minDateTime = record.getCreateTime();
            } else if (record.getCreateTime() > maxDateTime) {
                maxDateTime = record.getCreateTime();
            }
        }
    }

    public long getMaxAmount() {
        return maxAmount;
    }

    public long getMinAmount() {
        return minAmount;
    }

    public long getMaxDateTime() {
        return maxDateTime;
    }

    public long getMinDateTime() {
        return minDateTime;
    }
}
