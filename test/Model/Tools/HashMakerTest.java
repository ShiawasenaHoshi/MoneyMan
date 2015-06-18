package Model.Tools;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by vasily on 31.05.15.
 */
public class HashMakerTest {
    char[] password = "321".toCharArray();
    char[] wrongPassword = "1234".toCharArray();

    @Test
    public void testStringAreEquals() throws Exception {
        String hash = HashMaker.getHash(password);
        Assert.assertTrue(HashMaker.stringAreEquals(password, hash));
        Assert.assertFalse(HashMaker.stringAreEquals(wrongPassword, hash));
    }

    @Test
    public void testGetHash() throws Exception {
        Assert.assertNotNull(HashMaker.getHash(password));
    }
}