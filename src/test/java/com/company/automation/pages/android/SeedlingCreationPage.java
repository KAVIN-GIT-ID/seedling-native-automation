package com.company.automation.pages.android;

import com.company.automation.base.BasePage;
import com.company.automation.data.SeedlingTestData;
import com.company.automation.pages.ISeedlingCreationPage;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class SeedlingCreationPage extends BasePage implements ISeedlingCreationPage {

    // Bottom navigation center create button locators
    private final By createTabButton = By.xpath(
            "//android.view.ViewGroup[contains(@bounds, '[432,2244]') or contains(@bounds, '[519,2294]') or @content-desc='Create' or contains(@content-desc, 'plus')] | " +
            "//*[@content-desc='Create' or @text='Create' or contains(@content-desc, 'Create')]"
    );

    // "Create New Seedling" card button locators
    private final By createNewSeedlingButton = AppiumBy.accessibilityId("Create New Seedling");
    private final By createNewSeedlingUiAutomator = AppiumBy.androidUIAutomator(
            "new UiSelector().textContains(\"Create New Seedling\")"
    );
    private final By createNewSeedlingFallback = By.xpath(
            "//*[contains(@content-desc, 'Create New Seedling') or contains(@text, 'Create New Seedling') or contains(@text, 'Seedling')]"
    );

    // Search charity input field
    private final By searchCharityField = By.xpath("//android.widget.EditText");

    // Next step button
    private final By nextButton = By.xpath("//*[@content-desc='Next' or @text='Next']");

    // Step 2 Seedling Top Fields
    private final By seedlingTitleField = By.xpath("(//android.widget.EditText)[1]");
    private final By seedlingDescriptionField = By.xpath("(//android.widget.EditText)[2]");
    private final By coSponsorSearchField = By.xpath("(//android.widget.EditText)[3]");

    // Step 3 Fields
    private final By seedlingGoalField = By.xpath("(//android.widget.EditText)[1]");
    private final By endSeedlingCheckbox = By.xpath(
            "//android.view.ViewGroup[@bounds='[77,682][140,745]' or contains(@bounds, '[77,682]')] | " +
            "//*[contains(@text, 'goal is met') or contains(@text, 'End seedling')]"
    );
    private final By campaignGoalField = By.xpath("(//android.widget.EditText)[2]");
    private final By endCampaignCheckbox = By.xpath(
            "//android.view.ViewGroup[@bounds='[77,1494][140,1557]' or contains(@bounds, '[77,1494]')] | " +
            "//*[contains(@text, 'End campaign') or contains(@text, 'campaign if')]"
    );


    /**
     * Saves a PNG screenshot to logs/<name>_<timestamp>.png for CI diagnostics.
     */
    private void captureDebugSnapshot(String name) {
        try {
            Path dir = Path.of("logs");
            Files.createDirectories(dir);
            String ts = DateTimeFormatter.ofPattern("HHmmss_SSS")
                    .format(LocalDateTime.now());
            Path target = dir.resolve(name + "_" + ts + ".png");
            File src = ((TakesScreenshot) driver()).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), target,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logStep("📸 Screenshot saved: " + target);
            takeScreenshot(name);
        } catch (Exception e) {
            logStep("⚠️ Screenshot capture failed for '" + name + "': " + e.getMessage());
        }
    }

    @Override
    public void tapCreateTab() {
        logStep("Waiting for home screen to stabilize after login...");
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        captureDebugSnapshot("02_home_screen_before_tap_create_tab");

        try {
            tap(createTabButton, "Bottom Create Tab Button");
            logStep("Tapped Bottom Create Tab Button via locator");
        } catch (Exception e) {
            logStep("Locator tap failed for Create Tab, using coordinate fallback...");
            int screenWidth = driver().manage().window().getSize().getWidth();
            int screenHeight = driver().manage().window().getSize().getHeight();
            int x = screenWidth / 2;
            int y = (int) (screenHeight * 0.965);
            tapByCoordinates(x, y, "Bottom Create Tab Button");
        }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        captureDebugSnapshot("03_screen_after_tap_create_tab");
        dumpPageSource("AFTER_TAP_CREATE_TAB");
    }

    @Override
    public void tapCreateNewSeedling() {
        logStep("Attempting to locate and tap 'Create New Seedling' button...");

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 20000) {
            // 1. Try accessibilityId
            try {
                List<WebElement> els = driver().findElements(createNewSeedlingButton);
                if (!els.isEmpty()) {
                    els.get(0).click();
                    logStep("✅ Tapped 'Create New Seedling' via accessibilityId");
                    captureDebugSnapshot("04_tapped_create_new_seedling");
                    return;
                }
            } catch (Exception ignored) {}

            // 2. Try UiAutomator
            try {
                List<WebElement> els = driver().findElements(createNewSeedlingUiAutomator);
                if (!els.isEmpty()) {
                    els.get(0).click();
                    logStep("✅ Tapped 'Create New Seedling' via UiAutomator text");
                    captureDebugSnapshot("04_tapped_create_new_seedling");
                    return;
                }
            } catch (Exception ignored) {}

            // 3. Try Fallback XPath
            try {
                List<WebElement> els = driver().findElements(createNewSeedlingFallback);
                if (!els.isEmpty()) {
                    els.get(0).click();
                    logStep("✅ Tapped 'Create New Seedling' via Fallback XPath");
                    captureDebugSnapshot("04_tapped_create_new_seedling");
                    return;
                }
            } catch (Exception ignored) {}

            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        }

        // If not found after 20 seconds:
        captureDebugSnapshot("FAILED_create_new_seedling_not_found");
        dumpPageSource("FAILURE — Create New Seedling button not found");
        logStep("❌ Failed to find 'Create New Seedling' button. Attempting standard tap as final try...");
        tap(createNewSeedlingButton, "Create New Seedling Button");
    }

    @Override
    public void searchAndSelectCharity(String charitySearch, String charityLabel) {
        tap(searchCharityField, "Search Charity Field");
        type(searchCharityField, charitySearch, "Search Charity Field");

        try {
            Thread.sleep(2500);
        } catch (InterruptedException ignored) {}

        By charityResultLocator = AppiumBy.accessibilityId(charityLabel);
        try {
            tap(charityResultLocator, "Charity Search Result: " + charityLabel);
        } catch (Exception e) {
            By fallbackLocator = By.xpath("//android.view.ViewGroup[contains(@content-desc, '" + charitySearch + "')]");
            tap(fallbackLocator, "Charity Search Result (Fallback): " + charitySearch);
        }

        dismissKeyboardAndroid();
    }

    @Override
    public void enterSeedlingTitle(String title) {
        tap(seedlingTitleField, "Seedling Title Field");
        type(seedlingTitleField, title, "Seedling Title Field");
    }

    @Override
    public void enterSeedlingDescription(String description) {
        tap(seedlingDescriptionField, "Seedling Description Field");
        type(seedlingDescriptionField, description, "Seedling Description Field");
    }

    @Override
    public void searchAndSelectCoSponsor(String coSponsorSearch, String coSponsorName) {
        tap(coSponsorSearchField, "Co-Sponsor Search Field");
        type(coSponsorSearchField, coSponsorSearch, "Co-Sponsor Search Field");

        try {
            Thread.sleep(2500);
        } catch (InterruptedException ignored) {}

        By coSponsorLocator = By.xpath(
                "//*[@content-desc='" + coSponsorName + "' or contains(@content-desc, '" + coSponsorName + "') or contains(@text, '" + coSponsorName + "')]"
        );
        tap(coSponsorLocator, "Co-Sponsor Result: " + coSponsorName);

        dismissKeyboardAndroid();
    }

    @Override
    public void toggleCreateCampaignCheckbox() {
        dismissKeyboardAndroid();

        By campaignLabel = By.xpath("//android.widget.TextView[@text='Create a campaign']");
        By checkboxLocator = By.xpath(
                "//android.view.ViewGroup[@bounds='[77,1570][140,1633]' or contains(@bounds, '[77,1570]')] | " +
                "//android.widget.TextView[@text='Create a campaign']/preceding-sibling::android.view.ViewGroup | " +
                "//android.widget.TextView[@text='Create a campaign']"
        );

        try {
            List<WebElement> elements = driver().findElements(checkboxLocator);
            if (!elements.isEmpty()) {
                elements.get(0).click();
                logStep("Tapped on Create a campaign Checkbox");
            } else {
                tap(campaignLabel, "Create a campaign Label");
            }
        } catch (Exception e) {
            tapByCoordinates(108, 1601, "Create a campaign Checkbox");
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }

    @Override
    public void enterCampaignTitle(String campaignTitle) {
        scrollDown(0.70, 0.30);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        By campaignTitleLocator = By.xpath(
                "//android.widget.EditText[contains(@bounds, '1627')] | (//android.widget.EditText)[last()-1]"
        );
        tap(campaignTitleLocator, "Campaign Title Field");
        type(campaignTitleLocator, campaignTitle, "Campaign Title Field");
    }

    @Override
    public void enterCampaignDescription(String campaignDescription) {
        By campaignDescriptionLocator = By.xpath(
                "//android.widget.EditText[contains(@bounds, '1848')] | (//android.widget.EditText)[last()]"
        );
        tap(campaignDescriptionLocator, "Campaign Description Field");
        type(campaignDescriptionLocator, campaignDescription, "Campaign Description Field");
    }

    // Step 3 Implementations
    @Override
    public void enterSeedlingGoal(String goalAmount) {
        String cleanAmount = goalAmount.replace("$", "").trim();
        tap(seedlingGoalField, "Seedling Goal Field");
        type(seedlingGoalField, cleanAmount, "Seedling Goal Field");
    }

    @Override
    public void toggleEndSeedlingIfGoalMetCheckbox() {
        dismissKeyboardAndroid();
        try {
            List<WebElement> elements = driver().findElements(endSeedlingCheckbox);
            if (!elements.isEmpty()) {
                elements.get(0).click();
                logStep("Tapped on End seedling if goal is met Checkbox");
            } else {
                tapByCoordinates(108, 713, "End seedling if goal is met Checkbox");
            }
        } catch (Exception e) {
            tapByCoordinates(108, 713, "End seedling if goal is met Checkbox");
        }
    }

    @Override
    public void enterCampaignGoal(String campaignGoalAmount) {
        String cleanAmount = campaignGoalAmount.replace("$", "").trim();
        try {
            tap(campaignGoalField, "Campaign Goal Field");
        } catch (Exception e) {
            scrollDown(0.60, 0.35);
            tap(campaignGoalField, "Campaign Goal Field");
        }
        type(campaignGoalField, cleanAmount, "Campaign Goal Field");
    }

    @Override
    public void toggleEndCampaignIfGoalMetCheckbox() {
        dismissKeyboardAndroid();
        try {
            List<WebElement> elements = driver().findElements(endCampaignCheckbox);
            if (!elements.isEmpty()) {
                elements.get(0).click();
                logStep("Tapped on End campaign if goal is met Checkbox");
            } else {
                tapByCoordinates(108, 1525, "End campaign if goal is met Checkbox");
            }
        } catch (Exception e) {
            tapByCoordinates(108, 1525, "End campaign if goal is met Checkbox");
        }
    }

    // Step 4: Giving Incentives
    @Override
    public void fillIncentives() {
        logStep("Starting Step 4: Giving Incentives Configuration");

        // --- Tier 1 (Symbolic) ---
        logStep("Configuring Tier 1 Incentive");
        tapLatestGivingIncentiveCheckbox();
        enterLatestIncentiveDescription(SeedlingTestData.TIER1_DESCRIPTION);
        selectIncentiveCategory("Symbolic/Incidental Acknowledgements", null);

        scrollDown(0.65, 0.25);

        // --- Tier 2 (Non-Professional, MID FMV = $50) ---
        logStep("Configuring Tier 2 Incentive");
        tapLatestGivingIncentiveCheckbox();
        enterLatestIncentiveDescription(SeedlingTestData.TIER2_DESCRIPTION);
        selectIncentiveCategory("Non-Professional", "50");

        scrollDown(0.65, 0.25);

        // --- Tier 3 (Non-Professional, MID FMV = $50) ---
        logStep("Configuring Tier 3 Incentive");
        tapLatestGivingIncentiveCheckbox();
        enterLatestIncentiveDescription(SeedlingTestData.TIER3_DESCRIPTION);
        selectIncentiveCategory("Non-Professional", "50");

        scrollDown(0.65, 0.25);

        // --- Tier 4 (Professional, MID FMV = $250) ---
        logStep("Configuring Tier 4 Incentive");
        tapLatestGivingIncentiveCheckbox();
        enterLatestIncentiveDescription(SeedlingTestData.TIER4_DESCRIPTION);
        selectIncentiveCategory("Professional", "250");

        // --- Scroll down to reveal Special Incentives (Highest Donor & Group Incentive) ---
        scrollDown(0.70, 0.25);

        // --- Highest Donor Incentive ---
        logStep("Configuring Highest Donor Incentive");
        dismissKeyboardAndroid();
        By highestDonorCheckbox = By.xpath(
                "//android.widget.TextView[contains(@text, 'Highest Donor Incentive')]/preceding-sibling::android.view.ViewGroup | " +
                "//android.widget.TextView[contains(@text, 'Highest Donor Incentive')] | " +
                "//android.view.ViewGroup[@bounds='[77,1040][140,1103]' or contains(@bounds, '[77,1040]')]"
        );
        try {
            tap(highestDonorCheckbox, "Highest Donor Incentive Checkbox");
        } catch (Exception e) {
            tapByCoordinates(108, 1071, "Highest Donor Checkbox (Coordinates)");
        }

        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        enterLatestIncentiveDescription(SeedlingTestData.HIGHEST_DONOR_DESCRIPTION);
        selectIncentiveCategory("Symbolic/Incidental Acknowledgements", null);

        // --- Scroll down so Group Incentive is centered ---
        scrollDown(0.60, 0.25);

        // --- Group Incentive ---
        logStep("Configuring Group Incentive");
        dismissKeyboardAndroid();
        By groupCheckbox = By.xpath(
                "//android.widget.TextView[@text='Group Incentive']/preceding-sibling::android.view.ViewGroup | " +
                "//android.widget.TextView[@text='Group Incentive'] | " +
                "//android.view.ViewGroup[@bounds='[77,958][140,1021]' or contains(@bounds, '[77,958]')]"
        );
        try {
            tap(groupCheckbox, "Group Incentive Checkbox");
        } catch (Exception e) {
            tapByCoordinates(108, 989, "Group Incentive Checkbox (Coordinates)");
        }

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        // Target the Group Incentive EditText that follows "Group Incentive" header
        By groupDescField = By.xpath(
                "//android.widget.TextView[@text='Group Incentive']/following::android.widget.EditText[1] | " +
                "//android.widget.EditText[@bounds='[185,1684][971,1946]' or contains(@bounds, '1684')]"
        );
        try {
            tap(groupDescField, "Describe your Group incentive Field");
            type(groupDescField, SeedlingTestData.GROUP_INCENTIVE_DESCRIPTION, "Describe your Group incentive Field");
        } catch (Exception e) {
            tapByCoordinates(578, 1815, "Describe your Group incentive Field (Coordinates)");
            type(groupDescField, SeedlingTestData.GROUP_INCENTIVE_DESCRIPTION, "Describe your Group incentive Field");
        }
        dismissKeyboardAndroid();

        // --- Scroll down to bring Campaign Group Incentive into view ---
        scrollDown(0.60, 0.25);

        // --- Campaign Group Incentive ---
        logStep("Configuring Campaign Group Incentive");
        By campaignGroupCheckbox = By.xpath(
                "//android.widget.TextView[contains(@text, 'Campaign Group Incentive')]/preceding-sibling::* | " +
                "//android.widget.TextView[contains(@text, 'Campaign Group Incentive')] | " +
                "//android.view.ViewGroup[contains(@bounds, '1213') or contains(@bounds, '1249')]"
        );
        try {
            tap(campaignGroupCheckbox, "Campaign Group Incentive Checkbox");
        } catch (Exception e) {
            tapByCoordinates(108, 1231, "Campaign Group Incentive Checkbox (Coordinates)");
        }

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        // Target the Campaign Group Incentive EditText that follows "Campaign Group Incentive" header
        By campaignGroupDescField = By.xpath(
                "//android.widget.TextView[contains(@text, 'Campaign Group Incentive')]/following::android.widget.EditText[1] | " +
                "(//android.widget.EditText[@input-type='147457'])[last()]"
        );
        tap(campaignGroupDescField, "Describe your Campaign Group incentive Field");
        type(campaignGroupDescField, SeedlingTestData.CAMPAIGN_GROUP_DESCRIPTION, "Describe your Campaign Group incentive Field");

        dismissKeyboardAndroid();
        tapNext();
        logStep("Completed Step 4: All Incentives configured and Next tapped");
    }

    private void tapLatestGivingIncentiveCheckbox() {
        dismissKeyboardAndroid();
        By checkboxLocator = By.xpath(
                "(//*[contains(@text, 'Add a giving incentive') or contains(@content-desc, 'Add a giving incentive')])[last()] | " +
                "//android.widget.TextView[contains(@text, 'Add a giving incentive')][last()]/preceding-sibling::android.view.ViewGroup | " +
                "//*[contains(@text, 'Add a giving incentive')][last()]"
        );
        try {
            tap(checkboxLocator, "Add a giving incentive Checkbox");
        } catch (Exception e) {
            tapByCoordinates(120, 710, "Add a giving incentive Checkbox (Coordinates)");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

    private void enterLatestIncentiveDescription(String description) {
        By descField = By.xpath(
                "(//android.widget.EditText[@input-type='147457'])[last()] | " +
                "(//android.widget.EditText[contains(@hint, 'Describe') or contains(@text, 'Describe')])[last()] | " +
                "(//android.widget.EditText)[last()]"
        );
        tap(descField, "Incentive Description Field");
        type(descField, description, "Incentive Description Field");
        dismissKeyboardAndroid();
    }

    private void selectIncentiveCategory(String categoryKeyword, String categoryValueKey) {
        dismissKeyboardAndroid();

        // 1. Open Category Dropdown for the currently active visible tier
        By categoryDropdown = By.xpath(
                "(//android.widget.TextView[@text='Incentive Category'])[last()]/following-sibling::*[1] | " +
                "(//android.widget.TextView[@text='Incentive Category'])[last()]/../*[2] | " +
                "(//*[contains(@text, 'Select') and not(contains(@text, 'Value'))])[last()]"
        );
        try {
            tap(categoryDropdown, "Incentive Category Dropdown");
        } catch (Exception e) {
            tapByCoordinates(943, 1280, "Incentive Category Dropdown Arrow");
        }

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        // 2. Select Category item from popup
        By categoryOption = By.xpath("//android.widget.TextView[contains(@text, '" + categoryKeyword + "')]");
        tap(categoryOption, "Category Option: " + categoryKeyword);

        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {}

        // 3. Select Category Value if required
        if (categoryValueKey != null && !categoryValueKey.isEmpty()) {
            dismissKeyboardAndroid();

            By valueDropdown = By.xpath(
                    "(//android.widget.TextView[@text='Incentive Category Value'])[last()]/following-sibling::*[1] | " +
                    "(//android.widget.TextView[@text='Incentive Category Value'])[last()]/../*[2] | " +
                    "(//*[contains(@text, 'FMV') or contains(@text, 'Select')])[last()]"
            );

            try {
                tap(valueDropdown, "Incentive Category Value Dropdown");
            } catch (Exception e) {
                scrollDown(0.55, 0.40);
                try {
                    tap(valueDropdown, "Incentive Category Value Dropdown");
                } catch (Exception ex) {
                    tapByCoordinates(943, 1445, "Incentive Category Value Arrow");
                }
            }

            try {
                Thread.sleep(1500);
            } catch (InterruptedException ignored) {}

            By valueOption = By.xpath(
                    "//android.widget.TextView[contains(@text, '" + categoryValueKey + "')] | " +
                    "//*[@content-desc[contains(., '" + categoryValueKey + "')]]"
            );

            try {
                tap(valueOption, "Category Value Option: " + categoryValueKey);
            } catch (Exception e) {
                tapByCoordinates(550, 1690, "Category Value Option (Coordinates)");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }

    // Step 5: Challenge Match
    @Override
    public void fillChallengeMatch(String matchingAmount) {
        logStep("Starting Step 5: Challenge Match");
        if (matchingAmount != null && !matchingAmount.isEmpty()) {
            dismissKeyboardAndroid();
            try {
                By matchCheckbox = By.xpath(
                        "//android.view.ViewGroup[@bounds='[77,428][140,491]' or contains(@bounds, '[77,428]')] | " +
                        "//*[contains(@text, 'Challenge Match') or contains(@content-desc, 'Challenge Match')]"
                );
                tap(matchCheckbox, "Challenge Match Checkbox");
            } catch (Exception e) {
                tapByCoordinates(108, 459, "Challenge Match Checkbox (Coordinates)");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

            String cleanAmount = matchingAmount.replace("$", "").trim();
            By amountField = By.xpath(
                    "//android.widget.EditText[@bounds='[591,700][971,842]' or contains(@bounds, '700')] | " +
                    "//android.widget.EditText[@input-type='16385'] | " +
                    "(//android.widget.EditText)[last()]"
            );
            try {
                tap(amountField, "Matching Amount Field");
                type(amountField, cleanAmount, "Matching Amount Field");
            } catch (Exception e) {
                tapByCoordinates(781, 771, "Matching Amount Field (Coordinates)");
                type(amountField, cleanAmount, "Matching Amount Field");
            }
        }
        dismissKeyboardAndroid();
        tapNext();
        logStep("Completed Step 5: Challenge Match and Next tapped");
    }

    // Step 6: Video Upload
    @Override
    public void uploadVideoOrProceed() {
        logStep("Starting Step 6: Video / Media Screen");
        dismissKeyboardAndroid();

        // 1. Push Test Video to Device Storage using Appium (DCIM and Movies)
        try {
            if (driver() instanceof AndroidDriver) {
                File videoFile = new File("src/test/resources/Untitled video (1).mp4");
                if (videoFile.exists()) {
                    ((AndroidDriver) driver()).pushFile("/sdcard/DCIM/Camera/SeedlingTestVideo.mp4", videoFile);
                    ((AndroidDriver) driver()).pushFile("/sdcard/Movies/SeedlingTestVideo.mp4", videoFile);
                    logStep("✅ Pushed test video file to Android storage");
                }
            }
        } catch (Exception e) {
            logStep("Note: Video push skipped/failed: " + e.getMessage());
        }

        // 2. Click "Record or Choose A File"
        try {
            By uploadBtn = By.xpath("//*[contains(@content-desc, 'Choose A File') or contains(@text, 'Choose A File') or contains(@content-desc, 'Record') or contains(@text, 'Record')]");
            tap(uploadBtn, "Record or Choose A File Button");
        } catch (Exception e) {
            tapByCoordinates(540, 1000, "Record or Choose A File Button (Fallback)");
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // 3. Click "CHOOSE FROM LIBRARY" popup button if shown
        try {
            By chooseFromLibraryBtn = By.xpath("//*[@text='CHOOSE FROM LIBRARY' or @resource-id='android:id/button2' or contains(@text, 'LIBRARY') or contains(@content-desc, 'LIBRARY')]");
            List<WebElement> libBtns = driver().findElements(chooseFromLibraryBtn);
            if (!libBtns.isEmpty() && libBtns.get(0).isDisplayed()) {
                libBtns.get(0).click();
                logStep("✅ Tapped CHOOSE FROM LIBRARY button");
                Thread.sleep(3000);
            }
        } catch (Exception ignored) {}

        // 4. Select the first media thumbnail from gallery / photo picker
        logStep("Selecting the first video from the gallery...");
        By firstGridItem = By.xpath(
                "//android.widget.ImageView[contains(@resource-id, 'icon') or contains(@resource-id, 'thumbnail') or contains(@content-desc, 'Video') or contains(@content-desc, 'Photo')] | " +
                "//android.view.ViewGroup[contains(@resource-id, 'item')][1] | " +
                "(//android.widget.ImageView)[1]"
        );
        try {
            List<WebElement> items = driver().findElements(firstGridItem);
            if (!items.isEmpty()) {
                items.get(0).click();
                logStep("✅ Selected first media thumbnail from gallery");
            } else {
                tapByCoordinates(180, 1312, "First Video Thumbnail (Coordinates)");
            }
        } catch (Exception e) {
            tapByCoordinates(180, 1312, "First Video Thumbnail (Coordinates)");
        }

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // 5. Click 'Done' / 'Select' / 'Open' button to confirm media selection
        logStep("Tapping 'Done' / 'Select' to confirm selection...");
        By doneBtnLocator = By.xpath(
                "//*[@text='Done' or @text='DONE' or @text='Select' or @text='SELECT' or @text='Open' or @text='OPEN' " +
                "or @content-desc='Done' or contains(@resource-id, 'button_done') or contains(@resource-id, 'action_done')]"
        );
        try {
            List<WebElement> doneBtns = driver().findElements(doneBtnLocator);
            if (!doneBtns.isEmpty() && doneBtns.get(0).isDisplayed()) {
                doneBtns.get(0).click();
                logStep("✅ Tapped Done button in gallery picker");
            } else {
                tapByCoordinates(904, 2201, "Done Button (Coordinates)");
            }
        } catch (Exception e) {
            tapByCoordinates(904, 2201, "Done Button (Coordinates)");
        }

        // Wait for video upload / transcoding back in the Seedling app
        logStep("Waiting for video upload/processing to complete inside the app...");
        try { Thread.sleep(12000); } catch (InterruptedException ignored) {}

        // 6. Tap Next
        tapNext();
    }


    // Step 7: Review & Submit (Exact match to TypeScript submitSeedling)
    @Override
    public void submitSeedling() {
        logStep("Starting Step 7: Review & Submit");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {}

        // Tap Confirm & Submit (No scrolling needed as per UI bounds)
        By confirmButton = By.xpath("//*[@content-desc='Confirm & Submit' or @text='Confirm & Submit'] | //android.view.ViewGroup[@bounds='[39,2079][1041,2193]']");
        try {
            tap(confirmButton, "Confirm & Submit Button");
        } catch (Exception e) {
            tapByCoordinates(540, 2136, "Confirm & Submit Button");
        }

        // Tap Authorize Modal
        try {
            Thread.sleep(2500);
            By authorizeButton = By.xpath("//*[contains(@content-desc, 'Authorize') or contains(@text, 'Authorize')]");
            tap(authorizeButton, "Authorize Button");
            Thread.sleep(3000);
        } catch (Exception e) {
            tapByCoordinates(540, 1906, "Authorize Button");
        }

        // Tap OK Modal Button
        try {
            Thread.sleep(2500);
            By okButton = By.xpath("//*[@content-desc='OK' or @text='OK']");
            tap(okButton, "OK Modal Button");
        } catch (Exception e) {
            tapByCoordinates(540, 1554, "OK Modal Button");
        }

        logStep("🎉 Seedling Creation Submitted Successfully!");
    }

    @Override
    public void tapNext() {
        dismissKeyboardAndroid();
        try {
            List<WebElement> elements = driver().findElements(nextButton);
            if (!elements.isEmpty() && elements.get(0).isDisplayed()) {
                elements.get(0).click();
                logStep("Tapped on Next Button");
                return;
            }
        } catch (Exception ignored) {}

        // Tap Next button center on sticky bottom bar
        tapByCoordinates(748, 2121, "Next Button (Bottom Bar Coordinates)");
    }

    private void dismissKeyboardAndroid() {
        try {
            if (driver() instanceof AndroidDriver) {
                ((AndroidDriver) driver()).hideKeyboard();
            }
        } catch (Exception ignored) {}
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
    }

    private void tapByCoordinates(int x, int y, String elementName) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence tap = new Sequence(finger, 1);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y));
        tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver().perform(Collections.singletonList(tap));
        logStep("Tapped on " + elementName + " at (" + x + ", " + y + ")");
    }
}
