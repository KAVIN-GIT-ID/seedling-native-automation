package com.company.automation.pages;

public interface ILoginPage {
    default void handlePermissionIfPresent() {}
    void enterUsername(String username);
    void enterPassword(String password);
    void tapLogin();
    boolean isErrorDisplayed();
    String getErrorText();
}
