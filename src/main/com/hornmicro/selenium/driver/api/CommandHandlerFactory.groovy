package com.hornmicro.selenium.driver.api


import java.util.regex.Matcher

import com.thoughtworks.selenium.Selenium

class CommandHandlerFactory {
    static final List seleniumApi = Selenium.declaredMethods*.name
    static Long DEFAULT_TIMEOUT = 30000
    List<CommandHandler> handlers = []
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
        
        for (def functionName : seleniumApi) {
            Matcher match = ( functionName =~ /^(get|is)([A-Z].+)$/ ) 
            if (match.matches()) {
                def accessMethod = seleniumApi[functionName]
                def accessBlock = new FunctionBind(accessMethod, selenium)  //fnBind(accessMethod, seleniumApi);
                def baseName = match[0][2];
                def isBoolean = (match[0][1] == "is");
                def requiresTarget =  accessBlock.__method.parameterTypes.size() == 1 //(accessMethod.length == 1)
                
                this.registerAccessor(functionName, accessBlock)
                this._registerStoreCommandForAccessor(baseName, accessBlock, requiresTarget);

                def predicateBlock = this._predicateForAccessor(accessBlock, requiresTarget, isBoolean);
                this._registerAssertionsForPredicate(baseName, predicateBlock);
                this._registerWaitForCommandsForPredicate(seleniumApi, baseName, predicateBlock);
                
            }
        }
    }
    
    void _registerAllActions(Selenium selenium) {
        for (def functionName in seleniumApi) {
            Matcher match = (functionName =~ /^do([A-Z].+)$/)
            if (match.matches()) {
                def actionName = lcfirst(match[0][1])
                def actionMethod = seleniumApi[functionName]
                def dontCheckPopups = functionName in ['doWaitForPopup','getAlert','getConfirmation','doWaitForCondition','doWaitForPageLoad']
                //actionMethod.dontCheckAlertsAndConfirms
                def actionBlock = new FunctionBind(actionMethod, selenium)
                this.registerAction(actionName, actionBlock, false, dontCheckPopups)
                this.registerAction(actionName + "AndWait", actionBlock, true, dontCheckPopups)
            }
        }
    }
    
    
    
    
    def _waitForActionForPredicate(predicateBlock) {
        // Convert an isBlahBlah(target, value) function into a waitForBlahBlah(target, value) function.
        return { target, value ->
            
            def terminationCondition = { ->
                try {
                    return predicateBlock(target, value).isTrue
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
    
    def _registerWaitForCommandsForPredicate(seleniumApi, baseName, predicateBlock) {
        // Register a waitForBlahBlah and waitForNotBlahBlah based on the specified accessor.
        def waitForActionMethod = this._waitForActionForPredicate(predicateBlock);
        def waitForActionBlock = new FunctionBind(waitForActionMethod, seleniumApi);
        
        def invertedPredicateBlock = this._invertPredicate(predicateBlock);
        def waitForNotActionMethod = this._waitForActionForPredicate(invertedPredicateBlock);
        def waitForNotActionBlock = new FunctionBind(waitForNotActionMethod, seleniumApi);
        
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
        return { target, value ->
            PredicateResult result = predicateBlock(target, value);
            if (!result.isTrue) {
                throw new AssertionFailedError(result.message) //Assert.fail(result.message);
            }
        }
    }
    
    def _invertPredicate(predicateBlock) {
        // Given a predicate, return the negation of that predicate.
        // Leaves the message unchanged.
        // Used to create assertNot, verifyNot, and waitForNot commands.
        return { target, value ->
            PredicateResult result = predicateBlock(target, value);
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
    
    private String lcfirst(String action) {
        if(action == null) return null
        if(action.size() == 0) return action
        return action[0].toUpperCase() + action.substring(1)
    }
}

