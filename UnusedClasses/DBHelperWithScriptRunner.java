import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;

/**
 * Created by vasily on 30.05.15.
 */
public enum DBHelper {
    INSTANCE;
    final private static Logger LOGGER = LoggerFactory.getLogger(DBHelper.class);
    String databaseUri = "jdbc:sqlite:MoneyMan.db";
    Connection connection;
    ScriptRunner scriptRunner;

    public Connection getConnection() {
        if(connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(databaseUri);
                LOGGER.info("Соединение установлено");
                if (!isTablesExist()) {
                    Statement stmt = connection.createStatement();
                    String createSql = readResource(DBHelper.class, "Model/SQLScripts/create.sql");
                    stmt.executeUpdate(createSql);
                    stmt.close();
                }
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

    public void runScript(String fileName) {
        if (scriptRunner == null) {
            scriptRunner = new ScriptRunner(connection, false, true);
        }
        try {
            InputStream is = this.getClass().getResourceAsStream(fileName);
            LOGGER.info(getClass().getResourceAsStream(fileName).toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "Cp1251"));
            scriptRunner.runScript(br);
        } catch (IOException e) {
            LOGGER.error("{}: {}", e.getClass(), e.getMessage());
        } catch (SQLException e) {
            LOGGER.error("{}: {}", e.getClass(), e.getMessage());
        }
    }

    private String readResource(Class cpHolder, String path) throws Exception {
        java.net.URL url = cpHolder.getResource(path);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }
}

class ScriptRunner {
    private static final String DEFAULT_DELIMITER = ";";

    private Connection connection;

    private boolean stopOnError;
    private boolean autoCommit;

    private PrintWriter logWriter = new PrintWriter(System.out);
    private PrintWriter errorLogWriter = new PrintWriter(System.err);

    private String delimiter = DEFAULT_DELIMITER;
    private boolean fullLineDelimiter = false;

    /**
     * Default constructor
     */
    public ScriptRunner(Connection connection, boolean autoCommit,
                        boolean stopOnError) {
        this.connection = connection;
        this.autoCommit = autoCommit;
        this.stopOnError = stopOnError;
    }

    public void setDelimiter(String delimiter, boolean fullLineDelimiter) {
        this.delimiter = delimiter;
        this.fullLineDelimiter = fullLineDelimiter;
    }

    /**
     * Setter for logWriter property
     *
     * @param logWriter - the new value of the logWriter property
     */
    public void setLogWriter(PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * Setter for errorLogWriter property
     *
     * @param errorLogWriter - the new value of the errorLogWriter property
     */
    public void setErrorLogWriter(PrintWriter errorLogWriter) {
        this.errorLogWriter = errorLogWriter;
    }

    /**
     * Runs an SQL script (read in using the Reader parameter)
     *
     * @param reader - the source of the script
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    connection.setAutoCommit(this.autoCommit);
                }
                runScript(connection, reader);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * Runs an SQL script (read in using the Reader parameter) using the
     * connection passed in
     *
     * @param conn   - the connection to use for the script
     * @param reader - the source of the script
     * @throws SQLException if any SQL errors occur
     * @throws IOException  if there is an error reading from the Reader
     */
    private void runScript(Connection conn, Reader reader) throws IOException,
            SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    println(trimmedLine);
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("//")) {
                    // Do nothing
                } else if (trimmedLine.length() < 1
                        || trimmedLine.startsWith("--")) {
                    // Do nothing
                } else if (!fullLineDelimiter
                        && trimmedLine.endsWith(getDelimiter())
                        || fullLineDelimiter
                        && trimmedLine.equals(getDelimiter())) {
                    command.append(line.substring(0, line
                            .lastIndexOf(getDelimiter())));
                    command.append(" ");
                    Statement statement = conn.createStatement();

                    println(command);

                    boolean hasResults = false;
                    if (stopOnError) {
                        hasResults = statement.execute(command.toString());
                    } else {
                        try {
                            statement.execute(command.toString());
                        } catch (SQLException e) {
                            e.fillInStackTrace();
                            printlnError("Error executing: " + command);
                            printlnError(e);
                        }
                    }

                    if (autoCommit && !conn.getAutoCommit()) {
                        conn.commit();
                    }

                    ResultSet rs = statement.getResultSet();
                    if (hasResults && rs != null) {
                        ResultSetMetaData md = rs.getMetaData();
                        int cols = md.getColumnCount();
                        for (int i = 0; i < cols; i++) {
                            String name = md.getColumnLabel(i);
                            print(name + "\t");
                        }
                        println("");
                        while (rs.next()) {
                            for (int i = 0; i < cols; i++) {
                                String value = rs.getString(i);
                                print(value + "\t");
                            }
                            println("");
                        }
                    }

                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        // Ignore to workaround a bug in Jakarta DBCP
                    }
                    Thread.yield();
                } else {
                    command.append(line);
                    command.append(" ");
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            printlnError("Error executing: " + command);
            printlnError(e);
            throw e;
        } finally {
            conn.rollback();
            flush();
        }
    }

    private String getDelimiter() {
        return delimiter;
    }

    private void print(Object o) {
        if (logWriter != null) {
            System.out.print(o);
        }
    }

    private void println(Object o) {
        if (logWriter != null) {
            logWriter.println(o);
        }
    }

    private void printlnError(Object o) {
        if (errorLogWriter != null) {
            errorLogWriter.println(o);
        }
    }

    private void flush() {
        if (logWriter != null) {
            logWriter.flush();
        }
        if (errorLogWriter != null) {
            errorLogWriter.flush();
        }
    }
}

