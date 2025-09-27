package frames;

import app.App;
import material.MatButton;
import material.MatComboBox;
import material.MatPasswordField;
import material.MatTextField;
import utilities.Database;
import utilities.Theme;
import utilities.EmailSender;

import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.prefs.Preferences;

public class Welcome extends JFrame {

    private final JPanel leftPanel, rightPanel, signInPanel, signUpPanel, verifyPanel;
    private int spacing;
    private String currentUsername;
    private String currentEmail;
    private JCheckBox rememberDevice; // Moved to class level
    private static final Preferences prefs = Preferences.userNodeForPackage(Welcome.class);

    public Welcome() {
        setTitle(App.getTitle());
        setIconImage(App.getIcon());
        setLayout(new GridLayout(1, 2));

        // welcome panel
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        JLabel welcome = new JLabel(new ImageIcon(getWelcomeImage()));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(welcome);

        rightPanel = new JPanel(new CardLayout());

        // sign in panel
        signInPanel = buildSignInPanel();

        //sign up panel
        signUpPanel = buildSignUpPanel();

        //Verification Panel
        verifyPanel = buildVerifyPanel();

        rightPanel.add(signInPanel, "signIn");
        rightPanel.add(signUpPanel, "signUp");
        rightPanel.add(verifyPanel, "verify");

        showCard("signIn");

        add(leftPanel);
        add(rightPanel);

        setupWindow();
        Database.checkConnection();
    }

    private String generateDeviceToken() {
        Random random = new Random();
        return Long.toHexString(random.nextLong()) + Long.toHexString(random.nextLong());
    }

    private void storeDeviceToken(String token) {
        prefs.put("device_token", token);
    }

    private String getStoredDeviceToken() {
        return prefs.get("device_token", null);
    }

    private JPanel buildSignInPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = getConstraints();

        spacing = 0;
        addLabel(panel, gbc, 50, 50, "Welcome!", 50f, Theme.LIGHT_BLUE.color);
        addLabel(panel, gbc, 0, 50, "Sign in to your account", 30f, Theme.DARK_BLUE.color);

        MatTextField txtUsername = addTextField(panel, gbc, 50, "Username or Email");
        MatPasswordField txtPassword = addPasswordField(panel, gbc, 50);
        rememberDevice = new JCheckBox("Remember this device"); // Initialize here
        rememberDevice.setFont(App.getFont().deriveFont(15f));
        rememberDevice.setBackground(Color.WHITE);
        addSpacing(gbc, 0, 10);
        panel.add(rememberDevice, gbc);

        MatButton btnSignIn = addButton(panel, gbc, 30, "Sign In", Theme.LIGHT_BLUE.color, Color.WHITE);
        JLabel lblError = addLabel(panel, gbc, 0, 40, " ", 17f, Theme.RED.color);

        btnSignIn.addActionListener(e -> {
            String usernameOrEmail = txtUsername.getText();
            String password = String.valueOf(txtPassword.getPassword());
            if (validateSignIn(usernameOrEmail, password, lblError)) {
                new Dashboard(currentUsername);
                dispose();
            }
        });

        addLabel(panel, gbc, 0, 20, "Don't have an account?", 20f, Theme.DARK_BLUE.color);
        MatButton btnShowSignUp = addButton(panel, gbc, 50, "Sign Up", Color.WHITE, Theme.LIGHT_BLUE.color);
        btnShowSignUp.addActionListener(e -> showCard("signUp"));

