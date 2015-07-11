package Model.DataTypes;

import java.text.SimpleDateFormat;

/**
 * Created by vasily on 27.05.15.
 */
public class Record extends DataType {
    static SimpleDateFormat simpleDateFormat;
    private int id;
    private long amount;
    private String description;
    private Category category;
    private long dateDime;

    public Record(int id, long amount, String description, Category category, long createTime) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.dateDime = createTime;
    }

    public static Record getNewRecordNoID(long amount, String description, Category category) {
        return new Record(NO_ID, amount, description, category, System.currentTimeMillis());
    }

    public long getDateTime() {
        return dateDime;
    }

    public long getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        if (simpleDateFormat == null) {
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        return "Model.DataTypes.Record{" +
                "amount=" + amount +
                ", dateDime=" + simpleDateFormat.format(dateDime) +
                ", category=" + category.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        if (amount != record.amount) return false;
        if (dateDime != record.dateDime) return false;
        if (!description.equals(record.description)) return false;
        return category.equals(record.category);

    }

    @Override
    public int hashCode() {
        int result = (int) (amount ^ (amount >>> 32));
        result = 31 * result + description.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + (int) (dateDime ^ (dateDime >>> 32));
        return result;
    }
}
