package Controller;

import Model.Base.DBStore;
import Model.Base.DataStore;
import Model.DataTypes.*;
import Model.Tools.HashMaker;
import Model.Tools.Transliterator;
import View.LoginDialog;
import View.MainForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.OperationNotSupportedException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller {
    public static final String NO_CATEGORY_DESCRIPTION = "Без категории";
    final private static Logger LOGGER = LoggerFactory.getLogger(DataStore.class);
    static SimpleDateFormat simpleDateFormat;
    public User loggedUser = null;
    public CurrentRecord currentRecord;
    DataStore dataStore;
    volatile LoginDialog loginDialog = null;
    volatile MainForm mainForm = null;
    Locale locale = null;
    Collator currentCollator = null;
    private List<Record> mainTable;
    private List<Account> accounts;
    private List<Category> categories;
    private SelectParams lastSelectParams;
    private long minAmount = 0;
    private long maxAmount = 0;
    private long minDateTime = 0;
    private long maxDateTime = 0;
    private Comparator<Record> timeComparator;

    {
        locale = new Locale("ru_RU");
        currentCollator = Collator.getInstance(locale);
        currentCollator.setStrength(Collator.PRIMARY);
        timeComparator = (o1, o2) -> {
            if (o1.getDateTime() < o2.getDateTime()) {
                return -1;
            }
            if (o1.getDateTime() > o2.getDateTime()) {
                return 1;
            }
            return 0;
        };
    }

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
        currentRecord = new CurrentRecord();
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

    public String getCategory(int index) throws OperationNotSupportedException {
        Category result = categories.get(index);
        if (result.getName().equals(Category.NO_CATEGORY)) {
            throw new OperationNotSupportedException();
        }
        return categories.get(index).getDescription();
    }

    public String[] getCategories() {
        String[] result = new String[categories.size()];
        int i = 0;
        for (Category category : categories) {
            result[i] = category.getName().equals(Category.NO_CATEGORY) ? NO_CATEGORY_DESCRIPTION : category.getDescription();
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
            }
        } else {
            Set<Account> accounts = dataStore.getAccounts(loggedUser);
            if (accounts.size() == 0) {
                return;
            }
            for (Account account : accounts) {
                mainTable.addAll(dataStore.getRecords(account));
            }
        }
        if (selectParams.getCategoryIndex() >= 0) {
            if (selectParams.getCategoryIndex() < categories.size()) {
                for (int i = mainTable.size() - 1; i >= 0; i--) {
                    if (!mainTable.get(i).getCategory().getName().equals(categories.get(selectParams.getCategoryIndex()).getName())) {
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
                if (mainTable.get(i).getDateTime() < selectParams.getDateTimeFrom() || mainTable.get(i).getAmount() > selectParams.getDateTimeTo()) {
                    mainTable.remove(i);
                }
            }
        }
        sortBy();
    }

    public void sortBy() {
        sortBy(mainTable, timeComparator);
    }

    public void sortBy(List<Record> records, Comparator<Record> sorter) {
        this.timeComparator = sorter;
        Collections.sort(records, sorter);
    }

    private void updateAccountsList() {
        accounts.clear();
        accounts.addAll(dataStore.getAccounts(loggedUser));
    }

    private void updateCategoriesList() {
        categories.clear();
        categories.addAll(dataStore.getCategories());
        Collections.sort(categories, (o1, o2) -> currentCollator.compare(o1.getDescription(), o2.getDescription()));
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

    public void saveEditedRecord(int id, long amount, String description, int categoryIndex, long createTime) throws Exception {
        Account accountTo = null;
        Record record = new Record(id, amount, description, categories.get(categoryIndex), createTime);
        if (record.getId() == Record.NO_ID) {
            throw new Exception("У сохраняемой записи нет идентификатора и она не привязана к счету");
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
            throw new Exception("Нет записи с таким ID в базе");
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

    public void addCategory(String description) throws Exception {
        Category categoryToAdd = new Category(Transliterator.transliterate(description), description);
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
        Category categoryToEdit = new Category(categories.get(categoryIndex).getName(), description);
        if (categoryToEdit.getName().equalsIgnoreCase(Category.NO_CATEGORY)) {
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
        if (categoryToRemove.getName().equalsIgnoreCase(Category.NO_CATEGORY)) {
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
            allRecords.addAll(dataStore.getRecords(account));
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
                minDateTime = record.getDateTime();
            }
            if (record.getDateTime() < minDateTime) {
                minDateTime = record.getDateTime();
            } else if (record.getDateTime() > maxDateTime) {
                maxDateTime = record.getDateTime();
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

    public class CurrentRecord {
        public static final int NO_ID = Record.NO_ID;
        Record currentRecord;

        public void setCurrentRecord(int index) {
            if (mainTable.size() > 0 && index >= 0 && index < mainTable.size()) {
                currentRecord = mainTable.get(index);
            }
        }

        public int getID() {
            return currentRecord.getId();
        }

        public long getDateTime() {
            return currentRecord.getDateTime();
        }

        public long getAmount() {
            return currentRecord.getAmount();
        }

        public String getCategoryName() {
            return currentRecord.getCategory().getName();
        }

        public String getCategoryDescription() {
            return currentRecord.getCategory().getDescription();
        }

        public String getDescription() {
            return currentRecord.getDescription();
        }
    }
}
