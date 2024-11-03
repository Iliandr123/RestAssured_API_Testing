package com.example.teamcity.api;

import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.models.Roles;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.UncheckedRequests;
import com.example.teamcity.api.requests.unchecked.UncheckedBase;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.example.teamcity.api.enums.Endpoint.*;
import static com.example.teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class BuildTypeTest extends BaseApiTest {
    @Test(description = "User should be able to create build type", groups = {"Positive", "CRUD"})
    public void userCreatesBuildTypeTest() {
        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());

        var createdBuildType = userCheckRequests.<BuildType>getRequest(BUILD_TYPES).read("id:" + testData.getBuildType().getId());

        softy.assertEquals(testData.getBuildType().getName(), createdBuildType.getName(), "Build type name is not correct");
    }

    @Test(description = "User should not be able to create two build types with the same id", groups = {"Negative", "CRUD"})
    public void userCreatesTwoBuildTypesWithTheSameIdTest() {
        var buildTypeWithSameId = generate(Arrays.asList(testData.getProject()), BuildType.class, testData.getBuildType().getId());

        superUserCheckRequests.getRequest(USERS).create(testData.getUser());
        var userCheckRequests = new CheckedRequests(Specifications.authSpec(testData.getUser()));

        userCheckRequests.<Project>getRequest(PROJECTS).create(testData.getProject());

        userCheckRequests.getRequest(BUILD_TYPES).create(testData.getBuildType());
        new UncheckedBase(Specifications.authSpec(testData.getUser()), BUILD_TYPES)
                .create(buildTypeWithSameId)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("The build configuration / template ID \"%s\" is already used by another configuration or template".formatted(testData.getBuildType().getId())));
    }

    @Test(description = "Project admin should be able to create build type for their project", groups = {"Positive", "Roles"})
    public void projectAdminCreatesBuildTypeTest() {
        // set project
        var project = testData.getProject();
        // set user with "PROJECT_ADMIN" role
        var projectAdminRoles = generate(Roles.class, "PROJECT_ADMIN", "p:" + testData.getProject().getId());
        testData.getUser().setRoles(projectAdminRoles);
        var projectAdminUser = testData.getUser();
        // set build type
        var buildType = testData.getBuildType();

        // Step 1. Create project and assign projectAdmin to it by superuser
        superUserCheckRequests.getRequest(PROJECTS).create(project);
        superUserCheckRequests.getRequest(USERS).create(projectAdminUser);

        // Step 2. Should be successfully create buildType in project by its project admin
        new UncheckedRequests(Specifications.authSpec(projectAdminUser))
                .getRequest(BUILD_TYPES).create(buildType)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test(description = "Project admin should not be able to create build type for not their project", groups = {"Negative", "Roles"})
    public void projectAdminCreatesBuildTypeForAnotherUserProjectTest() {
        // set first project
        var project1 = testData.getProject();
        // set first user with "PROJECT_ADMIN" role
        var projectAdminRoles1 = generate(Roles.class, "PROJECT_ADMIN", "p:" + testData.getProject().getId());
        testData.getUser().setRoles(projectAdminRoles1);
        var projectAdminUser1 = testData.getUser();

        // Step 1. Create project1 and assign projectAdmin1 to it by superuser
        superUserCheckRequests.getRequest(PROJECTS).create(project1);
        superUserCheckRequests.getRequest(USERS).create(projectAdminUser1);

        // regenerate test data
        testData = generate();

        // set second project
        var project2 = testData.getProject();
        // set second user with "PROJECT_ADMIN" role
        var projectAdminRoles2 = generate(Roles.class, "PROJECT_ADMIN", "p:" + testData.getProject().getId());
        testData.getUser().setRoles(projectAdminRoles2);
        var projectAdminUser2 = testData.getUser();

        // Step 2. Create project2 and assign projectAdmin2 to it by superuser
        superUserCheckRequests.getRequest(PROJECTS).create(project2);
        superUserCheckRequests.getRequest(USERS).create(projectAdminUser2);

        // Step 3. Try to create buildType for the project1 by projectAdmin2
        var buildType = testData.getBuildType();
        buildType.setProject(project1);
        var requester = new UncheckedRequests(Specifications.authSpec(projectAdminUser2));
        var response = requester.getRequest(BUILD_TYPES).create(buildType);

        // Step 4. Forbidden status should be displayed
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString("You do not have enough permissions to edit project with"));
    }
}

