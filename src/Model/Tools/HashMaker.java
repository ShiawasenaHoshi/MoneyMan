package Model.Tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by vasily on 31.05.15.
 */
public class HashMaker {
    public static boolean stringAreEquals(char[] password, String hash) {
        return (getHash(password)).equals(hash);
    }
//TODO может быть мне стоит перевести все на charArray? Со стрингом была пометка deprecated и говорилось об уязвимости использования стринга

    public static String getHash(String password) {
        return getHash(password.toCharArray());
    }

    public static String getHash(char[] password) {
        String s = String.valueOf(password);
        String md5Hex = getMD5(s);
        StringBuilder saltBuilder = new StringBuilder();
        char[] md5HexChars = md5Hex.toCharArray();
        for (int i = 0; i < md5HexChars.length; i += 2) {
            saltBuilder.append(md5HexChars[i]);
        }
        md5Hex += saltBuilder.toString();
        return getSHA(md5Hex);
    }

    private static String getMD5(String s) {
        MessageDigest messageDigest;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(s.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        BigInteger bigInt = new BigInteger(1, digest);
        String md5Hex = bigInt.toString(16);

        while (md5Hex.length() < 32) {
            md5Hex = "0" + md5Hex;
        }

        return md5Hex;
    }

    private static String getSHA(String s) {
        MessageDigest messageDigest = null;
        byte digest[] = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(s.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (messageDigest != null) {
            messageDigest.update(s.getBytes());
        }

        StringBuilder hexString = new StringBuilder();
        for (byte aDigest : digest) {
            String hex = Integer.toHexString(0xff & aDigest);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
