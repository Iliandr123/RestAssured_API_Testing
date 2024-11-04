package com.example.teamcity.ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.example.teamcity.ui.elements.BuildElement;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class BuildsPage extends BasePage {
    private static final String BUILDS_URL = "/project/%s?=mode=builds";

    private ElementsCollection buildElements = $$("div[class*=BuildsByBuildType__container]");

    private SelenideElement header = $(".ProjectPageHeader__header--Z3");

    public BuildsPage() {
        header.shouldBe(Condition.visible, BASE_WAITING);
    }

    public static BuildsPage open(String projectName) {
        return Selenide.open(BUILDS_URL.formatted(projectName), BuildsPage.class);
    }

    public List<BuildElement> getBuilds() {
        return generatePageElements(buildElements, BuildElement::new);
    }
}
