package com.company.automation.pages;

public interface ILoginPage {
    default void handlePermissionIfPresent() {}
    void enterUsername(String username);
    void enterPassword(String password);
    void tapLogin();
    void tapGoogleSignIn();
    void tapAppleSignIn();
    void tapXSignIn();
    boolean isErrorDisplayed();
    String getErrorText();
}
