package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static UserSession instance;
    private String userName;
    private String password;
    private String privileges;

    // Constructor to initialize user data
    public UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
    }

    // Get the current instance, or create a new one if needed
    public static UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            instance = new UserSession(userName, password, privileges);
        }
        return instance;
    }

    public static UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    // Save user data to Preferences
    public void saveUserData() {
        Preferences userPreferences = Preferences.userRoot();
        userPreferences.put("USERNAME", this.userName);
        userPreferences.put("PASSWORD", this.password);
        userPreferences.put("PRIVILEGES", this.privileges);
    }

    // Getter for userName
    public String getUserName() {
        return this.userName;
    }

    // Getter for password
    public String getPassword() {
        return this.password;
    }

    // Getter for privileges
    public String getPrivileges() {
        return this.privileges;
    }

    // Method to clear the session data
    public void cleanUserSession() {
        this.userName = "";
        this.password = "";
        this.privileges = "";
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges='" + this.privileges + '\'' +
                '}';
    }
}
