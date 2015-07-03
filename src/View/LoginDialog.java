package View;

import Controller.Controller;

import javax.swing.*;
import java.awt.event.*;

public class LoginDialog extends JDialog implements Runnable {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tfLogin;
    private JPasswordField tfPassword;
    private JLabel lError;
    private JLabel lRegistration;

    private Controller controller;

    public LoginDialog(Controller controller) {
        this.controller = controller;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        lError.setVisible(false);
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        lRegistration.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NewUserDialog newUserDialog = new NewUserDialog(controller);
                newUserDialog.pack();
                newUserDialog.setName("Регистрация");
                newUserDialog.setVisible(true);
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        boolean ok = controller.enter(tfLogin.getText(), tfPassword.getPassword());
        if (ok) {
            this.dispose();
        } else {
            lError.setVisible(true);
        }
    }

    private void onCancel() {
        System.exit(0);
    }

    @Override
    public void run() {
        this.pack();
        this.setVisible(true);
//        lError.setVisible(false);
    }

}
