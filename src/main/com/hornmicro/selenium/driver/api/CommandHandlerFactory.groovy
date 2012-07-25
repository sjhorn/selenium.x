package com.hornmicro.selenium.driver.api


import java.lang.reflect.Method
import java.util.regex.Matcher

import org.codehaus.groovy.runtime.StackTraceUtils
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverBackedSelenium
import org.openqa.selenium.WebElement
import org.openqa.selenium.internal.seleniumemulation.ElementFinder
import org.openqa.selenium.internal.seleniumemulation.JavascriptLibrary
import org.openqa.selenium.safari.SafariDriver

import com.hornmicro.selenium.model.TestCaseModel
import com.hornmicro.selenium.model.TestModel
import com.thoughtworks.selenium.Selenium

// Based on javascript/selenium-core/scripts/selenium-commandhandlers.js
// and javascript/selenium-core/scripts/htmlutils.js
// and javascript/selenium-core/scripts/selenium-api.js
// and javascript/selenium-core/scripts/selenium-executionloop.js
// and ide/main/src/content/selenium-runner.js
class CommandHandlerFactory {
    static final List ignoredMethods = [ 
        'start', 'stop', 'setExtensionJs',
        'showContextualBanner', 'getLog', 'captureNetworkTraffic',
        'addCustomRequestHeader'
    ]
    
    static final List actionMethods = [
        'addLocationStrategy','addScript','addSelection','allowNativeXpath','altKeyDown','altKeyUp',
        'answerOnNextPrompt','assignId','captureEntirePageScreenshot','check','chooseCancelOnNextConfirmation',
        'chooseOkOnNextConfirmation','click','clickAt','close','contextMenu',
        'contextMenuAt','controlKeyDown','controlKeyUp','createCookie','deleteAllVisibleCookies',
        'deleteCookie','deselectPopUp','doubleClick','doubleClickAt','dragAndDrop',
        'dragAndDropToObject','dragdrop','fireEvent','focus','goBack',
        'highlight','ignoreAttributesWithoutValue','keyDown','keyPress','keyUp',
        'metaKeyDown','metaKeyUp','mouseDown','mouseDownAt','mouseDownRight',
        'mouseDownRightAt','mouseMove','mouseMoveAt','mouseOut','mouseOver',
        'mouseUp','mouseUpAt','mouseUpRight','mouseUpRightAt', 'open',
        'openWindow','refresh','removeAllSelections','removeScript','removeSelection',
        'rollup','runScript','select','selectFrame','selectPopUp',
        'selectWindow','setBrowserLogLevel','setCursorPosition','setMouseSpeed','setSpeed',
        'setTimeout','shiftKeyDown','shiftKeyUp','submit','type',
        'typeKeys','uncheck','useXpathLibrary','waitForCondition','waitForFrameToLoad',
        'waitForPageToLoad','waitForPopUp','windowFocus','windowMaximize'
    ]
    
    static final List seleniumApi = Selenium.declaredMethods.findAll { !(it.name in ignoredMethods) }
    static Long DEFAULT_TIMEOUT = 30000
    Map<String, CommandHandler> handlers = [:]
    Map storedVars = [:]
    
    void registerAction(name, actionBlock, wait, dontCheckAlertsAndConfirms) {
        this.handlers[name] = new ActionHandler(actionBlock, wait, dontCheckAlertsAndConfirms)
    }

    void registerAccessor(name, accessBlock) {
        this.handlers[name] = new AccessorHandler(accessBlock)
    }

    void registerAssert(name, assertBlock, haltOnFailure) {
        this.handlers[name] = new AssertHandler(assertBlock, haltOnFailure)
    }

    CommandHandler getCommandHandler(name) {
        return this.handlers[name]
    }
    
