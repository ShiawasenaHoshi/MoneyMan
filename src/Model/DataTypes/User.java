package Model.DataTypes;

import Model.Tools.HashMaker;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vasily on 27.05.15.
 */
public class User extends DataType {
    private String name;
    private String passwordHash;
    private Set<Account> accounts;

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
        accounts = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean checkPassword(char[] password) {
        return HashMaker.stringAreEquals(password, passwordHash);
    }

    @Override
    public String toString() {
        return "User{" +
                "accounts=" + accounts +
                ", name='" + name + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
