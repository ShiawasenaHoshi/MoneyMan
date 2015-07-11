package Controller;

import Model.DataTypes.Record;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by vasily on 04.07.15.
 */
public class MoneyManTableModel extends AbstractTableModel {
    public static final int COLUMNS_COUNT = 5;
    public static final int ID_COLUMN = 0;
    public static final int CREATE_TIME_COLUMN = 1;
    public static final int AMOUNT_COLUMN = 2;
    public static final int CATEGORY_COLUMN = 3;
    public static final int DESCRIPTION_COLUMN = 4;
    List<Record> records;
    Controller controller;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private String[] columnNames;

    public MoneyManTableModel(Controller controller, String[] columnNames) {
        this.controller = controller;
        this.columnNames = columnNames;
        records = controller.getMainTable();
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < records.size() && records.size() > 0)
        switch (columnIndex) {
            case ID_COLUMN:
                return records.get(rowIndex).getId();
            case CREATE_TIME_COLUMN:
                return dateFormat.format(records.get(rowIndex).getDateTime());
            case AMOUNT_COLUMN: {
                long amount = records.get(rowIndex).getAmount();
                return amount > 0 ? "+" + amount : amount;
            }
            case CATEGORY_COLUMN:
                return records.get(rowIndex).getCategory().getDescription();
            case DESCRIPTION_COLUMN:
                return records.get(rowIndex).getDescription();
        }
        return null;
    }

    public Record getRecordAt(int index) {
        if (index >= 0) {
            return records.get(index);
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
}