    void _registerAllAccessors(Selenium selenium) {
        // Methods of the form getFoo(target) result in commands:
        // getFoo, assertFoo, verifyFoo, assertNotFoo, verifyNotFoo
        // storeFoo, waitForFoo, and waitForNotFoo.
        
        for (Method method : seleniumApi ) {
            String functionName = method.name
            Matcher match = ( functionName =~ /^(get|is)([A-Z].+)$/ ) 
            if (match.matches()) {
                //println "Looking at Accessor $functionName"
                def accessMethod = method
                def accessBlock = new FunctionBind(accessMethod, selenium)  //fnBind(accessMethod, seleniumApi);
                def baseName = match.group(2)
                def isBoolean = (match.group(1) == "is");
                def requiresTarget =  accessBlock.__method.parameterTypes.size() == 1 //(accessMethod.length == 1)
                
                this.registerAccessor(functionName, accessBlock)
                this._registerStoreCommandForAccessor(baseName, accessBlock, requiresTarget)

                def predicateBlock = this._predicateForAccessor(accessBlock, requiresTarget, isBoolean)
                this._registerAssertionsForPredicate(baseName, predicateBlock)
                this._registerWaitForCommandsForPredicate(selenium, baseName, predicateBlock)
                
            }
        }
    }
    
    void _registerAllActions(Selenium selenium) {
        for (Method method : seleniumApi) {
            String functionName = method.name
            //Matcher match = (functionName =~ /^do([A-Z].+)$/)
            if (functionName in actionMethods /*match.matches()*/) {
                if(functionName == 'open' && method.parameterTypes.size() != 1)
                    continue // ignore the 2 arg open.
                
                def actionName = functionName //lcfirst(match.group(1))
                def actionMethod = method
                def dontCheckPopups = functionName in ['doWaitForPopup','getAlert','getConfirmation','doWaitForCondition','doWaitForPageLoad']
                //actionMethod.dontCheckAlertsAndConfirms
                def actionBlock = new FunctionBind(actionMethod, selenium)
                this.registerAction(actionName, actionBlock, false, dontCheckPopups)
                this.registerAction(actionName + "AndWait", actionBlock, true, dontCheckPopups)
            }
            
        }
    }
    
    // This does not match anything at the moment.
    void _registerAllAsserts(Selenium selenium) {
        for (Method method : seleniumApi) {
            String functionName = method.name
            Matcher match = (functionName =~ /^assert([A-Z].+)$/)
            if (match.matches()) {
                def assertBlock = new FunctionBind(method, selenium)

                // Register the assert with the "assert" prefix, and halt on failure.
                def assertName = functionName
                this.registerAssert(assertName, assertBlock, true)

                // Register the assert with the "verify" prefix, and do not halt on failure.
                def verifyName = "verify" + match.group(1)
                this.registerAssert(verifyName, assertBlock, false)
            }
        }
    }

    void registerAll(Selenium selenium) {
        this._registerAllAccessors(selenium)
        this._registerAllActions(selenium)
        this._registerAllAsserts(selenium)
    }
    
    def _waitForActionForPredicate(predicateBlock) {
        // Convert an isBlahBlah(target, value) function into a waitForBlahBlah(target, value) function.
        return { target, value=null ->
            
            def terminationCondition = { ->
                try {
                    if(value) {
                        return predicateBlock(target, value).isTrue
                    } else if (target) {
                        return predicateBlock(target).isTrue
                    } else {
                        return predicateBlock().isTrue
                    }
                } catch (e) {
                    // Treat exceptions as meaning the condition is not yet met.
                    // Useful, for example, for waitForValue when the element has
                    // not even been created yet.
                    // TODO: possibly should rethrow some types of exception.
                    return false;
                }
            }
            Long timeoutTime = new Date().getTime() + DEFAULT_TIMEOUT
            return { ->
                if(new Date().getTime() > timeoutTime) {
                    throw new SeleniumError("Timed out after " + DEFAULT_TIMEOUT + "ms")
                }
                return terminationCondition()
            }
            //return Selenium.decorateFunctionWithTimeout(terminationCondition, this.defaultTimeout);
        }
    }
    
