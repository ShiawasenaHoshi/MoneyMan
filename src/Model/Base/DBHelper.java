package Model.Base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Created by vasily on 30.05.15.
 */
public enum DBHelper {
    INSTANCE;
    final private static Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);
    private Connection connection;

    public Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                String databaseUri = "jdbc:sqlite:MoneyMan.db";
                connection = DriverManager.getConnection(databaseUri);
                LOGGER.info("Соединение установлено");
                if (!isTablesExist()) {
                    createTables();
                }
                LOGGER.info("Таблицы есть");
            } catch (SQLException e) {
                LOGGER.error("{}", e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                LOGGER.error("{}", e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                LOGGER.error("{}", e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void closeResources(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            try {
                if (res != null) {
                    res.close();
                    res = null;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to close resource: {}", res);
            }
        }
    }

    //TODO сделать завершение коннекшна спустя какое-то время
    public void closeConnection() {
        if (connection != null)
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                LOGGER.error("{}", e.getMessage());
                e.printStackTrace();
            }
    }

    public void recreateTables() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String createSql = readResource(DBHelper.class, "/Model/SQLScripts/drop_tables.sql");
            statement.executeUpdate(createSql);
            LOGGER.info("Все таблицы дропнуты");
            createTables();
        } catch (SQLException e) {
            LOGGER.error("{}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(statement);
        }
    }

    //TODO мне не нравится, что здесь и в следующем метод эксепшн вываливается, а в других местах обрабатывается. Не красиво
    private void createTables() throws Exception {
        LOGGER.info("Таблиц нет. Создаем");
        //TODO неплохо бы проверить не только наличие таблиц, но и их правильность
        Statement stmt = connection.createStatement();
        String createSql = readResource(DBHelper.class, "/Model/SQLScripts/create.sql");
        stmt.executeUpdate(createSql);
        //String insertSql = readResource(DBHelper.class, "/Model/SQLScripts/insert.sql");
        //stmt.executeUpdate(insertSql);
        stmt.close();
    }

    private boolean isTablesExist() throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='USERS';");
        boolean result = true;
        int count = rs.getInt(1);
        if (count == 0) {
            result = false;
        }
        rs.close();
        stmt.close();
        return result;
    }

    private String readResource(Class cpHolder, String path) throws Exception {
        java.net.URL url = cpHolder.getResource(path);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }
}
