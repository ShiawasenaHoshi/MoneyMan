package View;

import Controller.Controller;
import Controller.MoneyManTableModel;
import Controller.SelectParams;
import Controller.WindowController;

import javax.naming.OperationNotSupportedException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class MainForm extends JFrame implements Runnable {
    public static final String NEW_RECORD = "Новая запись";
    public static final String RECORD_NUMBER_TEXT = "Запись №";
    public static final String INCOME_TEXT = "Приход: +";
    public static final String SPEND_TEXT = "Расход: ";
    public static final String RECORDS_COUNT_TEXT = "Записей: ";
    private JPanel rootPanel;
    private JTable tRecords;
    private JTabbedPane tpSortByTabs;
    private JList<String> lCategories;
    private JList<String> lAccounts;
    private JFormattedTextField ftfDateTo;
    private JFormattedTextField ftfDateFrom;
    private JFormattedTextField ftfAmountFrom;
    private JFormattedTextField ftfAmountTo;
    private JFormattedTextField ftfRecordDateTime;
    private JFormattedTextField ftfRecordAmount;
    private JComboBox<String> cbRecordCategory;
    private JTextArea taRecordDescription;
    private JLabel lRecordID;
    private JButton bCreateRecord;
    private JButton bRemoveRecord;
    private JLabel jRecordsCount;
    private JLabel jSpend;
    private JLabel jIncome;
    private JPanel StatisticPanel;
    private JPanel TablePanel;
    private JPanel RecordAddRemovePanel;
    private JPanel RecordEditPanel;
    private JPanel ControlPanel;
    private JButton bSaveRecord;
    private JButton bAddAccount;
    private JButton bEditAccount;
    private JButton bRemoveAccount;
    private JButton bAddCategory;
    private JButton bEditCategory;
    private JButton bRemoveCategory;
    private JTextField tfAccountDescription;
    private JPanel accountPanel;
    private JPanel categoryPanel;
    private JTextField tfCategoryDescription;
    private Controller controller;
    //todo сделать возможность сокрытия колонок
    private String[] columnNames = {"ID", "Дата", "Сумма", "Категория", "Описание"};
    private MoneyManTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private Frame thisFrame;
    private MessageBoxes messageBoxes;
    private int currentAccountIndex;
    private int currentCategoryIndex;

    public MainForm(Controller controller) {
        super("Главная форма");
        this.controller = controller;
        thisFrame = this;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
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
        messageBoxes = new MessageBoxes();
        //Иницализация таблицы
        tableModel = new MoneyManTableModel(controller, columnNames);
        tRecords.setModel(tableModel);
        tRecords.getSelectionModel().addListSelectionListener(new TableSelectionChangeListener());
        tRecords.setDefaultRenderer(Object.class, new TableRenderer());

        //Инициализация кнопок редактирования записи
        EditRecordButtonsListener editRecordButtonsListener = new EditRecordButtonsListener();
        bCreateRecord.addActionListener(editRecordButtonsListener);
        bRemoveRecord.addActionListener(editRecordButtonsListener);
        bSaveRecord.addActionListener(editRecordButtonsListener);

        //Инициализация табов
        tpSortByTabs.addChangeListener(new SortByTabsChangeListener());
        //Инициализация таба с счетами
        lAccounts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lAccounts.addListSelectionListener(new LAccountsSelectionListener());
        AccountButtonsListener accountButtonsListener = new AccountButtonsListener();
        bAddAccount.addActionListener(accountButtonsListener);
        bEditAccount.addActionListener(accountButtonsListener);
        bRemoveAccount.addActionListener(accountButtonsListener);
        tfAccountDescription.setVisible(false);
        tfAccountDescription.addKeyListener(new AccountTextFieldListener());

        //Инициализация таба с категориями
        lCategories.setAutoscrolls(true);
        lCategories.addListSelectionListener(new LCategoriesSelectionListener());
        CategoryButtonsListener categoryButtonsListener = new CategoryButtonsListener();
        bAddCategory.addActionListener(categoryButtonsListener);
        bEditCategory.addActionListener(categoryButtonsListener);
        bRemoveCategory.addActionListener(categoryButtonsListener);
        tfCategoryDescription.setVisible(false);
        tfCategoryDescription.addKeyListener(new CategoryTextFieldListener());

        DefaultFormatterFactory amountDFF = new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat()));
        ftfAmountFrom.setFormatterFactory(amountDFF);
        ftfAmountTo.setFormatterFactory(amountDFF);
        ftfAmountFrom.setValue(controller.getMinAmount());
        ftfAmountTo.setValue(controller.getMaxAmount());
        AmountFromToListener amountListener = new AmountFromToListener();
        ftfAmountFrom.addKeyListener(amountListener);
        ftfAmountTo.addKeyListener(amountListener);

        DateTimeFromToListener dateTimeFromToListener = new DateTimeFromToListener();
        ftfDateFrom.addKeyListener(dateTimeFromToListener);
        ftfDateTo.addKeyListener(dateTimeFromToListener);

        DefaultFormatterFactory dateTimeDFF = new DefaultFormatterFactory(new DateFormatter(dateFormat));
        ftfRecordDateTime.setFormatterFactory(dateTimeDFF);
        ftfDateFrom.setFormatterFactory(dateTimeDFF);
        ftfDateTo.setFormatterFactory(dateTimeDFF);
        try {
            MaskFormatter dateTimeMask = new MaskFormatter("####-##-## ##:##:##.###");
            dateTimeMask.install(ftfRecordDateTime);
            dateTimeMask.install(ftfDateFrom);
            dateTimeMask.install(ftfDateTo);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //noinspection MagicConstant
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        refreshTabAccount();
        if (controller.getAccounts().length > 0) {
            currentAccountIndex = 0;
            lAccounts.setSelectedIndex(0);
        }
        refreshTable();
        if (tableModel.getRowCount() > 0) {
            controller.setCurrentRecord(0);
            tRecords.setRowSelectionInterval(0, 0);
            refreshPanelRecordEdit((Integer) tableModel.getValueAt(tRecords.getSelectedRow(), MoneyManTableModel.ID_COLUMN));
        }

    }

    private void refreshAfterEditRecord() {
        switch (tpSortByTabs.getSelectedIndex()) {
            case SortByTabsChangeListener.ACCOUNT_SORT_TAB: {
                refreshTabAccount();
                refreshTable();
                break;
            }
            case SortByTabsChangeListener.CATEGORY_SORT_TAB: {
                refreshTabCategory();
                refreshTable();
                break;
            }
            case SortByTabsChangeListener.AMOUNT_SORT_TAB: {
                refreshTabAmount();
                refreshTable();
                break;
            }
            case SortByTabsChangeListener.DATETIME_SORT_TAB: {
                refreshTabDateTime();
                refreshTable();
                break;
            }
        }
    }

    private void refreshTabDateTime() {
        ftfDateFrom.setValue(controller.getMinDateTime());
        ftfDateTo.setValue(controller.getMaxDateTime());
    }

    private void refreshTabAmount() {
        ftfAmountFrom.setValue(controller.getMinAmount());
        ftfAmountTo.setValue(controller.getMaxAmount());
    }

    private void refreshTabAccount() {
        accountPanel.validate();
        String[] accounts = controller.getAccounts();
        if (accounts.length >= 0) {
            lAccounts.setListData(accounts);
        }
    }

    private void refreshTabCategory() {
        categoryPanel.validate();
        String[] categories = controller.getCategories();
        if (categories.length > 0) {
            lCategories.setListData(categories);
        }
    }

    private void refreshTable() {
        controller.fillTableBy();
        tableModel.fireTableDataChanged();
        jIncome.setText(INCOME_TEXT + controller.getIncome());
        jSpend.setText(SPEND_TEXT + controller.getSpend());
        jRecordsCount.setText(RECORDS_COUNT_TEXT + tableModel.getRowCount());
    }

    //todo можно добавить множественное редактирование
    private void refreshPanelRecordEdit(int currentRecordID) {
        controller.setCurrentRecord(currentRecordID);
        lRecordID.setText(RECORD_NUMBER_TEXT + controller.getCurrentRecordID());
        ftfRecordDateTime.setValue(controller.getCurrentRecordDateTime());
        ftfRecordAmount.setValue(controller.getCurrentRecordAmount());
        //Этот список категорий обновляется из кэша
        cbRecordCategory.removeAllItems();
        for (String s : controller.getCategories()) {
            cbRecordCategory.addItem(s);
        }
        cbRecordCategory.setSelectedItem(controller.getCurrentRecordCategoryDescription());
        taRecordDescription.setText(controller.getCurrentRecordDescription());
    }

    //todo множественное удаление
    class EditRecordButtonsListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(bCreateRecord)) {
                createRecord();
            } else if (e.getSource().equals(bRemoveRecord)) {
                removeRecord();
            } else if (e.getSource().equals(bSaveRecord)) {
                saveRecord();
                createRecord();
            }
        }

        private void saveRecord() {
            int id;
            long amount = ((Number) ftfRecordAmount.getValue()).longValue();
            String description = taRecordDescription.getText();
            int categoryIndex = cbRecordCategory.getSelectedIndex();
            long dateTime;
            try {
                dateTime = dateFormat.parse(ftfRecordDateTime.getText()).getTime();
            } catch (ParseException parseException) {
                messageBoxes.errorDateIncorrect();

                parseException.printStackTrace();
                return;
            }
            if (lRecordID.getText().equals(NEW_RECORD)) {
                id = Controller.RECORD_NO_ID;
            } else {
                id = controller.getCurrentRecordID();
            }
            if (id != Controller.RECORD_NO_ID && currentAccountIndex == -1) {
                messageBoxes.warningChooseAccount();
                return;
            }
            if (tpSortByTabs.getSelectedIndex() == SortByTabsChangeListener.ACCOUNT_SORT_TAB && currentAccountIndex >= 0) {
                controller.saveNewRecord(id, amount, description, categoryIndex, dateTime, currentAccountIndex);
            } else {
                try {
                    controller.saveEditedRecord(id, amount, description, categoryIndex, dateTime);
                } catch (Exception exception) {
                    messageBoxes.warningChooseAccount();
                    return;
                }
            }
            refreshAfterEditRecord();
            lAccounts.setSelectedIndex(currentAccountIndex);
        }

        private void removeRecord() {
            int row = tRecords.getSelectedRow();
            controller.removeRecord(controller.getCurrentRecordID());
            refreshAfterEditRecord();
            if (row >= tRecords.getRowCount()) {
                row = tRecords.getRowCount() - 1;
            }
            if (row >= 0) {
                tRecords.setRowSelectionInterval(row, row);
            }
            lAccounts.setSelectedIndex(currentAccountIndex);
        }

        private void createRecord() {
            lRecordID.setText(NEW_RECORD);
            ftfRecordDateTime.setValue(System.currentTimeMillis());
            ftfRecordAmount.setValue(0);
            cbRecordCategory.removeAllItems();
            for (String s : controller.getCategories()) {
                cbRecordCategory.addItem(s);
            }
            taRecordDescription.setText("");
        }

    }

    class LCategoriesSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (lCategories.getSelectedIndex() >= 0) {
                currentCategoryIndex = lCategories.getSelectedIndex();
                controller.fillTableBy(new SelectParams().setCategoryIndex(lCategories.getSelectedIndex()));
                refreshTable();
            }
        }
    }

    class LAccountsSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (lAccounts.getSelectedIndex() >= 0) {
                currentAccountIndex = lAccounts.getSelectedIndex();
                controller.fillTableBy(new SelectParams().setAccountIndex(currentAccountIndex));
                refreshTable();
            }
        }
    }

    class TableSelectionChangeListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            int row = tRecords.getSelectedRow();
            if (row >= 0) {
                controller.setCurrentRecord(tRecords.getSelectedRow());
                refreshPanelRecordEdit((Integer) tableModel.getValueAt(tRecords.getSelectedRow(), MoneyManTableModel.ID_COLUMN));
            }
        }
    }

    class SortByTabsChangeListener implements ChangeListener {
        public static final int ACCOUNT_SORT_TAB = 0;
        public static final int CATEGORY_SORT_TAB = 1;
        public static final int DATETIME_SORT_TAB = 2;
        public static final int AMOUNT_SORT_TAB = 3;

        @Override
        public void stateChanged(ChangeEvent e) {
            switch (tpSortByTabs.getSelectedIndex()) {
                case ACCOUNT_SORT_TAB: {
                    bCreateRecord.setEnabled(true);
                    refreshTabAccount();
                    break;
                }
                case CATEGORY_SORT_TAB: {
                    bCreateRecord.setEnabled(false);
                    refreshTabCategory();
                    break;
                }
                case DATETIME_SORT_TAB: {
                    bCreateRecord.setEnabled(false);
                    refreshTabDateTime();
                    break;
                }
                case AMOUNT_SORT_TAB: {
                    bCreateRecord.setEnabled(false);
                    refreshTabAmount();
                    break;
                }
            }
        }
    }

    class AmountFromToListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                if (tpSortByTabs.getSelectedIndex() == SortByTabsChangeListener.AMOUNT_SORT_TAB) {
                    if ((long) ftfAmountFrom.getValue() > (long) ftfAmountTo.getValue()) {
                        ftfAmountFrom.setValue((long) ftfAmountTo.getValue() - 1);
                    }
                    controller.fillTableBy(new SelectParams().setAmountRestricts((long) ftfAmountFrom.getValue(), (long) ftfAmountTo.getValue()));
                    refreshTable();
                }
            }
        }
    }

    class DateTimeFromToListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                if (tpSortByTabs.getSelectedIndex() == SortByTabsChangeListener.DATETIME_SORT_TAB) {
                    long dateTimeFrom;
                    long dateTimeTo;
                    try {
                        dateTimeFrom = dateFormat.parse(ftfDateFrom.getText()).getTime();
                        dateTimeTo = dateFormat.parse(ftfDateTo.getText()).getTime();
                    } catch (ParseException exception) {
                        messageBoxes.errorDateIncorrect();
                        exception.printStackTrace();
                        return;
                    }
                    if (dateTimeFrom > dateTimeTo) {
                        dateTimeFrom = dateTimeTo - 10000;
                        ftfDateFrom.setValue(dateTimeFrom);
                        ftfDateTo.setValue(dateTimeTo);
                    }
                    controller.fillTableBy(new SelectParams().setDateTimeRestricts(dateTimeFrom, dateTimeTo));
                    refreshTable();
                }
            }
        }
    }

    class TableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
            return c;
        }
    }

    class AccountButtonsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(bAddAccount)) {
                showTextField();
            } else if (e.getSource().equals(bEditAccount)) {
                if (lAccounts.getSelectedIndex() >= 0) {
                    showTextField();
                    refreshTextField();
                }
            } else if (e.getSource().equals(bRemoveAccount)) {
                if (lAccounts.getSelectedIndex() >= 0) {
                    int response = messageBoxes.removeAccountDialog();
                    if (response == JOptionPane.YES_OPTION) {
                        removeAccount();
                    }
                }
            }
        }

        private void removeAccount() {
            try {
                controller.removeAccount(lAccounts.getSelectedIndex());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            refreshTabAccount();
            refreshTable();
        }

        private void showTextField() {
            if (!tfAccountDescription.isVisible()) {
                tfAccountDescription.setVisible(true);
                refreshTabAccount();
            }
        }

        public void refreshTextField() {
            tfAccountDescription.setText(controller.getAccount(lAccounts.getSelectedIndex()));
        }
    }

    //todo проверка на вводимые названия счетов
    class AccountTextFieldListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                String description = tfAccountDescription.getText();
                if (lAccounts.getSelectedIndex() >= 0) {
                    try {
                        controller.editAccount(lAccounts.getSelectedIndex(), tfAccountDescription.getText());
                        tfAccountDescription.setText("");
                        tfAccountDescription.setVisible(false);
                        refreshTabAccount();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        controller.addAccount(description);
                        tfAccountDescription.setText("");
                        tfAccountDescription.setVisible(false);
                        refreshTabAccount();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    class CategoryButtonsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(bAddCategory)) {
                currentCategoryIndex = -1;
                showTextField();
            } else if (lCategories.getSelectedIndex() == -1) {
                return;
            } else if (lCategories.getSelectedValue().equalsIgnoreCase(WindowController.NO_CATEGORY_DESCRIPTION)) {
                messageBoxes.errorEditNoCategory();
            } else if (e.getSource().equals(bEditCategory)) {
                refreshTextField();
                showTextField();
            } else if (e.getSource().equals(bRemoveCategory)) {
                int[] indexes = lCategories.getSelectedIndices();
                int response = messageBoxes.confirmationRemoveCategory();
                if (response == JOptionPane.YES_OPTION) {
                    for (int i = indexes.length - 1; i >= 0; i--) {
                        removeCategory(indexes[i]);
                    }
                }
            }
            if (currentCategoryIndex >= 0) {
                lCategories.setSelectedIndex(currentCategoryIndex);
            }
            refreshPanelRecordEdit(controller.getCurrentRecordID());
        }

        private void removeCategory(int index) {
            try {
                controller.removeCategory(index);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            refreshTabCategory();
            refreshTable();
        }

        private void showTextField() {
            if (!tfCategoryDescription.isVisible()) {
                tfCategoryDescription.setVisible(true);
                refreshTabCategory();
            }
        }

        public void refreshTextField() {
            if (lCategories.getSelectedIndex() == -1) {
                return;
            }
            try {
                tfCategoryDescription.setText(controller.getCategory(lCategories.getSelectedIndex()));
            } catch (OperationNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    class CategoryTextFieldListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                if (currentCategoryIndex >= 0) {
                    try {
                        controller.editCategory(lCategories.getSelectedIndex(), tfCategoryDescription.getText());
                        tfCategoryDescription.setText("");
                        tfCategoryDescription.setVisible(false);
                        refreshTabCategory();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        if (tfCategoryDescription.getText().equalsIgnoreCase(WindowController.NO_CATEGORY_DESCRIPTION)) {
                            messageBoxes.errorEditNoCategory();
                            return;
                        }
                        controller.addCategory(tfCategoryDescription.getText());
                        tfCategoryDescription.setText("");
                        tfCategoryDescription.setVisible(false);
                        refreshTabCategory();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                refreshPanelRecordEdit(controller.getCurrentRecordID());
            }
        }
    }

    private class MessageBoxes {
        void errorEditNoCategory() {
            JOptionPane.showMessageDialog(thisFrame, "Категорию \"Без категории\" редактировать нельзя", "", JOptionPane.ERROR_MESSAGE);
        }

        int removeAccountDialog() {
            return JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите удалить данный счет",
                    "Подтверждение удаления счета",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }

        void errorDateIncorrect() {
            JOptionPane.showMessageDialog(thisFrame, "Некорректная дата", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        }

        void warningChooseAccount() {
            JOptionPane.showMessageDialog(thisFrame, "Выберите счет, чтобы сохранить в него новую запись", "Счет не выбран", JOptionPane.WARNING_MESSAGE);
        }

        int confirmationRemoveCategory() {
            return JOptionPane.showConfirmDialog(null, "Вы уверены, что хотите удалить категории/категорию",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
    }
}




