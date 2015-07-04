package View;

import Controller.Controller;
import Controller.MoneyManTableModel;
import Model.DataTypes.Category;
import Model.DataTypes.Record;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by vasily on 03.07.15.
 */
public class MainForm extends JFrame implements Runnable {
    private JPanel rootPanel;
    private JTable tRecords;
    private JTabbedPane SortByTabs;
    private JList lCategories;
    private JList lAccounts;
    private JSlider sDateFrom;
    private JSlider sDateTo;
    private JFormattedTextField ftfDateTo;
    private JFormattedTextField ftfDateFrom;
    private JSlider sAmountFrom;
    private JFormattedTextField ftfAmountFrom;
    private JFormattedTextField ftfAmountTo;
    private JSlider sAmountTo;
    private JFormattedTextField ftfRecordDate;
    private JFormattedTextField ftfRecordAmount;
    private JComboBox cbRecordCategory;
    private JTextArea taRecordDescription;
    private JLabel lRecordID;
    private JButton bAddRecord;
    private JButton bRemoveRecord;
    private JLabel jRecordsCount;
    private JLabel jSpend;
    private JLabel jIncome;
    private JPanel StatisticPanel;
    private JPanel TablePanel;
    private JPanel RecordAddRemovePanel;
    private JPanel RecordEditPanel;
    private JPanel ControlPanel;
    private Controller controller;
    private String[] columnNames = {"ID", "Дата", "Сумма", "Категория", "Описание"};

    public MainForm(Controller controller) {
        super("Главная форма");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });
        firstRun();

    }

    private void onExit() {
        System.exit(0);
    }

    @Override
    public void run() {
        this.setContentPane(rootPanel);
        this.pack();
        this.setVisible(true);
    }

    private void firstRun() {
        refreshAccountList();
        tRecords.setModel(new MoneyManTableModel(controller));
//        tRecords = new JTable(new MoneyManTableModel(controller));
//        Iterator<Account> accountIterator = accounts.iterator();
//        if (accountIterator.hasNext()) {
//            Account account = accountIterator.next();
//            refreshTable(account.getRecords());
//        }
    }

    private void refreshAccountList() {
        String[] accounts = controller.getAccounts();
        if (accounts.length > 0) {
            lAccounts.setListData(accounts);
        } else {
            return;
        }
    }

    private void refreshCategoryList(Collection<Category> collection) {
        Iterator<Category> categoryIterator = collection.iterator();
        if (categoryIterator.hasNext()) {

        } else {
            return;
        }
    }

    //О том как редактировать отдельные ячейки http://stackoverflow.com/questions/5918727/updating-data-in-a-jtable
    // туториал по табличкам http://docs.oracle.com/javase/tutorial/uiswing/components/table.html
    private void refreshTable() {
        tRecords.invalidate();
    }

    private void refreshRecordEdit(Record record) {

    }

    private void writeRecord() {

    }

    private void createNewRecord() {

    }


}

class MyModel extends AbstractTableModel {

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}