        return panel;
    }

    private JPanel buildSignUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = getConstraints();

        spacing = 0;
        addLabel(panel, gbc, 0, 20, "Hey there!", 50f, Theme.LIGHT_BLUE.color);
        addLabel(panel, gbc, 0, 35, "Sign up to get started", 30f, Theme.DARK_BLUE.color);

        MatTextField txtUsername = addTextField(panel, gbc, 25, "Username");
        MatTextField txtEmail = addTextField(panel, gbc, 25, "Email");
        MatPasswordField txtPassword = addPasswordField(panel, gbc, 25);

        spacing += 100;
        gbc.gridy = spacing;
        MatTextField txtHeight = addMiniTextField(panel, gbc, 0, 330, "Height (cm)");
        MatTextField txtWeight = addMiniTextField(panel, gbc, 120, 0, "Weight (kg)");

        spacing += 100;
        gbc.gridy = spacing;
        MatComboBox<String> cmbBirthYear = addMiniComboBox(panel, gbc, 0, 330, getYears());
        MatComboBox<String> cmbGender = addMiniComboBox(panel, gbc, 120, 0, getGenders());

        MatButton btnSignUp = addButton(panel, gbc, 20, "Sign Up", Theme.LIGHT_BLUE.color, Color.WHITE);
        JLabel lblError = addLabel(panel, gbc, 0, 20, " ", 17f, Theme.RED.color);

        btnSignUp.addActionListener(e -> {
            String username = txtUsername.getText();
            String email = txtEmail.getText();
            String password = String.valueOf(txtPassword.getPassword());
            String height = txtHeight.getText();
            String weight = txtWeight.getText();
            String birthYear = String.valueOf(cmbBirthYear.getSelectedItem());
            String gender = String.valueOf(cmbGender.getSelectedItem());

            if (validateSignUp(username, email, password, height, weight, birthYear, gender, lblError)) {
                currentUsername = username;
                currentEmail = email;
                String code = generateVerificationCode();

                try {
                    Database.executeUpdate("UPDATE users SET verification_code = '" + code + "' WHERE username = '" + username + "'");
                    EmailSender.sendVerificationEmail(email, code);
                    showCard("verify");
                } catch (Exception ex) {
                    lblError.setText("Failed to send verification email");
                }
            }
        });

        addLabel(panel, gbc, 0, 15, "Already have an account?", 20f, Theme.DARK_BLUE.color);
        MatButton btnShowSignIn = addButton(panel, gbc, 30, "Sign In", Color.WHITE, Theme.LIGHT_BLUE.color);
        btnShowSignIn.addActionListener(e -> showCard("signIn"));

        return panel;
    }

    private JPanel buildVerifyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = getConstraints();

        spacing = 0;
        addLabel(panel, gbc, 50, 50, "Verify Your Email", 50f, Theme.LIGHT_BLUE.color);
        JLabel lblEmail = addLabel(panel, gbc, 0, 50, "Code sent to " + (currentEmail != null ? maskEmail(currentEmail) : "your email"), 30f, Theme.DARK_BLUE.color);

        MatTextField txtCode = addTextField(panel, gbc, 50, "Verification Code");
        MatButton btnVerify = addButton(panel, gbc, 30, "Verify", Theme.LIGHT_BLUE.color, Color.WHITE);
        JLabel lblError = addLabel(panel, gbc, 0, 40, " ", 17f, Theme.RED.color);

        btnVerify.addActionListener(e -> {
            String code = txtCode.getText();
            try {
                ResultSet rs = Database.executeQuery(
                        "SELECT username FROM users WHERE username = '" + currentUsername +
                                "' AND verification_code = '" + code + "'");

                if (rs != null && rs.next()) {
                    if (rememberDevice.isSelected()) {
                        // Generate and store a device token
                        String deviceToken = generateDeviceToken();
                        Database.executeUpdate(
                                "UPDATE users SET device_token = '" + deviceToken +
                                        "' WHERE username = '" + currentUsername + "'");


                        storeDeviceToken(deviceToken);
                    }

                    Database.executeUpdate(
                            "UPDATE users SET verification_code = NULL WHERE username = '" + currentUsername + "'");
                    new Dashboard(currentUsername);
                    dispose();
                } else {
                    lblError.setText("Invalid verification code");
                }
            } catch (Exception ex) {
                lblError.setText("Verification failed");
            }
        });

        MatButton btnResend = addButton(panel, gbc, 20, "Resend Code", Color.WHITE, Theme.LIGHT_BLUE.color);
        btnResend.addActionListener(e -> {
            String newCode = generateVerificationCode();
            try {
                Database.executeUpdate("UPDATE users SET verification_code = '" + newCode + "' WHERE username = '" + currentUsername + "'");
                EmailSender.sendVerificationEmail(currentEmail, newCode);
                lblError.setText("New code sent!");
            } catch (Exception ex) {
                lblError.setText("Failed to resend code");
            }
        });

        return panel;
    }

    private boolean validateSignIn(String usernameOrEmail, String password, JLabel error) {
        try {
            String storedToken = getStoredDeviceToken();
            if (storedToken != null) {
                ResultSet tokenMatch = Database.executeQuery(
                        "SELECT username FROM users WHERE username = BINARY '" + usernameOrEmail +
                                "' AND device_token = '" + storedToken + "'");

                if (tokenMatch != null && tokenMatch.next()) {
                    currentUsername = usernameOrEmail;
                    new Dashboard(currentUsername);
                    dispose();
                    return true;
                }
            }

            if (Objects.equals(usernameOrEmail, "")) {
                error.setText("Username or email required");
                return false;
            } else if (Objects.equals(password, "")) {
                error.setText("Password required");
                return false;
            }

            boolean isEmail = usernameOrEmail.contains("@");
            String query;

            if (isEmail) {
                if (!Pattern.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", usernameOrEmail)) {
                    error.setText("Invalid email format");
                    return false;
                }
                query = "SELECT username FROM users WHERE email = BINARY '" + usernameOrEmail + "'";
            } else {
                if ((usernameOrEmail.length() < 5) || (usernameOrEmail.length() > 15)) {
                    error.setText("Username must be between 5 and 15 characters");
                    return false;
                } else if (!Pattern.matches("^[A-Za-z0-9]{5,15}$", usernameOrEmail)) {
                    error.setText("Username must be alphanumeric");
                    return false;
                }
                query = "SELECT username FROM users WHERE username = BINARY '" + usernameOrEmail + "'";
            }

            ResultSet matchedUser = Database.executeQuery(query);
            assert matchedUser != null;
            if (matchedUser.next()) {
                currentUsername = matchedUser.getString("username");

                ResultSet matchedPassword = Database.executeQuery(
                        "SELECT username FROM users WHERE username = BINARY '" + currentUsername +
                                "' AND password = '" + password + "'");

                assert matchedPassword != null;
                if (matchedPassword.next()) {
                    // Generate and send new verification code each login
                    String code = generateVerificationCode();
                    try {
                        Database.executeUpdate("UPDATE users SET verification_code = '" + code +
                                "' WHERE username = '" + currentUsername + "'");

                        // Get user's email to send the code
                        ResultSet emailRS = Database.executeQuery(
                                "SELECT email FROM users WHERE username = '" + currentUsername + "'");
                        if (emailRS.next()) {
                            currentEmail = emailRS.getString("email");
                            EmailSender.sendVerificationEmail(currentEmail, code);
                            showCard("verify");
                            return false;
                        }
                    } catch (Exception ex) {
                        error.setText("Failed to send verification code");
                        return false;
                    }
                } else {
                    error.setText("Invalid password");
                    return false;
                }
            } else {
                error.setText(isEmail ? "Email doesn't exist" : "Username doesn't exist");
                return false;
            }
        } catch (Exception exception) {
            Database.showError("Database error: " + exception.getMessage());
            return false;
        }
        return false;
    }

    private boolean validateSignUp(String username, String email, String password, String height,
                                   String weight, String birthYear, String gender, JLabel error) {
        try {
            if (Objects.equals(username, "")) {
                error.setText("Username required");
                return false;
            } else if (Objects.equals(email, "")) {
                error.setText("Email required");
                return false;
            } else if (Objects.equals(password, "")) {
                error.setText("Password required");
                return false;
            } else if (Objects.equals(height, "")) {
                error.setText("Height required");
                return false;
            } else if (Objects.equals(weight, "")) {
                error.setText("Weight required");
                return false;
            } else if (Objects.equals(birthYear, "Birth year")) {
                error.setText("Birth year required");
                return false;
            } else if (Objects.equals(gender, "Gender")) {
                error.setText("Gender required");
                return false;
            } else if ((username.length() < 5) || (username.length() > 15)) {
                error.setText("Username must be between 5 and 15 characters");
                return false;
            } else if (!Pattern.matches("^[A-Za-z0-9]{5,15}$", username)) {
                error.setText("Username must be alphanumeric");
                return false;
            } else if (!Pattern.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", email)) {
                error.setText("Invalid email format");
                return false;
            } else if ((Integer.parseInt(height) < 10) || (Integer.parseInt(height) > 250)) {
                error.setText("Height must be between 10 and 250");
                return false;
            } else if ((Integer.parseInt(weight) < 10) || (Integer.parseInt(weight) > 250)) {
                error.setText("Weight must be between 10 and 250");
                return false;
            }
        } catch (Exception exception) {
            error.setText("Invalid height or weight");
            return false;
        }

        try {
            ResultSet matchedUsername = Database.executeQuery("SELECT username FROM users WHERE username = BINARY '" + username + "'");
            assert matchedUsername != null;
            if (matchedUsername.next()) {
                error.setText("Username already exists");
                return false;
            }

            ResultSet matchedEmail = Database.executeQuery("SELECT email FROM users WHERE email = BINARY '" + email + "'");
            assert matchedEmail != null;
            if (matchedEmail.next()) {
                error.setText("Email already registered");
                return false;
            }

            int updatedRows = Database.executeUpdate(
                    "INSERT INTO users (username, email, height, weight, birthYear, gender, password, verified) " +
                            "VALUES ('" + username + "', '" + email + "', " + height + ", " + weight + ", " +
                            birthYear + ", '" + gender + "', '" + password + "', 0)");

            return updatedRows > 0;
        } catch (Exception exception) {
            Database.showError("Database error: " + exception.getMessage());
            return false;
        }
    }


    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return email;

        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return "*".repeat(name.length()) + "@" + domain;
        }

        String masked = name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
        return masked + "@" + domain;
    }

    private MatTextField addTextField(JPanel panel, GridBagConstraints constraints, int bottom, String label) {
        addSpacing(constraints, 0, bottom);
        MatTextField textField = new MatTextField();
        textField.setLabel(label);
        textField.setPreferredSize(new Dimension(420, 55));
        textField.setFont(App.getFont().deriveFont(20f));
        panel.add(textField, constraints);
        return textField;
    }

    private MatPasswordField addPasswordField(JPanel panel, GridBagConstraints constraints, int bottom) {
        addSpacing(constraints, 0, bottom);

        MatPasswordField passwordField = new MatPasswordField();
        passwordField.setLabel("Password");
        passwordField.setPreferredSize(new Dimension(420, 55));
        passwordField.setFont(App.getFont().deriveFont(20f));
        passwordField.setEchoChar('•');

        MatButton showHide = new MatButton();
        showHide.setText("Show");
        showHide.setBorderRadius(13);
        showHide.setFont(App.getFont().deriveFont(15f));
        showHide.setBackground(Color.WHITE);
        showHide.setForeground(Theme.LIGHT_BLUE.color);
        showHide.setType(MatButton.ButtonType.FLAT);
        showHide.setPreferredSize(new Dimension(60, 35));

        showHide.addActionListener(event -> {
            if (Objects.equals(showHide.getText(), "Show")) {
                showHide.setText("Hide");
                passwordField.setEchoChar((char) 0);
            } else {
                showHide.setText("Show");
                passwordField.setEchoChar('•');
            }
        });

        constraints.insets = new Insets(0, 250, bottom + 4, 0);
        panel.add(showHide, constraints);
        constraints.insets = new Insets(0, 0, bottom, 100);
        panel.add(passwordField, constraints);

        return passwordField;
    }

    private MatButton addButton(JPanel panel, GridBagConstraints constraints, int bottom, String text, Color background, Color foreground) {
        addSpacing(constraints, 0, bottom);
        MatButton button = new MatButton();
        button.setText(text);
        button.setBorderRadius(13);
        button.setFont(App.getFont().deriveFont(20f));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setPreferredSize(new Dimension(420, 50));
        panel.add(button, constraints);
        return button;
    }

    private MatTextField addMiniTextField(JPanel panel, GridBagConstraints constraints, int left, int right, String label) {
        constraints.insets = new Insets(0, left, 25, right);
        MatTextField textField = new MatTextField();
        textField.setLabel(label);
        textField.setPreferredSize(new Dimension(190, 55));
        textField.setFont(App.getFont().deriveFont(20f));
        panel.add(textField, constraints);
        return textField;
    }

    private MatComboBox<String> addMiniComboBox(JPanel panel, GridBagConstraints constraints, int left, int right, String[] list) {
        constraints.insets = new Insets(0, left, 40, right);
        MatComboBox<String> comboBox = new MatComboBox<>();
        comboBox.setModel(new DefaultComboBoxModel<>(list));
        comboBox.setPreferredSize(new Dimension(190, 45));
        comboBox.setFont(App.getFont().deriveFont(20f));
        panel.add(comboBox, constraints);
        return comboBox;
    }

    private JLabel addLabel(JPanel panel, GridBagConstraints constraints, int top, int bottom, String text, float fontSize, Color color) {
        addSpacing(constraints, top, bottom);
        JLabel label = new JLabel(text);
        label.setFont(App.getFont().deriveFont(fontSize));
        label.setForeground(color);
        panel.add(label, constraints);
        return label;
    }

    private void showCard(String cardName) {
        CardLayout cl = (CardLayout) rightPanel.getLayout();
        cl.show(rightPanel, cardName);
    }

    private void addSpacing(GridBagConstraints constraints, int top, int bottom) {
        spacing += 100;
        constraints.gridy = spacing;
        constraints.insets = new Insets(top, 0, bottom, 100);
    }

    private GridBagConstraints getConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = constraints.gridheight = 100;
        constraints.gridx = 0;
        return constraints;
    }

    private Image getWelcomeImage() {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/cover.png")))
                .getImage()
                .getScaledInstance(-1, (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 1.1), Image.SCALE_SMOOTH);
    }

    private String[] getGenders() {
        return new String[]{"Gender", "Male", "Female"};
    }

    private String[] getYears() {
        int minAge = 5;
        int maxAge = 100;
        String[] years = new String[maxAge - minAge + 2];
        years[0] = "Birth year";
        int year = Calendar.getInstance().get(Calendar.YEAR) - minAge;
        for (int i = 1; i < years.length; i++) {
            years[i] = Integer.toString(year);
            year--;
        }
        return years;
    }

    private void setupWindow() {
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        leftPanel.setBackground(Color.WHITE);
        rightPanel.setBackground(Color.WHITE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Welcome();
    }
}