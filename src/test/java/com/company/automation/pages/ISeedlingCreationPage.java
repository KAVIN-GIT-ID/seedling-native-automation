package com.company.automation.pages;

/**
 * Interface contract for complete Seedling Creation Page Objects across Android & iOS.
 */
public interface ISeedlingCreationPage {

    // Step 1: Navigation & Charity
    void tapCreateTab();
    void tapCreateNewSeedling();
    void searchAndSelectCharity(String charitySearch, String charityLabel);
    void tapNext();

    // Step 2: Details & Campaign
    void enterSeedlingTitle(String title);
    void enterSeedlingDescription(String description);
    void searchAndSelectCoSponsor(String coSponsorSearch, String coSponsorName);
    void toggleCreateCampaignCheckbox();
    void enterCampaignTitle(String campaignTitle);
    void enterCampaignDescription(String campaignDescription);

    // Step 3: Goals
    void enterSeedlingGoal(String goalAmount);
    void toggleEndSeedlingIfGoalMetCheckbox();
    void enterCampaignGoal(String campaignGoalAmount);
    void toggleEndCampaignIfGoalMetCheckbox();

    // Step 4: Incentives
    void fillIncentives();

    // Step 5: Challenge Match
    void fillChallengeMatch(String matchingAmount);

    // Step 6: Video Upload
    void uploadVideoOrProceed();

    // Step 7: Review & Submit
    void submitSeedling();
}
