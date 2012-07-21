package com.hornmicro.selenium.driver

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverBackedSelenium
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.internal.seleniumemulation.ElementFinder
import org.openqa.selenium.internal.seleniumemulation.JavascriptLibrary
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.safari.SafariDriver

import com.thoughtworks.selenium.Selenium

class DriveTest {
    static List methods = Selenium.declaredMethods*.name
    static Boolean highlight = true
    static Map drivers = [:]
    
    static void loadDriver(String browser) {
        switch(browser.toLowerCase()) {
            case 'chrome':
                System.setProperty("webdriver.chrome.driver", "libs/chromedriver")
                DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                capabilities.setCapability("chrome.binary", "/Applications/Chromium.app/Contents/MacOS/Chromium");
            
                drivers['chrome'] = new ChromeDriver(capabilities)
                break
                
            case 'firefox':
                drivers['firefox'] = new FirefoxDriver()
                break
                
            case 'safari':
                drivers['safari'] = new SafariDriver()
                break
                
            default:
                throw new RuntimeException("$browser Not supported...yet")
        }
        
    }
    
    static WebDriver getDriver(String browser="Firefox") {
        if(!drivers.containsKey(browser)) {
            loadDriver(browser)
        }
        return drivers[browser]
    }
    
    static executeAction(browser, baseUrl, command, target="", value="") {
        WebDriver driver = getDriver(browser)
        Selenium selenium = new WebDriverBackedSelenium(driver, baseUrl)
        if(target.size() && value.size()) {
            highlightElement(driver, target)
            
            return selenium."$command"(target, value)
        } else if(target.size()) {
            highlightElement(driver, target)
        
            return selenium."$command"(target)
        } else {
            return selenium."$command"()
        }
    }
    
    static Boolean findElement(String browser, String baseURL, String target) {
        try {
            WebDriver driver = getDriver(browser)
            JavascriptExecutor js = ((JavascriptExecutor) driver)
            JavascriptLibrary javascriptLibrary = new JavascriptLibrary()
            ElementFinder elementFinder = new ElementFinder(javascriptLibrary)
            
            WebElement element = elementFinder.findElement(driver, target)
            String bgcolor = element.getCssValue("backgroundColor");
            (0..4).each {
                js.executeScript("arguments[0].style.backgroundColor = '#fff648'", element)
                Thread.sleep(300)
                js.executeScript("arguments[0].style.backgroundColor = '${bgcolor}'", element)
                Thread.sleep(300)
            }
            return true
        } catch(e) {
            return false
        }
        
    }
    
    static void highlightElement(WebDriver driver, String target) {
        if(!highlight) {
            return
        }
        try {
            JavascriptExecutor js = ((JavascriptExecutor) driver)
            JavascriptLibrary javascriptLibrary = new JavascriptLibrary()
            ElementFinder elementFinder = new ElementFinder(javascriptLibrary)
            
            WebElement element = elementFinder.findElement(driver, target)
            String bgcolor = element.getCssValue("backgroundColor");
            
            js.executeScript("arguments[0].style.backgroundColor = '#fff648'", element)
            Thread.sleep(800)
            js.executeScript("arguments[0].style.backgroundColor = '${bgcolor}'", element)
        } catch(e) {
            println ">>>>> Ignoring $e.message"
        }
    }
    
    static void dispose() {
        drivers.each { String name, WebDriver driver ->
            println "Closing ${name}"
            try {
                driver.quit()
            } catch(e) {
                e.printStackTrace()
            }
            println "Done"
        }
        drivers.clear()
    }
    
    /*
    Closure assertClosure(method, truth=true) {
        switch(method.parameterTypes.size()) {
            case 0: return { -> assert method() == truth }
            case 1: return { arg1 -> assert method(arg1) == truth }
            case 2: return { arg1, arg2 -> assert method(arg1, arg2) == truth }
            default:
                throw new RuntimeException("I don't know this method -> ${method.name}")
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
    */
}
