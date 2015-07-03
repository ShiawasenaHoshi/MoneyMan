package View;

import Controller.Controller;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

    public MainForm(Controller controller) {
        super("Главная форма");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

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
}