    def _registerWaitForCommandsForPredicate(Selenium selenium, String baseName, predicateBlock) {
        // Register a waitForBlahBlah and waitForNotBlahBlah based on the specified accessor.
        def waitForActionMethod = this._waitForActionForPredicate(predicateBlock)
        def waitForActionBlock = waitForActionMethod //new FunctionBind(waitForActionMethod, selenium)
        
        def invertedPredicateBlock = this._invertPredicate(predicateBlock);
        def waitForNotActionMethod = this._waitForActionForPredicate(invertedPredicateBlock);
        def waitForNotActionBlock = waitForNotActionMethod //new FunctionBind(waitForNotActionMethod, selenium);
        
        this.registerAction("waitFor" + baseName, waitForActionBlock, false, true);
        this.registerAction("waitFor" + this._invertPredicateName(baseName), waitForNotActionBlock, false, true);
        //TODO decide remove "waitForNot.*Present" action name or not
        //for the back compatiblity issues we still make waitForNot.*Present availble
        this.registerAction("waitForNot" + baseName, waitForNotActionBlock, false, true);
    }
    
    def _registerAssertionsForPredicate(baseName, predicateBlock) {
        // Register an assertion, a verification, a negative assertion,
        // and a negative verification based on the specified accessor.
        def assertBlock = this.createAssertionFromPredicate(predicateBlock)
        this.registerAssert("assert" + baseName, assertBlock, true)
        this.registerAssert("verify" + baseName, assertBlock, false)

        def invertedPredicateBlock = this._invertPredicate(predicateBlock)
        def negativeassertBlock = this.createAssertionFromPredicate(invertedPredicateBlock)
        this.registerAssert("assert" + this._invertPredicateName(baseName), negativeassertBlock, true)
        this.registerAssert("verify" + this._invertPredicateName(baseName), negativeassertBlock, false)
    }
    
    def createAssertionFromPredicate(predicateBlock) {
        // Convert an isBlahBlah(target, value) function into an assertBlahBlah(target, value) function.
        return { target, value=null ->
            PredicateResult result
            if(value != null) {
                result = predicateBlock(target, value)
            } else {
                result = predicateBlock(target)
            }
                
            if (!result.isTrue) {
                throw new AssertionFailedError(result.message) //Assert.fail(result.message);
            }
        }
    }
    
    def _invertPredicate(predicateBlock) {
        // Given a predicate, return the negation of that predicate.
        // Leaves the message unchanged.
        // Used to create assertNot, verifyNot, and waitForNot commands.
        return { target, value=null ->
            PredicateResult result
            if(result) {
                result = predicateBlock(target, value)
            } else if (target) {
                result = predicateBlock(target)
            } else {
                result = predicateBlock()
            }
            result.isTrue = !result.isTrue
            return result
        }
    }
    
    def _invertPredicateName(baseName) {
        Matcher matchResult = (baseName =~ /^(.*)Present$/)
        if (matchResult.matches()) {
            return matchResult[0][1] + "NotPresent";
        }
        return "Not" + baseName;
    }
    
    def _predicateForAccessor(accessBlock, requiresTarget, isBoolean) {
        if (isBoolean) {
            return this._predicateForBooleanAccessor(accessBlock)
        }
        if (requiresTarget) {
            return this._predicateForSingleArgAccessor(accessBlock)
        }
        return this._predicateForNoArgAccessor(accessBlock)
    }
    
    def _predicateForNoArgAccessor(accessBlock) {
        // Given a (no-arg) accessor function getBlah(),
        // return a "predicate" equivalient to isBlah(value) that
        // is true when the value returned by the accessor matches the specified value.
        return { value ->
            def accessorResult = accessBlock()
            accessorResult = selArrayToString(accessorResult)
            if (PatternMatcher.matches(value, accessorResult)) {
                return new PredicateResult(true, "Actual value '" + accessorResult + "' did match '" + value + "'");
            } else {
                return new PredicateResult(false, "Actual value '" + accessorResult + "' did not match '" + value + "'");
            }
        }
    }
    
