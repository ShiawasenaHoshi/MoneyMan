package Model.Base;

import Model.DataTypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DBStore implements DataStore {
    final private static Logger LOGGER = LoggerFactory.getLogger(DataStore.class);
    Connection connection;

    {
        connection = DBHelper.INSTANCE.getConnection();
    }

    @Override
    public User getUser(String name) {
        return (User) getDataByID(User.class, name);
    }

    @Override
    public Set<String> getUserNames() {
        Statement statement = null;
        ResultSet resultSet = null;
        Set<String> userNames = new HashSet<>();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM USERS;");
            while (resultSet.next()) {
                String name = resultSet.getString("NAME");
                userNames.add(name);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, statement);
        }
        if (userNames.size() == 0) {
            LOGGER.warn("В базе нет ни одного пользователя");
        }
        return userNames;
    }

    @Override
    public Set<Account> getAccounts(User owner) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Set<Account> accounts = new HashSet<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNTS WHERE USER_NAME = ?;");
            preparedStatement.setString(1, owner.getName());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int accountID = resultSet.getInt("ID");
                String accountDescription = resultSet.getString("DESCR");
                Account account = new Account(accountID, accountDescription);
                account.addRecords(getRecords(account));
                accounts.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
//        if (accounts.size() == 0) {
//            LOGGER.info("У пользователя {} нет ни одного счета", owner.getName());
//        }
        return accounts;
    }

    @Override
    public Set<Record> getRecords(Account account) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Set<Record> records = new HashSet<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM RECORDS WHERE ACCOUNT_ID=?;");
            preparedStatement.setInt(1, account.getID());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                long amount = resultSet.getLong("AMOUNT");
                String description = resultSet.getString("DESCR");
                String categoryName = resultSet.getString("CATEGORY_NAME");
                long createTime = resultSet.getLong("CREATE_TIME");
                preparedStatement = connection.prepareStatement("SELECT * FROM CATEGORIES WHERE NAME=?;");
                //TODO если такого идентификатора категории не существует, то вся база рухнет
                preparedStatement.setString(1, categoryName);
                Category category = getCategory(preparedStatement.executeQuery().getString("NAME"));
                //TODO я создаю много одинаковых объектов категории. Надо, чтобы на каждую был ОДИН объект. Можно сделать приватным конструктор и сделать статически геттер который проверяет есть ли такие же категории

                Record record = new Record(id, amount, description, category, createTime);
                records.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
