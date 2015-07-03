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
    private JTable table1;
    private JTabbedPane tabbedPane1;
    private JList list2;
    private JList list1;
    private JSlider slider1;
    private JSlider slider2;
    private JFormattedTextField formattedTextField1;
    private JFormattedTextField formattedTextField2;
    private JSlider slider3;
    private JFormattedTextField formattedTextField3;
    private JFormattedTextField formattedTextField4;
    private JSlider slider4;
    private JFormattedTextField formattedTextField5;
    private JFormattedTextField formattedTextField6;
    private JComboBox comboBox1;
    private JTextArea textArea1;
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
