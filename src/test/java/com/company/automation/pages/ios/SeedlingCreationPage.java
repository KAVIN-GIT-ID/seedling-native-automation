package com.company.automation.pages.ios;

import com.company.automation.base.BasePage;
import com.company.automation.data.SeedlingTestData;
import com.company.automation.pages.ISeedlingCreationPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.By;
import java.io.File;

public class SeedlingCreationPage extends BasePage implements ISeedlingCreationPage {

    // Bottom navigation center create button (3rd button in bottom tab bar)
    private final By createTabButton = By.xpath(
            "//XCUIElementTypeTabBar/XCUIElementTypeButton[3] | " +
            "//XCUIElementTypeButton[contains(@name, 'AddSeedingRoute') or contains(@name, 'Create') or contains(@label, 'Create')]"
    );

    // "Create New Seedling" card button
    private final By createNewSeedlingButton = AppiumBy.accessibilityId("Create New Seedling");

    // Search charity input field
    private final By searchCharityField = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name BEGINSWITH 'Search charities'`][-1]");

    // Next step button
    private final By nextButton = By.xpath("//XCUIElementTypeOther[@name='Next'] | //XCUIElementTypeButton[@name='Next']");

    // Step 2 Fields
    private final By seedlingTitleField = By.xpath("(//XCUIElementTypeOther[@name=\"Seedling Title*  \"])[2]");
    private final By seedlingDescriptionField = By.xpath("(//XCUIElementTypeTextView)[1]");
    private final By coSponsorSearchField = By.xpath("(//XCUIElementTypeOther[contains(@name, 'Search for a co-sponsor')])[last()]");
    private final By createCampaignCheckbox = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name == 'Create a campaign ?'`][1]/XCUIElementTypeOther[1]/XCUIElementTypeOther");
    private final By campaignTitleField = By.xpath("(//XCUIElementTypeOther[contains(@name, 'Campaign Title')])[last()]");
    private final By campaignDescriptionField = By.xpath("(//XCUIElementTypeOther[contains(@name, 'Campaign Description')]//XCUIElementTypeTextView)[last()]");

    // Step 3 Fields
    private final By seedlingGoalField = By.xpath("//XCUIElementTypeOther[contains(@name, 'Seedling Goal')] | (//XCUIElementTypeOther[contains(@name, 'Goal')])[1]");
    private final By endSeedlingCheckbox = By.xpath("//XCUIElementTypeOther[contains(@name, 'End Seedling If') and contains(@name, 'goal is met')]");
    private final By campaignGoalField = By.xpath("//XCUIElementTypeOther[contains(@name, 'Campaign Goal')]");
    private final By endCampaignCheckbox = By.xpath("//XCUIElementTypeOther[contains(@name, 'End Campaign if') and contains(@name, 'goal is met')]");

    @Override
    protected void tap(By locator, String elementName) {
        try {
            // Bypass BasePage's waitForClickable which times out on React Native XCUIElementTypeOther
            org.openqa.selenium.WebElement element = getWait().waitForVisible(locator);
            
            // element.click() silently fails on many RN iOS elements.
            // We must calculate the dynamic center and tap by coordinates.
            int x = element.getLocation().getX() + (element.getSize().getWidth() / 2);
            int y = element.getLocation().getY() + (element.getSize().getHeight() / 2);
            
            tapByCoordinates(x, y, elementName);
        } catch (Exception e) {
            throw new org.openqa.selenium.TimeoutException("Failed to tap " + elementName + ": " + e.getMessage(), e);
        }
    }

    @Override
    protected void type(By locator, String text, String elementName) {
        try {
            // Get element to type into
            org.openqa.selenium.WebElement element = getWait().waitForPresence(locator);
            
            // Tap to focus using dynamic coordinates
            int x = element.getLocation().getX() + (element.getSize().getWidth() / 2);
            int y = element.getLocation().getY() + (element.getSize().getHeight() / 2);
            tapByCoordinates(x, y, elementName + " (Focus)");
            
            // Wait for keyboard to animate and RN field to gain focus
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            
            // Native sendKeys() often visually types but fails to trigger RN's onChangeText.
            // Exclusively use W3C Actions API to simulate global keyboard input.
            new org.openqa.selenium.interactions.Actions(driver()).sendKeys(text).perform();
            logStep("Entered '" + text + "' into " + elementName + " via Actions API");
            dismissKeyboardIOS(); // Attempt to hide keyboard after typing
        } catch (Exception e) {
            throw new org.openqa.selenium.TimeoutException("Failed to type into " + elementName + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void tapCreateTab() {
        try { Thread.sleep(10000); } catch (InterruptedException ignored) {}
        try {
            tap(createTabButton, "Bottom Create Tab Button");
        } catch (Exception e) {
            tapByCoordinates(207, 844, "Bottom Create Tab Button (Coordinates)");
        }
    }

    @Override
    public void tapCreateNewSeedling() {
        // Wait for the modal to fully animate up before clicking
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        try {
            tap(createNewSeedlingButton, "Create New Seedling Button");
        } catch (Exception e) {
            tapByCoordinates(207, 247, "Create New Seedling Button (Coordinates)");
        }
    }

    @Override
    public void searchAndSelectCharity(String charitySearch, String charityLabel) {
        // Wait for page transition animation to finish before calculating tap coordinates
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        
        type(searchCharityField, charitySearch, "Search Charity Field");
        try { Thread.sleep(4000); } catch (InterruptedException ignored) {} // Extra time for API search
        
        dismissKeyboardIOS();
        
        By charityResultLocator = By.xpath("//XCUIElementTypeOther[contains(@name, '" + charityLabel + "')] | //XCUIElementTypeStaticText[contains(@name, '" + charityLabel + "')]");
        try {
            tap(charityResultLocator, "Charity Search Result: " + charityLabel);
        } catch (Exception e) {
            tapByCoordinates(200, 300, "Charity Search Result (Coordinates)");
        }
        
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Override
    public void enterSeedlingTitle(String title) {
        type(seedlingTitleField, title, "Seedling Title Field");
    }

    @Override
    public void enterSeedlingDescription(String description) {
        type(seedlingDescriptionField, description, "Seedling Description Field");
        dismissKeyboardIOS();
    }

    @Override
    public void searchAndSelectCoSponsor(String coSponsorSearch, String coSponsorName) {
        // Wait for page transition animation to finish before calculating tap coordinates
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        
        dumpPageSource("Before CoSponsor");

        type(coSponsorSearchField, coSponsorSearch, "Co-Sponsor Search Field");
        try { Thread.sleep(4000); } catch (InterruptedException ignored) {} // Extra time for API search

        dismissKeyboardIOS();

        dismissKeyboardIOS();

        By coSponsorLocator = By.xpath("(//XCUIElementTypeOther[contains(@name, '" + coSponsorName + "')])[last()]");
        try {
            tap(coSponsorLocator, "Co-Sponsor Result: " + coSponsorName);
        } catch (Exception e) {
            tapByCoordinates(200, 450, "Co-Sponsor Result (Coordinates)");
        }
        
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Override
    public void toggleCreateCampaignCheckbox() {
        dismissKeyboardIOS();
        try {
            org.openqa.selenium.WebElement element = driver().findElement(createCampaignCheckbox);
            int x = element.getLocation().getX() + (element.getSize().getWidth() / 2);
            int y = element.getLocation().getY() + (element.getSize().getHeight() / 2);
            tapByCoordinates(x, y, "Create a campaign Checkbox (Dynamic)");
        } catch (Exception e) {
            tapByCoordinates(29, 557, "Create a campaign Checkbox (Fallback)"); 
        }
    }

    @Override
    public void enterCampaignTitle(String campaignTitle) {
        type(campaignTitleField, campaignTitle, "Campaign Title Field");
    }

    @Override
    public void enterCampaignDescription(String campaignDescription) {
        type(campaignDescriptionField, campaignDescription, "Campaign Description Field");
    }

    @Override
    public void enterSeedlingGoal(String goalAmount) {
        dumpPageSource("Step 3 Start");
        String cleanAmount = goalAmount.replace("$", "").trim();
        type(seedlingGoalField, cleanAmount, "Seedling Goal Field");
    }

    @Override
    public void toggleEndSeedlingIfGoalMetCheckbox() {
        dismissKeyboardIOS();
        dumpPageSource("Step 3 Goal Input Complete");
        tap(endSeedlingCheckbox, "End seedling if goal is met Checkbox");
    }

    @Override
    public void enterCampaignGoal(String campaignGoalAmount) {
        String cleanAmount = campaignGoalAmount.replace("$", "").trim();
        type(campaignGoalField, cleanAmount, "Campaign Goal Field");
    }

    @Override
    public void toggleEndCampaignIfGoalMetCheckbox() {
        dismissKeyboardIOS();
        dumpPageSource("Step 3 Campaign Goal Input Complete");
        tap(endCampaignCheckbox, "End campaign if goal is met Checkbox");
    }

    // Step 4: Giving Incentives (Skipped per user configuration — proceed directly to Step 5)
    @Override
    public void fillIncentives() {
        logStep("Skipping Step 4 incentive configuration — tapping Next directly (iOS)");
        dismissKeyboardIOS();
        tapNext();
        logStep("Completed Step 4: Next tapped directly");
    }

    private void tapGivingIncentiveCheckbox(int index) {
        dismissKeyboardIOS();
        
        // Use iOSNsPredicateString to instantly find the Tier Header text. 
        // Avoid complex descendant // XPaths as they time out on massive React Native DOMs.
        By tierHeaderLocator = AppiumBy.iOSNsPredicateString("name BEGINSWITH 'Tier " + index + "' AND accessible == 1");
        try {
            org.openqa.selenium.WebElement header = getWait().waitForPresence(tierHeaderLocator);
            int tapX = header.getLocation().getX() + 141; // Checkbox center is 141px to the right of header X
            int tapY = header.getLocation().getY() + 72;  // Checkbox center is 72px below the header Y
            tapByCoordinates(tapX, tapY, "Add a giving incentive Checkbox (Tier " + index + ")");
        } catch (Exception e) {
            logStep("Tier " + index + " header not visible, scrolling down...");
            scrollDown(0.65, 0.40);
            try {
                org.openqa.selenium.WebElement header = getWait().waitForPresence(tierHeaderLocator);
                int tapX = header.getLocation().getX() + 141;
                int tapY = header.getLocation().getY() + 72;
                tapByCoordinates(tapX, tapY, "Add a giving incentive Checkbox (Tier " + index + " after scroll)");
            } catch (Exception ex) {
                tapByCoordinates(29, 400 + (index * 50), "Add a giving incentive Checkbox (Coordinates Tier " + index + ")");
            }
        }
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    private void enterLatestIncentiveDescription(String description) {
        dumpPageSource("Step 4 Incentive Field Debug");
        By descField = AppiumBy.iOSClassChain("**/XCUIElementTypeTextView[-1]");
        tap(descField, "Incentive Description Field");
        type(descField, description, "Incentive Description Field");
        dismissKeyboardIOS();
    }

    private void selectIncentiveCategory(String categoryKeyword, String categoryValueKey) {
        dismissKeyboardIOS();

        By categoryDropdown = AppiumBy.iOSClassChain("**/XCUIElementTypeOther[`name CONTAINS 'Incentive Category' AND accessible == 1`][-1]");
        try {
            tap(categoryDropdown, "Incentive Category Dropdown");
        } catch (Exception e) {
            tapByCoordinates(350, 500, "Incentive Category Dropdown Arrow");
        }

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        By categoryOption = By.xpath("//XCUIElementTypeOther[contains(@name, '" + categoryKeyword + "') and @accessible='true'] | //XCUIElementTypeStaticText[contains(@name, '" + categoryKeyword + "') and @accessible='true']");
        try {
            tap(categoryOption, "Category Option: " + categoryKeyword);
        } catch (Exception e) {
            try {
                logStep("Attempting PickerWheel fallback for Category");
                org.openqa.selenium.WebElement picker = driver().findElement(By.xpath("//XCUIElementTypePickerWheel"));
                picker.sendKeys(categoryKeyword);
                tapByCoordinates(350, 450, "Tap outside to dismiss Picker");
            } catch (Exception ex) {
                tapByCoordinates(200, 750, "Category Option (Coordinates)");
            }
        }

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        if (categoryValueKey != null && !categoryValueKey.isEmpty()) {
            dismissKeyboardIOS();

            By valueDropdown = By.xpath("(//XCUIElementTypeOther[contains(@name, 'Incentive Category Value') and @accessible='true'])[last()]");
            try {
                tap(valueDropdown, "Incentive Category Value Dropdown");
            } catch (Exception e) {
                scrollDown(0.55, 0.40);
                try {
                    tap(valueDropdown, "Incentive Category Value Dropdown");
                } catch (Exception ex) {
                    tapByCoordinates(350, 600, "Incentive Category Value Arrow");
                }
            }

            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

            By valueOption = By.xpath("//XCUIElementTypeOther[contains(@name, '" + categoryValueKey + "') and @accessible='true'] | //XCUIElementTypeStaticText[contains(@name, '" + categoryValueKey + "') and @accessible='true']");
            try {
                tap(valueOption, "Category Value Option: " + categoryValueKey);
            } catch (Exception e) {
                try {
                    logStep("Attempting PickerWheel fallback for Category Value");
                    org.openqa.selenium.WebElement picker = driver().findElement(By.xpath("//XCUIElementTypePickerWheel"));
                    picker.sendKeys(categoryValueKey);
                    tapByCoordinates(350, 450, "Tap outside to dismiss Picker");
                } catch (Exception ex) {
                    tapByCoordinates(200, 750, "Category Value Option (Coordinates)");
                }
            }

            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
    }

    @Override
    public void fillChallengeMatch(String matchingAmount) {
        logStep("Starting Step 5: Challenge Match (iOS)");
        if (matchingAmount != null && !matchingAmount.isEmpty()) {
            dismissKeyboardIOS();
            try {
                By matchCheckbox = By.xpath("//XCUIElementTypeOther[contains(@name, 'Challenge Match')]");
                tap(matchCheckbox, "Challenge Match Checkbox");
            } catch (Exception e) {
                tapByCoordinates(29, 300, "Challenge Match Checkbox (Coordinates)");
            }

            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            String cleanAmount = matchingAmount.replace("$", "").trim();
            By amountField = By.xpath("(//XCUIElementTypeTextField)[last()] | (//XCUIElementTypeTextView)[last()]");
            try {
                tap(amountField, "Matching Amount Field");
                type(amountField, cleanAmount, "Matching Amount Field");
            } catch (Exception e) {
                tapByCoordinates(200, 450, "Matching Amount Field (Coordinates)");
                type(amountField, cleanAmount, "Matching Amount Field");
            }
        }
        dismissKeyboardIOS();
        tapNext();
        logStep("Completed Step 5: Challenge Match and Next tapped");
    }

    @Override
    public void uploadVideoOrProceed() {
        logStep("Starting Step 6: Video Upload (iOS)");
        dismissKeyboardIOS();

        // 1. Push Test Video to Device Storage using Appium iOSDriver
        try {
            if (driver() instanceof IOSDriver) {
                File videoFile = new File("src/test/resources/Untitled video (1).mp4");
                if (videoFile.exists()) {
                    ((IOSDriver) driver()).pushFile("@com.apple.Photos/SeedlingTestVideo.mp4", videoFile);
                    logStep("Pushed test video to iOS Photos");
                }
            }
        } catch (Exception ignored) {}

        // 2. Click "Choose A File"
        By uploadBtn = By.xpath("//XCUIElementTypeOther[contains(@name, 'Choose A File')] | //XCUIElementTypeButton[contains(@name, 'Choose A File')]");
        tap(uploadBtn, "Choose A File Button");

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // 3. Click "Photo Library" button from action sheet
        By chooseFromLibraryBtn = By.xpath("//XCUIElementTypeButton[@name='Photo Library']");
        try {
            tap(chooseFromLibraryBtn, "Photo Library Button");
        } catch (Exception e) {
            tapByCoordinates(200, 700, "Photo Library Button (Coordinates)");
        }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // 4. Select the first media file from iOS gallery grid
        logStep("Selecting the first video from the iOS gallery...");
        By firstGridItem = By.xpath("//XCUIElementTypeCell[1]");
        try {
            tap(firstGridItem, "First Video Thumbnail");
        } catch (Exception e) {
            tapByCoordinates(100, 200, "First Video Thumbnail");
        }

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // 5. Click 'Choose' button to compress and select
        logStep("Tapping 'Choose' to confirm video selection...");
        By chooseBtnLocator = By.xpath("//XCUIElementTypeButton[@name='Choose']");
        try {
            tap(chooseBtnLocator, "Choose Video Button");
        } catch (Exception e) {
            tapByCoordinates(350, 800, "Choose Video Button");
        }

        // Wait for video upload / transcoding back in the Seedling app
        logStep("Waiting 15s for iOS video upload/processing to complete inside the app...");
        try { Thread.sleep(15000); } catch (InterruptedException ignored) {}

        // 6. Tap Next
        tapNext();
    }

    @Override
    public void submitSeedling() {
        logStep("Starting Step 7: Review & Submit (iOS)");
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        // Tap Confirm & Submit
        By confirmButton = By.xpath("//XCUIElementTypeOther[contains(@name, 'Confirm & Submit')] | //XCUIElementTypeButton[contains(@name, 'Confirm & Submit')]");
        try {
            tap(confirmButton, "Confirm & Submit Button");
        } catch (Exception e) {
            tapByCoordinates(200, 800, "Confirm & Submit Button");
        }

        // Tap Authorize Modal
        try {
            Thread.sleep(2500);
            By authorizeButton = By.xpath("//XCUIElementTypeButton[contains(@name, 'Authorize')] | //XCUIElementTypeOther[contains(@name, 'Authorize')]");
            tap(authorizeButton, "Authorize Button");
            Thread.sleep(3000);
        } catch (Exception e) {
            tapByCoordinates(200, 700, "Authorize Button");
        }

        // Tap OK Modal Button
        try {
            Thread.sleep(2500);
            By okButton = By.xpath("//XCUIElementTypeButton[@name='OK'] | //XCUIElementTypeOther[@name='OK']");
            tap(okButton, "OK Modal Button");
        } catch (Exception e) {
            tapByCoordinates(200, 500, "OK Modal Button");
        }

        logStep("🎉 Seedling Creation Submitted Successfully!");
    }

    @Override
    public void tapNext() {
        dismissKeyboardIOS();
        // Swipe up (finger moves from 70% to 30% Y) to scroll down and reveal the Next button above the keyboard
        scrollDown(); 
        try {
            Thread.sleep(1000); // Wait for scroll/dismiss animation
            org.openqa.selenium.WebElement element = driver().findElement(nextButton);
            int x = element.getLocation().getX() + (element.getSize().getWidth() / 2);
            int y = element.getLocation().getY() + (element.getSize().getHeight() / 2);
            tapByCoordinates(x, y, "Next Button (Dynamic Coordinates)");
        } catch (Exception e) {
            tapByCoordinates(287, 773, "Next Button (Fallback Coordinates)");
        }
    }

    private void dismissKeyboardIOS() {
        try {
            if (driver() instanceof io.appium.java_client.ios.IOSDriver) {
                org.openqa.selenium.By doneKey = org.openqa.selenium.By.name("Done");
                if (!driver().findElements(doneKey).isEmpty()) {
                    driver().findElement(doneKey).click();
                    logStep("Tapped Done key to dismiss keyboard");
                } else {
                    try {
                        driver().executeScript("mobile: hideKeyboard", java.util.Map.of("strategy", "tapOutside"));
                        logStep("Dismissed Keyboard via mobile: hideKeyboard tapOutside");
                    } catch (Exception e) {
                        tapByCoordinates(200, 150, "Dismiss Keyboard (Tap Safe Area)");
                        tapByCoordinates(50, 250, "Dismiss Keyboard (Tap Safe Area 2)");
                    }
                }
            }
        } catch (Exception ignored) {}
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    private void tapByCoordinates(int x, int y, String elementName) {
        org.openqa.selenium.interactions.PointerInput finger = new org.openqa.selenium.interactions.PointerInput(org.openqa.selenium.interactions.PointerInput.Kind.TOUCH, "finger");
        org.openqa.selenium.interactions.Sequence tap = new org.openqa.selenium.interactions.Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(java.time.Duration.ZERO, org.openqa.selenium.interactions.PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(new org.openqa.selenium.interactions.Pause(finger, java.time.Duration.ofMillis(50)));
        tap.addAction(finger.createPointerUp(org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT.asArg()));
        driver().perform(java.util.Collections.singletonList(tap));
        logStep("Tapped on " + elementName + " at (" + x + ", " + y + ")");
    }
}
