package Controller;

/**
 * Created by vasily on 07.07.15.
 */
public class SortParams {
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

    public SortParams setAccountIndex(int accountIndex) {
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

    public SortParams setCategoryIndex(int categoryIndex) {
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

    public SortParams setDateTimeRestricts(long dateTimeFrom, long dateTimeTo) {
        this.dateTimeFrom = dateTimeFrom;
        this.dateTimeTo = dateTimeTo;
        dateTimeRestricts = true;
        return this;
    }

    public SortParams offDateTimeRestricts() {
        dateTimeRestricts = false;
        return this;
    }

    public SortParams setAmountRestricts(long amountFrom, long amountTo) {
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        amountRestricts = true;
        return this;
    }

    public SortParams offAmountRestricts() {
        amountRestricts = false;
        return this;
    }
}
