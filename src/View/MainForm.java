package View;

import Controller.Controller;
import Controller.MoneyManTableModel;
import Model.DataTypes.Record;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by vasily on 03.07.15.
 */
public class MainForm extends JFrame implements Runnable {
    private JPanel rootPanel;
    private JTable tRecords;
    private JTabbedPane tpSortByTabs;
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
    private JFormattedTextField ftfRecordDateTime;
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
    private MoneyManTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public MainForm(Controller controller) {
        super("Главная форма");
        this.controller = controller;
        init();
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

    private void init() {
        refreshAccountList();
        tableModel = new MoneyManTableModel(controller);
        //fixme не хватает tableheader
        tRecords.setModel(tableModel);
        tRecords.addMouseListener(new TableMouseListener());
        tpSortByTabs.addChangeListener(new SortByTabsChangeListener());
        lCategories.addListSelectionListener(new LCategoriesSelectionListener());
        lAccounts.addListSelectionListener(new LAccountsSelectionListener());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        ftfRecordDateTime.setFormatterFactory(
                new DefaultFormatterFactory(
                        new DateFormatter(
                                dateFormat)));
        //про создание масок для formattedTextField http://stackoverflow.com/questions/4252257/jformattedtextfield-with-maskformatter
        try {
            MaskFormatter dateTimeMask = new MaskFormatter("####-##-## ##:##:##.###");
            dateTimeMask.install(ftfRecordDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (tableModel.getRowCount() > 0) {
            tRecords.setRowSelectionInterval(0, 0);
            refreshRecordEdit(tableModel.getRecordAt(0));
        }

    }

    private void refreshAccountList() {
        String[] accounts = controller.getAccounts();
        if (accounts.length > 0) {
            lAccounts.setListData(accounts);
        } else {
            return;
        }
    }

    private void refreshCategoryList() {
        String[] categories = controller.getCategories();
        //todo категорий как минимум одна, поскольку есть NO_CATEGORY
        if (categories.length > 0) {
            lCategories.setListData(categories);
        } else {
            return;
        }
    }

    //О том как редактировать отдельные ячейки http://stackoverflow.com/questions/5918727/updating-data-in-a-jtable
    // туториал по табличкам http://docs.oracle.com/javase/tutorial/uiswing/components/table.html
    // выделение нескольких http://stackoverflow.com/questions/14416188/jtable-how-to-get-selected-cells
    private void refreshTable() {
        tableModel.fireTableDataChanged();
    }

    //todo можно добавить множественное редактирование
    private void refreshRecordEdit(Record record) {
        lRecordID.setText("Запись №" + record.getId());
        ftfRecordDateTime.setValue(record.getCreateTime());
        ftfRecordAmount.setValue(record.getAmount());
        //fixme здесь должно быть обновление категории
        taRecordDescription.setText(record.getDescription());
    }

    private void writeRecord() {

    }

    private void createNewRecord() {

    }

    class LCategoriesSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            controller.fillTableByCategory(lCategories.getSelectedValue().toString());
            refreshTable();
        }
    }

    class LAccountsSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            //todo там где можно обойтись int пользоваться string не гуд
            String selected = lAccounts.getSelectedValue().toString();
            controller.fillTableByAccount(Integer.parseInt(selected.substring(0, selected.indexOf(" "))));
            refreshTable();
        }
    }

    class TableMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = tRecords.rowAtPoint(e.getPoint());
            refreshRecordEdit(tableModel.getRecordAt(row));
        }
    }

    class SortByTabsChangeListener implements ChangeListener {
        static final int ACCOUNT_SORT_TAB = 0;
        static final int CATEGORY_SORT_TAB = 1;
        static final int DATETIME_SORT_TAB = 2;
        static final int AMOUNT_SORT_TAB = 3;

        @Override
        public void stateChanged(ChangeEvent e) {
            switch (tpSortByTabs.getSelectedIndex()) {
                case ACCOUNT_SORT_TAB: {
                    refreshAccountList();
                    break;
                }
                case CATEGORY_SORT_TAB: {
                    refreshCategoryList();
                    break;
                }
                case DATETIME_SORT_TAB: {
                    break;
                }
                case AMOUNT_SORT_TAB: {
                    break;
                }
            }
        }

    }
}


