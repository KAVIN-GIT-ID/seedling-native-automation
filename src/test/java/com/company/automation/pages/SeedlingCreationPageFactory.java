package com.company.automation.pages;

import com.company.automation.driver.DriverManager;

public final class SeedlingCreationPageFactory {

    private SeedlingCreationPageFactory() {}

    public static ISeedlingCreationPage create() {
        return DriverManager.getPlatform().equals("ios")
                ? new com.company.automation.pages.ios.SeedlingCreationPage()
                : new com.company.automation.pages.android.SeedlingCreationPage();
    }
}
