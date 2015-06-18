package Model.Base;

import org.junit.Test;

import java.sql.Connection;

/**
 * Created by vasily on 31.05.15.
 */
public class DBHelperTest {

    @Test
    public void testGetConnection() throws Exception {
        Connection connection = DBHelper.INSTANCE.getConnection();
    }
}