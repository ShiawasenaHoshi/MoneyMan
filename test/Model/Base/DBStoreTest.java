package Model.Base;

import Model.DataTypes.Account;
import Model.DataTypes.Category;
import Model.DataTypes.Record;
import Model.DataTypes.User;
import Model.Tools.HashMaker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by vasily on 31.05.15.
 */
public class DBStoreTest {
    DataStore db = new DBStore();

    @Before
    public void initialize() {
        DBHelper.INSTANCE.recreateTables();
    }

    @After
    public void finish() {
        DBHelper.INSTANCE.closeConnection();
    }

    @Test
    public void testGetUser() throws Exception {
        Assert.assertTrue(db.getUser("Alisa").getName().equals("Alisa"));
    }

    @Test
    public void testGetUserNames() throws Exception {
        Set<String> userNames = db.getUserNames();
        Iterator it = userNames.iterator();
        String Bob = (String) it.next();
        String Alisa = (String) it.next();
        System.out.println(Bob);
        System.out.println(Alisa);
        Assert.assertTrue(Bob.equals("Bob"));
        Assert.assertTrue(Alisa.equals("Alisa"));
    }

    @Test
    public void testGetAccounts() throws Exception {
        Assert.assertNotNull(db.getAccounts(db.getUser("Alisa")));
    }

    @Test
    public void testGetRecords() throws Exception {
        for (Account account : db.getAccounts(db.getUser("Bob"))) {
            for (Record record : db.getRecords(account)) {
                Assert.assertNotNull(record);
            }
        }
    }

    @Test
    public void testAddUser() throws Exception {
        User enot = db.addUser(new User("Енот", HashMaker.getHash("енот")));
        String enotName = null;
        for (String s : db.getUserNames()) {
            if (s.equals("Енот")) {
                enotName = s;
            }
        }
        Assert.assertNotNull(enotName);
        Assert.assertNull(db.addUser(new User("Енот", HashMaker.getHash("енот"))));
        User nullUser = null;
        Assert.assertNull(db.addUser(nullUser));
    }

    @Test
    public void testAddAccount() throws Exception {
        User user = db.getUser("Alisa");
        User nullUser = null;
        Account account = Account.getNewAccountNoID("Дома" + Math.random());
        Account nullAccount = null;
        User unknownUser = new User("Барсук", HashMaker.getHash("барсук"));
        Assert.assertNotNull(db.addAccount(user, account));
        Assert.assertNull(db.addAccount(nullUser, account));
        Assert.assertNull(db.addAccount(user, nullAccount));
        Assert.assertNull(db.addAccount(unknownUser, account));
    }

    @Test
    public void testAddRecord() throws Exception {
        User user = db.getUser("Bob");
        Set<Account> accountSet = db.getAccounts(user);
        Iterator<Account> it = accountSet.iterator();
        Account account = it.next();
        Record newRecord = Record.getNewRecordNoID(12312321L, "qwwfqwfqfw", db.getCategory(Category.NO_CATEGORY));
        Record recordWithID = db.addRecord(account, newRecord);
        Assert.assertNotNull(recordWithID);
        Set<Record> recordSet = db.getRecords(account);
        boolean recordExist = false;
        for (Record record : recordSet) {
            if (record.equals(newRecord)) {
                recordExist = true;
            }
        }
        Assert.assertTrue(recordExist);
    }

    @Test
    public void testRemoveUser() throws Exception {
        User user = db.removeUser("Alisa");
        boolean alisaExists = false;
        for (String s : db.getUserNames()) {
            if (s.equals("Alisa")) {
                alisaExists = true;
            }
        }
        Assert.assertFalse(alisaExists);
        Assert.assertNotNull(db.addUser(user));
    }

    @Test
    public void testRemoveAccount() throws Exception {
        User user = db.getUser("Alisa");
        Set<Account> accountSet = db.getAccounts(user);
        Iterator<Account> it = accountSet.iterator();
        Account account = it.next();
        Assert.assertEquals(db.removeAccount(user, account), account);
    }

    @Test
    public void testRemoveRecord() throws Exception {
        User user = db.getUser("Alisa");
        Set<Account> accountSet = db.getAccounts(user);
        Iterator<Account> it = accountSet.iterator();
        Account account = it.next();
        Set<Record> recordSet = db.getRecords(account);
        Record record = recordSet.iterator().next();
        Assert.assertEquals(db.removeRecord(account, record), record);
        recordSet = db.getRecords(account);
        boolean recordExist = false;
        for (Record record2 : recordSet) {
            if (record.equals(record2)) {
                recordExist = true;
            }
        }
        Assert.assertFalse(recordExist);
    }

    @Test
    public void testGetDataById() throws Exception {
        Assert.assertTrue(((User) db.getDataByID(User.class, "Alisa")).getName().equals("Alisa"));
        Assert.assertTrue(((Account) db.getDataByID(Account.class, "1")).getID() == 1);
        Assert.assertTrue(((Record) db.getDataByID(Record.class, "1")).getId() == 1);
        Assert.assertTrue(((Category) db.getDataByID(Category.class, Category.NO_CATEGORY)).getName().equals(Category.NO_CATEGORY));
    }

    @Test
    public void testGetCategory() throws Exception {
        String categoryName = ((Category) db.getDataByID(Category.class, Category.NO_CATEGORY)).getName();
        Assert.assertEquals(db.getCategory(categoryName).getName(), categoryName);
    }

    @Test
    public void testGetCategories() throws Exception {
        Set<Category> categories = db.getCategories();
        for (Category category : categories) {
            Assert.assertNotNull(category);
        }
    }

    @Test
    public void testAddCategory() throws Exception {

    }

    @Test
    public void testRemoveCategory() throws Exception {

    }


}