package com.company.automation.tests;

import com.company.automation.base.BaseTest;
import com.company.automation.data.SeedlingTestData;
import com.company.automation.pages.ISeedlingCreationPage;
import com.company.automation.pages.SeedlingCreationPageFactory;
import org.testng.annotations.Test;

public class SeedlingCreationTests extends BaseTest {

    @Test(description = "Verify Seedling Creation end-to-end flow from Step 1 to Step 7 Submit")
    public void testCreateSeedlingBaseline() {
        // 1. Common Registered User Login
        loginAsRegisteredUser();

        // 2. Navigate to Creation Flow
        ISeedlingCreationPage creationPage = SeedlingCreationPageFactory.create();
        creationPage.tapCreateTab();
        creationPage.tapCreateNewSeedling();

        // 3. Step 1: Select Charity
        creationPage.searchAndSelectCharity(
                SeedlingTestData.CHARITY_SEARCH,
                SeedlingTestData.CHARITY_LABEL
        );

        // 4. Proceed to Step 2
        creationPage.tapNext();

        // 5. Step 2: Seedling Details
        creationPage.enterSeedlingTitle(SeedlingTestData.TITLE);
        creationPage.enterSeedlingDescription(SeedlingTestData.DESCRIPTION);


        // 6. Step 2: Invite Co-Sponsor
        creationPage.searchAndSelectCoSponsor(
                SeedlingTestData.CO_SPONSOR_SEARCH,
                SeedlingTestData.CO_SPONSOR_NAME
        );

        // 7. Step 2: Create Campaign Toggle & Details
        creationPage.toggleCreateCampaignCheckbox();
        creationPage.enterCampaignTitle(SeedlingTestData.CAMPAIGN_TITLE);
        creationPage.enterCampaignDescription(SeedlingTestData.CAMPAIGN_DESCRIPTION);

        // 8. Proceed to Step 3
        creationPage.tapNext();
        log.info("Completed Step 2: Details, Co-Sponsor & Campaign filled and Next tapped");

        // 9. Step 3: Goals
        creationPage.enterSeedlingGoal(SeedlingTestData.SEEDLING_GOAL);
        creationPage.toggleEndSeedlingIfGoalMetCheckbox();
        creationPage.enterCampaignGoal(SeedlingTestData.CAMPAIGN_GOAL);
        creationPage.toggleEndCampaignIfGoalMetCheckbox();

        // 10. Proceed to Step 4
        creationPage.tapNext();
        log.info("Completed Step 3: Seedling & Campaign goals set and Next tapped");

        // 11. Step 4: Giving Incentives
        creationPage.fillIncentives();
        log.info("Completed Step 4: All Giving Incentives configured and Next tapped");

        // 12. Step 5: Challenge Match
        creationPage.fillChallengeMatch(SeedlingTestData.MATCHING_AMOUNT);
        log.info("Completed Step 5: Challenge Match configured and Next tapped");

        // 13. Step 6: Video Upload
        creationPage.uploadVideoOrProceed();
        log.info("Completed Step 6: Video Upload configured and Next tapped");

        // 14. Step 7: Review & Submit
        creationPage.submitSeedling();
        log.info("🎉 Completed Step 7: Review & Final Seedling Submission successful!");
    }
}
