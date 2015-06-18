package Model.Base;

import Model.DataTypes.*;

import java.util.Set;

/**
 * Created by vasily on 27.05.15.
 */
public interface DataStore {
    User getUser(String name);

    Set<String> getUserNames();

    Set<Account> getAccounts(User owner);

    Set<Record> getRecords(Account account);

    Category getCategory(String name);

    Set<Category> getCategories();

    /*Эти методы возвращают свой тип данных для того, чтобы можно было создать новый объект и вернуть его же, но с
    * ID, который Autoincrement. Для User это смысла не несет, потому что имя юзера и есть его ID. Сделано, чтобы возвращался
    * его объект только для симметрии*/
    User addUser(User user);

    Account addAccount(User user, Account account);

    Record addRecord(Account account, Record record);

    Category addCategory(Category category);

    User removeUser(String name);

    Account removeAccount(User owner, Account account);

    Record removeRecord(Account from, Record record);

    Category removeCategory(Category category);

    //Этот метод любой объект по его ID. С помощью него можно, к примеру, проверять существование объекта в базе
    DataType getDataByID(Class dataType, String id);
}
