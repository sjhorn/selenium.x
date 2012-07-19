package com.hornmicro.selenium.driver

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverBackedSelenium
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.DesiredCapabilities

import com.thoughtworks.selenium.Selenium

class DriveTest {
    static List methods = Selenium.declaredMethods*.name
    
    static WebDriver driver
    
    static executeAction(baseUrl, command, target="", value="") {
        if(!driver) {
            System.setProperty("webdriver.chrome.driver", "libs/chromedriver")
            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability("chrome.binary", "/Applications/Chromium.app/Contents/MacOS/Chromium");
            driver = new ChromeDriver(capabilities);
        }
        Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl)
        if(target.size() && value.size()) {
            selenium."$command"(target, value)
        } else if(target.size()) {
            selenium."$command"(target)
        } else {
            selenium."$command"()
        }
    }
    
    Closure assertClosure(method, truth=true) {
        switch(method.parameterTypes.size()) {
            case 0: return { -> assert method() == truth }
            case 1: return { arg1 -> assert method(arg1) == truth }
            case 2: return { arg1, arg2 -> assert method(arg1, arg2) == truth }
            default:
                throw new RuntimeException("Dont know this method + ${method.name}")
        }
    }
    
    void waitForClosure(method, truth=true) {
        
    }
    
    void andWaitClosure(method) {
        
    }
    
    void getMethod(name) {
        
    }
    
    
    
    static main(args) {
        //WebDriver driver = new FirefoxDriver();
        try {
            String baseUrl = "http://www.wotif.com/";
            
            //Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl);
            //selenium.open("hotel/View?hotel=W4937");
            //assert selenium.isTextPresent("Cancellation policy: No Cancellations or Changes")
            //selenium.click("link=Details");
            //assert selenium.isVisible("cancelPolicy0")
            
            //println "PASSED WOOHOO !!!"
        } finally {
            //driver.quit()
        }
        
    }

}
