package com.hornmicro.selenium.driver

import com.thoughtworks.selenium.Selenium

class SeleniumInstance {
    Selenium selenium
    String baseUrl

    SeleniumInstance(Selenium selenium, String baseUrl) {
        this.selenium = selenium
        this.baseUrl = baseUrl
    }    
}