//        if (records.size() == 0) {
//            LOGGER.info("В счете {} нет ни одной записи", account.getID());
//        }
        return records;
    }

    @Override
    public Category getCategory(String name) {
        return (Category) getDataByID(Category.class, name);
    }

    @Override
    public Set<Category> getCategories() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Set<Category> categories = new HashSet<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT * FROM CATEGORIES;");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                categories.add(new Category(resultSet.getString("NAME"), resultSet.getString("DESCR")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
        return categories;
    }

    @Override
    public User addUser(User user) {
        if (user == null) {
            LOGGER.error("Model.DataTypes.User не может быть равен null");
            return null;
        }
        for (String userName : getUserNames()) {
            if (user.getName().contentEquals(userName)) {
                LOGGER.error("Пользователь {} уже существует", userName);
                return null;
            }
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO USERS (name, password) VALUES (?, ?);");
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getPasswordHash());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Юзера {} добавить неполучилось: {}", user.getName(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return user;
    }

    @Override
    public Account addAccount(User user, Account account) {
        Account accountWithID = null;
        if (user == null) {
            LOGGER.error("Model.DataTypes.User не может быть равен null");
            return null;
        }
        if (account == null) {
            LOGGER.error("Model.DataTypes.Account не может быть равен null");
            return null;
        }
        if (getUser(user.getName()) == null) {
            LOGGER.error("В базе нет пользователя {}! Добавьте вначале его", user.getName());
            return null;
        }
        if (account.getID() != Account.NO_ID) {
            return setAccount(account);
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO ACCOUNTS (descr, user_name) VALUES (? , ?);");
            preparedStatement.setString(1, account.getDescription());
            preparedStatement.setString(2, user.getName());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            accountWithID = new Account(resultSet.getInt(1), account.getDescription());
        } catch (SQLException e) {
            LOGGER.error("Счет за {} {}  для {} добавить неполучилось: {}",
                    account.getID(), account.getDescription(), user.getName(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
        return accountWithID;
    }

    private Account setAccount(Account account) {
        Account accountWithID = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE Accounts SET descr = ? WHERE id = ?;");
            preparedStatement.setString(1, account.getDescription());
            preparedStatement.setInt(2, account.getID());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            accountWithID = new Account(resultSet.getInt(1), account.getDescription());
            //TODO Когда добавляется счет, необходимо обновить объект счета и добавить ему ID
        } catch (SQLException e) {
            LOGGER.error("Счет за {} {}  изменить не получилось: {}",
                    account.getID(), account.getDescription(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
        return accountWithID;
    }

    @Override
    public Record addRecord(Account account, Record record) {
        Record recordWithID = null;
        if (account == null) {
            LOGGER.error("Model.DataTypes.Account не может быть равен null");
            return null;
        }
        if (record == null) {
            LOGGER.error("Model.DataTypes.Record не может быть равен null");
            return null;
        }
        if (record.getId() != Record.NO_ID) {
            return setRecord(account, record);
        }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO records " +
                    "(amount, descr, create_time, category_name, account_id) values (?, ?, ?, ?, ?);");
            preparedStatement.setLong(1, record.getAmount());
            preparedStatement.setString(2, record.getDescription());
            preparedStatement.setLong(3, record.getDateTime());
            preparedStatement.setString(4, record.getCategory().getName());
            preparedStatement.setInt(5, account.getID());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            recordWithID = new Record(resultSet.getInt(1), record.getAmount(), record.getDescription(), record.getCategory(), record.getDateTime());
        } catch (SQLException e) {
            LOGGER.error("Запись {} в счет {} добавить неполучилось: {}",
                    record.toString(), account.getDescription(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
        return recordWithID;
    }

    private Record setRecord(Account account, Record record) {
        Record returnRecord = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE records " +
                    "SET amount = ?, descr = ?, create_time = ?, category_name = ?, account_id = ? " +
                    "WHERE id = ?;");
            preparedStatement.setLong(1, record.getAmount());
            preparedStatement.setString(2, record.getDescription());
            preparedStatement.setLong(3, record.getDateTime());
            preparedStatement.setString(4, record.getCategory().getName());
            preparedStatement.setInt(5, account.getID());
            preparedStatement.setInt(6, record.getId());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            returnRecord = new Record(resultSet.getInt(1), record.getAmount(), record.getDescription(), record.getCategory(), record.getDateTime());
        } catch (SQLException e) {
            LOGGER.error("Запись {} в счет {} добавить неполучилось: {}",
                    record.toString(), account.getDescription(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
        }
        return returnRecord;
    }

    @Override
    public Category addCategory(Category category) {
        if (category == null) {
            LOGGER.error("Category не может быть равен null");
            return null;
        }
        if (category.getName().equals(Category.NO_CATEGORY)) {
            LOGGER.error("Нельзя редактировать NO_CATEGORY");
            return null;
        }
        for (Category check : getCategories()) {
            if (check.getName().equals(category.getName())) {
                return setCategory(category);
            }
        }
        Category returnCategory = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO categories (name, descr) values (?, ?);");
            preparedStatement.setString(1, category.getName());
            preparedStatement.setString(2, category.getDescription());
            preparedStatement.executeUpdate();
            returnCategory = category;
        } catch (SQLException e) {
            LOGGER.error("Категорию {} добавить неполучилось: {}", category.toString(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return returnCategory;
    }

    private Category setCategory(Category category) {
        Category returnCategory = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("UPDATE categories SET descr = ? WHERE name = ?;");
            preparedStatement.setString(1, category.getDescription());
            preparedStatement.setString(2, category.getName());
            preparedStatement.executeUpdate();
            returnCategory = category;
        } catch (SQLException e) {
            LOGGER.error("Категорию {} изменить неполучилось: {}",
                    category.getName(), e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return returnCategory;
    }

    @Override
    public User removeUser(String name) {
        User removedUser = getUser(name);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM USERS WHERE NAME = ?;");
            preparedStatement.setString(1, name);
            preparedStatement.execute();
//            LOGGER.info("Пользователь {} удален", removedUser.getName());
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return removedUser;
    }

    @Override
    public Account removeAccount(User owner, Account account) {
        if (getDataByID(Account.class, String.valueOf(account.getID())) == null) {
            return null;
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNTS WHERE ID = ?;");
            preparedStatement.setInt(1, account.getID());
            preparedStatement.execute();
//            LOGGER.info("Счет {} {} пользователя {} удален", account.getID(), account.getDescription(), owner.getName());
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return account;
    }

    @Override
    public Record removeRecord(Account from, Record record) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM RECORDS WHERE ID = ?");
            preparedStatement.setInt(1, record.getId());
            preparedStatement.execute();
//            LOGGER.info("Запись {} {} удалена", record.getDescription(), record.getAmount());
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return new Record(Record.NO_ID, record.getAmount(), record.getDescription(), record.getCategory(), record.getDateTime());
    }

    @Override
    public Category removeCategory(Category category) {
        PreparedStatement preparedStatement = null;
        Category returnCategory = null;
        if (category.getName().equals(Category.NO_CATEGORY)) {
            LOGGER.error("Нельзя удалить NO_CATEGORY");
            return null;
        }
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM categories WHERE name = ?;");
            preparedStatement.setString(1, category.getName());
            preparedStatement.execute();
//            LOGGER.info("Категория {} {} удалена", category.getName(), category.getDescription());
            preparedStatement = connection.prepareStatement("UPDATE records SET category_name = ? WHERE category_name = ?;");
            preparedStatement.setString(1, Category.NO_CATEGORY);
            preparedStatement.setString(2, category.getName());
            preparedStatement.execute();
            returnCategory = category;
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage());
//            e.printStackTrace();
        } finally {
            DBHelper.INSTANCE.closeResources(preparedStatement);
        }
        return returnCategory;
    }

    @Override
    public DataType getDataByID(final Class dataType, String identificator) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        switch (dataType.getSimpleName()) {
            case "User": {
                User user = null;
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM USERS WHERE NAME = ?;");
                    preparedStatement.setString(1, identificator);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String password = resultSet.getString("PASSWORD");
                        user = new User(identificator, password);
//                        LOGGER.info(user.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
                }
                return user;
            }

            case "Account": {
                int id = Integer.parseInt(identificator);
                Account account = null;
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNTS WHERE ID = ?;");
                    preparedStatement.setInt(1, id);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String accountDescription = resultSet.getString("DESCR");
                        account = new Account(id, accountDescription);
//                        LOGGER.info(account.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
                }
                return account;
            }
            case "Record": {
                int id = Integer.parseInt(identificator);
                Record record = null;
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM RECORDS WHERE ID = ?;");
                    preparedStatement.setInt(1, id);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        Long amount = resultSet.getLong("AMOUNT");
                        String description = resultSet.getString("DESCR");
                        Long createTime = resultSet.getLong("CREATE_TIME");
                        String categoryName = resultSet.getString("CATEGORY_NAME");
                        record = new Record(id, amount, description, getCategory(categoryName), createTime);
//                        LOGGER.info(record.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
                }
                return record;
            }
            case "Category": {
                Category category = null;
                try {
                    preparedStatement = connection.prepareStatement("SELECT * FROM CATEGORIES WHERE NAME = ?;");
                    preparedStatement.setString(1, identificator);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String categoryName = resultSet.getString("NAME");
                        String categoryDescription = resultSet.getString("DESCR");
                        category = new Category(categoryName, categoryDescription);
//                        LOGGER.info(category.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DBHelper.INSTANCE.closeResources(resultSet, preparedStatement);
                }
                return category;
            }
        }
        return null;
    }
}