    def _predicateForSingleArgAccessor(accessBlock) {
        // Given an accessor function getBlah(target),
        // return a "predicate" equivalient to isBlah(target, value) that
        // is true when the value returned by the accessor matches the specified value.
        return { target, value ->
            def accessorResult = accessBlock(target)
            accessorResult = selArrayToString(accessorResult)
            if (PatternMatcher.matches(value, accessorResult)) {
                return new PredicateResult(true, "Actual value '" + accessorResult + "' did match '" + value + "'");
            } else {
                return new PredicateResult(false, "Actual value '" + accessorResult + "' did not match '" + value + "'");
            }
        }
    }
    
    def _predicateForBooleanAccessor(accessBlock) {
        // Given a boolean accessor function isBlah(),
        // return a "predicate" equivalient to isBlah() that
        // returns an appropriate PredicateResult value.
        return { Object... arguments ->
            def accessorResult
            if (arguments.size() > 2) throw new SeleniumError("Too many arguments! " + arguments.size());
            if (arguments.size() == 2) {
                accessorResult = accessBlock(arguments[0], arguments[1])
            } else if (arguments.length == 1) {
                accessorResult = accessBlock(arguments[0])
            } else {
                accessorResult = accessBlock()
            }
            if (accessorResult) {
                return new PredicateResult(true, "true")
            } else {
                return new PredicateResult(false, "false")
            }
        }
    }
    
    void _registerStoreCommandForAccessor(baseName, accessBlock, requiresTarget) {
        def action
        if (requiresTarget) {
            action = { target, varName ->
                storedVars[varName] = accessBlock(target)
            }
        } else {
            action = { varName ->
                storedVars[varName] = accessBlock()
            }
        }
        this.registerAction("store" + baseName, action, false, true);
    }
    
    private selArrayToString(a) {
        String selString = a as String
        selString = selString.replaceAll(/([,\\])/, '\\$1');
        return new String(a);
    }
    /*
    private String lcfirst(String action) {
        if(action == null) return null
        if(action.size() == 0) return action
        return action[0].toUpperCase() + action.substring(1)
    }
    */
    
    static void highlightElement(WebDriver driver, String target) {
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
     
    static main(args) {
        WebDriver driver
        try {
            //System.setProperty("webdriver.chrome.driver", "libs/chromedriver")
            //DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            //capabilities.setCapability("chrome.binary", "/Applications/Chromium.app/Contents/MacOS/Chromium");
            
            driver = new SafariDriver() //new ChromeDriver(capabilities)
            Selenium selenium = new WebDriverBackedSelenium(driver, "http://www.wotif.com/")
            def chf = new CommandHandlerFactory()
            chf.registerAll(selenium)
            
            TestCaseModel testCase = TestCaseModel.load(new File("test/Map.html"))
            
            for( TestModel test : testCase.tests) {
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
                        println res.passed 
                    } else {
                        System.err.println("Failed [$res.failureMessage]")
                        break
                    } 
                } else if(res instanceof AccessorResult) {
                    if(res?.terminationCondition) {
                        println "Term condition AccessorResult" 
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
                                    Thread.sleep(20)
                                }
                                println "Success"
                            } catch(e) {
                                System.err.println "Failed ${e.message}"
                                break
                            }
                        } else {
                            println "Action result ${res.terminationCondition}"
                        }
                    } else {
                        println "Done"
                    }
                }
                println ("_"*40)+"\n\n"
                
                //Thread.sleep(20) 
                
            }            
            
        } catch(e) {
            StackTraceUtils.sanitize(e)
            e.printStackTrace()
        } finally {
            driver.quit()
            System.exit(0) // Safari hang
        }
    }
}

