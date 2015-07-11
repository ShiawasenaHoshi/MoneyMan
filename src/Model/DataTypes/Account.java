package Model.DataTypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by vasily on 27.05.15.
 */
public class Account extends DataType {
    public static final int NO_ID = -1;
    private Set<Record> records;
    private int id;
    private String description;

    public Account(int id, String description) {
        this.id = id;
        this.description = description;
        records = new HashSet<>();
    }

    public Set<Record> getRecords() {
        return records;
    }

    public Account addRecords(Collection<? extends Record> collection) {
        records.addAll(collection);
        return this;
    }

    public String getDescription() {
        return description;
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return "Model.DataTypes.Account{" +
                "id=" + id +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return id == account.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
