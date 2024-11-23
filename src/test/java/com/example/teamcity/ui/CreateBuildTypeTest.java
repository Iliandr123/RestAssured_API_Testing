package com.example.teamcity.ui;

import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.ui.pages.BuildsPage;
import com.example.teamcity.ui.pages.admin.CreateBuildConfigurationPage;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import static com.example.teamcity.api.enums.Endpoint.BUILD_TYPES;
import static com.example.teamcity.api.enums.Endpoint.PROJECTS;

@Test(groups = {"Regression"})
public class CreateBuildTypeTest extends BaseUiTest {
    private static final String REPO_URL = "https://github.com/Iliandr123/RestAssured_API_Testing";

    @Test(description = "User should be able to create build type configuration", groups = {"Positive"})
    public void userCreateBuildTypeConfigurationTest() {

        loginAs(testData.getUser());

        var project = superUserCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        CreateBuildConfigurationPage.open(testData.getProject().getId())
                .createForm(REPO_URL)
                .setupBuildTypeConfiguration(testData.getBuildType().getName());

        var createdBuildTypeConfig = superUserCheckRequests.<BuildType>getRequest(BUILD_TYPES).read("name:" + testData.getBuildType().getName());
        softy.assertNotNull(createdBuildTypeConfig);

        var buildExists = BuildsPage.open(project.getId())
                .getBuilds().stream()
                .anyMatch(build -> build.getButton().text().equals(testData.getBuildType().getName()));

        softy.assertTrue(buildExists);
    }

    @Test(description = "User should not be able to create build type configuration with same name", groups = {"Negative"})
    public void userCannotCreateBuildTypeConfigurationWithSameNameTest() {

        loginAs(testData.getUser());

        var project = superUserCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());
        var buildType = superUserCheckRequests.<BuildType>getRequest(BUILD_TYPES).create(testData.getBuildType());

        CreateBuildConfigurationPage
                .open(project.getId())
                .createForm(REPO_URL)
                .setupBuildTypeConfiguration(buildType.getName());

        softy.assertTrue(CreateBuildConfigurationPage.hasErrorMessage("Build configuration with name \"%s\" already exists in project: \"%s\""
                .formatted(testData.getBuildType().getName(), project.getName())));

        superUserUncheckRequests.getRequest(BUILD_TYPES).read("name:" + buildType.getId())
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
