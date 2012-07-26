package com.hornmicro.selenium.driver

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverBackedSelenium

import com.hornmicro.selenium.driver.api.CommandHandlerFactory
import com.thoughtworks.selenium.Selenium

class SeleniumInstance {
    Selenium selenium
    String baseUrl
    CommandHandlerFactory commandHandlerFactory

    SeleniumInstance(String baseUrl, WebDriver driver) {
        this.baseUrl = baseUrl
        this.selenium = new WebDriverBackedSelenium(driver, baseUrl)
        this.commandHandlerFactory = new CommandHandlerFactory(selenium)
    }    
}
