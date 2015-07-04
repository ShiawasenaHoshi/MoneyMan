package Controller;

import Model.DataTypes.Record;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
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

    public MoneyManTableModel(Controller controller) {
        this.controller = controller;
        records = new ArrayList<>();
        records.addAll(controller.getMainTable());
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
        switch (columnIndex) {
            case ID_COLUMN:
                return records.get(rowIndex).getId();
            case CREATE_TIME_COLUMN:
                return records.get(rowIndex).getCreateTime();
            case AMOUNT_COLUMN:
                return records.get(rowIndex).getAmount();
            case CATEGORY_COLUMN:
                return records.get(rowIndex).getCategory().getName();
            case DESCRIPTION_COLUMN:
                return records.get(rowIndex).getDescription();
        }
        return null;
    }
}
