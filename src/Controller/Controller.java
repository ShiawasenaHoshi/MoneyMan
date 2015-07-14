package Controller;

import Model.DataTypes.Record;

import javax.naming.OperationNotSupportedException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface Controller {
    int RECORD_NO_ID = Record.NO_ID;

    List<Record> getMainTable();

    boolean enter(String login, char[] password);

    boolean userExist(String login);

    void createNewUser(String login, char[] password);

    void loggedIn(String userName);

    String getAccount(int index);

    String[] getAccounts();

    String getCategory(int index) throws OperationNotSupportedException;

    String[] getCategories();

    void fillTableBy();

    void fillTableBy(SelectParams selectParams);

    void sortBy();

    void sortBy(List<Record> records, Comparator<Record> sorter);

    long getSpend();

    long getIncome();

    void removeRecord(int id);

    void saveNewRecord(int id, long amount, String description, int categoryIndex, long createTime, int accountIndex);

    void saveEditedRecord(int id, long amount, String description, int categoryIndex, long createTime) throws Exception;

    void addAccount(String description) throws Exception;

    void editAccount(int accountIndex, String description) throws Exception;

    void removeAccount(int accountIndex) throws Exception;

    void addCategory(String description) throws Exception;

    void editCategory(int categoryIndex, String description) throws Exception;

    void removeCategory(int categoryIndex) throws Exception;

    void calculateMinMaxAmountsAfterChanges(Record editedRecord);

    void calculateMinMax();

    void calculateMinMax(Collection<? extends Record> allRecords);

    long getMaxAmount();

    long getMinAmount();

    long getMaxDateTime();

    long getMinDateTime();

    void setCurrentRecord(int index);

    int getCurrentRecordID();

    long getCurrentRecordDateTime();

    long getCurrentRecordAmount();

    String getCurrentRecordCategoryName();

    String getCurrentRecordCategoryDescription();

    String getCurrentRecordDescription();
}
