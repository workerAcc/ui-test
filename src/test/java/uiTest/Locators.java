package uiTest;

import com.codeborne.selenide.Condition;

import static com.codeborne.selenide.Selenide.$x;

public class Locators {
    public void clickButtonLink(String name) {
        $x("//a[contains(text(),'" + name + "')]").click();
    }
    public void checkTextInTitle(String name) {
        $x("//h1[@class='tn-atom']").shouldHave(Condition.ownText(name));
    }
}
