package Controller;

import Model.Base.DBStore;
import Model.Base.DataStore;
import Model.DataTypes.*;
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

    public String getAccount(int index) {
        return accounts.get(index).getDescription();
    }

    public String[] getAccounts() {
        String[] result = new String[accounts.size()];
        int i = 0;
        for (Account account : accounts) {
            long balance = getBalance(dataStore.getRecords(account));
            result[i] = String.format("%s   %s", account.getDescription(), (balance > 0) ? "+" + balance : balance);
            ++i;
        }
        return result;
    }

    public String[] getCategory(int index){
        String[] categoryInfo = new String[2];
        Category category = categories.get(index);
        categoryInfo[0] = category.getName();
        categoryInfo[1] = category.getDescription();
        return categoryInfo;
    }

    public String[] getCategories() {
        String[] result = new String[categories.size()];
        int i = 0;
        for (Category category : categories) {
            result[i] = category.getDescription();
            ++i;
        }
        return result;
    }

    public void fillTableBy() {
        fillTableBy(lastSelectParams);
    }

    public void fillTableBy(SelectParams selectParams) {
        lastSelectParams = selectParams;
        if (selectParams == null) {
            selectParams = new SelectParams().setAccountIndex(0);
        }
        mainTable.clear();
        if (selectParams.getAccountIndex() >= 0) {
            if (selectParams.getAccountIndex() < accounts.size()) {
                mainTable.addAll(dataStore.getRecords(accounts.get(selectParams.getAccountIndex())));
            } else {
                Set<Account> accounts = dataStore.getAccounts(loggedUser);
                if (accounts.size() == 0) {
                    return;
                }
                for (Account account : accounts) {
                    for (Record record : dataStore.getRecords(account)) {
                        mainTable.add(record);
                    }
                }
            }
        }
        if (selectParams.getCategoryIndex() >= 0) {
            if (selectParams.getCategoryIndex() < categories.size()) {
                for (int i = mainTable.size() - 1; i >= 0; i--) {
                    if (!mainTable.get(i).getCategory().equals(categories.get(selectParams.getCategoryIndex()))) {
                        mainTable.remove(i);
                    }
                }
            } else {
                for (int i = mainTable.size() - 1; i >= 0; i--) {
                    if (!mainTable.get(i).getCategory().equals(categories.get(0))) {
                        mainTable.remove(i);
                    }
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

    private void updateAccountsList() {
        accounts.clear();
        accounts.addAll(dataStore.getAccounts(loggedUser));
    }

    private void updateCategoriesList() {
        categories.clear();
        categories.addAll(dataStore.getCategories());
        //todo добавить сортировку по алфавиту
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

    public void addAccount(String description) throws Exception {
        Account account = dataStore.addAccount(loggedUser, new Account(DataType.NO_ID, description));
        if (account == null) {
            throw new Exception("Аккаунт не создан");
        }
        updateAccountsList();
    }

    public void editAccount(int accountIndex, String description) throws Exception {
        if (accountIndex >= 0 && accountIndex < accounts.size()) {
            Account account = dataStore.addAccount(loggedUser, new Account(accounts.get(accountIndex).getID(), description));
            if (account == null) {
                throw new Exception("Аккаунт не создан");
            }
            updateAccountsList();
        }
    }

    public void removeAccount(int accountIndex) throws Exception {
        if (accountIndex >= 0 && accountIndex < accounts.size()) {
            Account account = dataStore.removeAccount(loggedUser, accounts.get(accountIndex));
            if (account == null) {
                throw new Exception("Аккаунт не удален");
            }
            Set<Record> records = dataStore.getRecords(account);
            for (Record record : records) {
                dataStore.removeRecord(account, record);
            }
            updateAccountsList();
        }
    }

    public void addCategory(String name, String description) throws Exception {
        Category categoryToAdd = new Category(name, description);
        if (dataStore.addCategory(categoryToAdd) == null) {
            throw new Exception("Категория не добавлена");
        }
        updateCategoriesList();
    }

    public void editCategory(int categoryIndex, String description) throws Exception {
        if (categories.size() == 0 || categoryIndex >= categories.size()) {
            return;
        }
        //fixme
        Category categoryToEdit = categories.get(categoryIndex);
        if (categoryToEdit.getName().equals(Category.NO_CATEGORY)) {
            throw new Exception("Редактировать NO_CATEGORY нельзя");
        }
        dataStore.addCategory(categoryToEdit);
        updateCategoriesList();
        fillTableBy();
    }

    public void removeCategory(int categoryIndex) throws Exception {
        if (categories.size() == 0 || categoryIndex >= categories.size()) {
            return;
        }
        Category categoryToRemove = categories.get(categoryIndex);
        if (categoryToRemove.getName().equals(Category.NO_CATEGORY)) {
            throw new Exception("Удалить NO_CATEGORY нельзя");
        }
        dataStore.removeCategory(categoryToRemove);
        updateCategoriesList();
        fillTableBy();
    }

    public void calculateMinMaxAmountsAfterChanges(Record editedRecord) {
        if (editedRecord.getAmount() >= maxAmount || editedRecord.getAmount() <= minAmount) {
            calculateMinMax();
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
