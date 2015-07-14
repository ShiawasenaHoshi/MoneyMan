package View;

import Controller.Controller;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewUserDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tfLogin;
    private JPasswordField tfPassword;
    private JLabel lError;
    private Controller controller;

    public NewUserDialog(Controller controller) {
        this.controller = controller;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void onOK() {
        if (!loginIsOk(tfLogin.getText())) {
            lError.setText("Логин должен состоять из 3-20 латинских символов и букв, первый символ обязательно буква");
            return;
        }
        if (!passwordIsOk(tfPassword.getText())) {
            lError.setText("Пароль должен состоять из латинских букв (строчных и прописных) и цифр");
            return;
        }
        if (controller.userExist(tfLogin.getText())) {
            lError.setText("Такой пользователь уже существует");
            return;
        }
        controller.createNewUser(tfLogin.getText(), tfPassword.getPassword());
        JOptionPane.showMessageDialog(this, String.format("Пользователь %s создан!", tfLogin.getText()),
                "Пользователь создан", JOptionPane.ERROR_MESSAGE);
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private boolean loginIsOk(String name) {
        Pattern p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9-_\\.]{2,20}$");
        Matcher m = p.matcher(name);
        return m.matches();
    }

    //fixme не угодно работать с паролем в стринге
    private boolean passwordIsOk(String password) {
        Pattern p = Pattern.compile("^[a-zA-Z][a-zA-Z0-9-_\\.]{2,20}$");
        Matcher m = p.matcher(password);
        return m.matches();
    }
}
