package com.hornmicro.selenium.driver

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.internal.seleniumemulation.ElementFinder
import org.openqa.selenium.internal.seleniumemulation.JavascriptLibrary
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.safari.SafariDriver

import com.hornmicro.selenium.driver.api.AccessorResult
import com.hornmicro.selenium.driver.api.AssertResult
import com.hornmicro.selenium.driver.api.CommandHandler
import com.hornmicro.selenium.driver.api.CommandHandlerFactory
import com.hornmicro.selenium.driver.api.SeleniumCommand
import com.hornmicro.selenium.model.TestModel
import com.opera.core.systems.OperaDriver
import com.thoughtworks.selenium.Selenium

class DriveTest {
    static Boolean highlight = true
    static Map<String, WebDriver> drivers = [:]
    static Map<WebDriver, SeleniumInstance> seleniums = [:]
    
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
                
            case 'opera':
                drivers['opera'] = new OperaDriver()
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
    
    static SeleniumInstance getSeleniumInstance(String baseUrl, WebDriver driver) {
        if(!seleniums.containsKey(driver)) {
            println "Making first instance"
            seleniums[driver] = new SeleniumInstance(baseUrl, driver)
        }
        SeleniumInstance si = seleniums[driver]
        
        // If the baseUrl changes 
        println "Comparing $si.baseUrl with $baseUrl"
        if(si.baseUrl != baseUrl) {
            println "Making new instance "
            seleniums[driver] = new SeleniumInstance(baseUrl, driver)
        }
        return seleniums[driver]
    }
    
    static executeAction(String browser, String baseUrl, TestModel test) {
        WebDriver driver = getDriver(browser)
        
        SeleniumInstance si = getSeleniumInstance(baseUrl, driver)
        Selenium selenium = si.selenium
        CommandHandlerFactory chf = si.commandHandlerFactory
        
        // Ensure the current window is selected in safari driver
        if(test.command == "open") {
            println ">>>> Focusing window now"
            selenium.selectWindow("null")   
        }
        
        SeleniumCommand command = new SeleniumCommand(test.command)
        CommandHandler handler = chf.getCommandHandler(command.command)
        command.target = test.target
        command.value = test.value
        println "Running ${command}"
        
        if(test.target)
            highlightElement(driver, test.target)
        def res = handler.execute(selenium, command)
        
        if(res instanceof AssertResult) {
            if(res.passed) {
                println "Passed"
            } else {
                System.err.println("Failed [$res.failureMessage]")
                throw new RuntimeException("Failed [$res.failureMessage]")
            }
        } else if(res instanceof AccessorResult) {
            if(res?.terminationCondition) {
                System.err.println "Term condition AccessorResult"
            } else {
                println "Accessor Result ${res.result}"
            }
        } else { // ActionResult
            if(res?.terminationCondition) {
                println "Term condition ActionResult"
                if(res.terminationCondition instanceof Closure) {
                    try {
                        def r
                        while(!(r = res.terminationCondition())) {
                            Thread.sleep(100)
                        }
                        println "Success"
                    } catch(e) {
                        System.err.println "Failed ${e.message}"
                        throw new RuntimeException("Failed [$res.failureMessage]")
                    }
                } else {
                    println "Action result ${res.terminationCondition}"
                }
            } else {
                println "Done"
            }
        }
        println ("_"*40)+"\n\n"
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
            //println ">>>>> Ignoring $e.message"
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
}
