package Controller;

/**
 * Created by vasily on 07.07.15.
 */
public class SelectParams {
    private int accountIndex = -1;
    private int categoryIndex = -1;
    private boolean amountRestricts = false;
    private long amountFrom = 0;
    private long amountTo = 0;
    private boolean dateTimeRestricts = false;
    private long dateTimeFrom;
    private long dateTimeTo;

    public int getAccountIndex() {
        return accountIndex;
    }

    public SelectParams setAccountIndex(int accountIndex) {
        this.accountIndex = accountIndex;
        return this;
    }

    public long getAmountFrom() {
        return amountFrom;
    }

    public boolean isAmountRestricted() {
        return amountRestricts;
    }

    public long getAmountTo() {
        return amountTo;
    }

    public int getCategoryIndex() {
        return categoryIndex;
    }

    public SelectParams setCategoryIndex(int categoryIndex) {
        this.categoryIndex = categoryIndex;
        return this;
    }

    public boolean isDateTimeRestricted() {
        return dateTimeRestricts;
    }

    public long getDateTimeFrom() {
        return dateTimeFrom;
    }

    public long getDateTimeTo() {
        return dateTimeTo;
    }

    public SelectParams setDateTimeRestricts(long dateTimeFrom, long dateTimeTo) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeTo = dateTimeTo;
        dateTimeRestricts = true;
        return this;
    }

    public SelectParams offDateTimeRestricts() {
        dateTimeRestricts = false;
        return this;
    }

    public SelectParams setAmountRestricts(long amountFrom, long amountTo) {
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        amountRestricts = true;
        return this;
    }

    public SelectParams offAmountRestricts() {
        amountRestricts = false;
        return this;
    }
}
