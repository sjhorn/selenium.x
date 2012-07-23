package com.hornmicro.selenium

import java.util.regex.Matcher

import com.thoughtworks.selenium.Selenium

class ParseAPI {

    static main(args) {
        List methods = Selenium.declaredMethods*.name
        List ignoredMethods = [ 'start', 'stop', 'setExtensionJs',
            'showContextualBanner', 'getLog', 'captureNetworkTraffic',
            'addCustomRequestHeader']
        
        List nonWaitActions = ['open', 'selectWindow', 'chooseCancelOnNextConfirmation', 
            'answerOnNextPrompt', 'close', 'setContext', 'setTimeout', 'selectFrame']
        
        List commands = []
        Matcher r
        methods.each { String func ->
            if(func in ignoredMethods) return
            if(func.matches(/do[A-Z]/)) {
                def action = func.substring(2, 1).toLowerCase() + func.substring(3)
                commands.push(action)
                if(!action.matches(/^waitFor/) && !(action in nonWaitActions) ) {
                    commands.push(action+ "AndWait")
                }    
            } else if (func.matches(/^assert.+/)) {
                commands.push(func)
                commands.push("verify" + func.substring(6))
            } else if ( (r = (func =~ /^(get|is)(.+)$/)).matches() ) {
                def base = r[0][2]
                commands.push("assert" + base)
                commands.push("verify" + base)
                commands.push("store" + base)
                commands.push("waitFor" + base)
                Matcher r2
                if ( (r2 = (func =~ /^is(.*)Present$/)).matches() ) {
                    base = r2[0][1]
                    commands.push("assert" + base + "NotPresent")
                    commands.push("verify" + base + "NotPresent")
                    commands.push("waitFor" + base + "NotPresent")
                } else {
                    commands.push("assertNot" + base)
                    commands.push("verifyNot" + base)
                    commands.push("waitForNot" + base)
                }
            }
        }
        commands.push("pause")
        commands.push("store")
        commands.push("echo")
        commands.push("break")
        
        commands.sort().each {
            if(it.startsWith("waitFor")) 
                println it 
        }
        
        
        /*
         _loadSeleniumCommands: function() {
            var commands = [];
            
            var nonWaitActions = ['open', 'selectWindow', 'chooseCancelOnNextConfirmation', 'answerOnNextPrompt', 'close', 'setContext', 'setTimeout', 'selectFrame'];
            
            for (func in this.editor.seleniumAPI.Selenium.prototype) {
                //this.log.debug("func=" + func);
                var r;
                if (func.match(/^do[A-Z]/)) {
                    var action = func.substr(2,1).toLowerCase() + func.substr(3);
                    commands.push(action);
                    if (!action.match(/^waitFor/) && nonWaitActions.indexOf(action) < 0) {
                        commands.push(action + "AndWait");
                    }
                } else if (func.match(/^assert.+/)) {
                    commands.push(func);
                    commands.push("verify" + func.substr(6));
                } else if ((r = func.match(/^(get|is)(.+)$/))) {
                    var base = r[2];
                    commands.push("assert" + base);
                    commands.push("verify" + base);
                    commands.push("store" + base);
                    commands.push("waitFor" + base);
                    var r2;
                    if ((r = func.match(/^is(.*)Present$/))) {
                        base = r[1];
                        commands.push("assert" + base + "NotPresent");
                        commands.push("verify" + base + "NotPresent");
                        commands.push("waitFor" + base + "NotPresent");
                    } else {
                        commands.push("assertNot" + base);
                        commands.push("verifyNot" + base);
                        commands.push("waitForNot" + base);
                    }
                }
            }
            
            commands.push("pause");
            commands.push("store");
            commands.push("echo");
            commands.push("break"); 
         */
        
        /*
        Node html = new XmlParser(new Parser()).parse(new File("api/iedoc.xml"))
        
        def core = []
        html.'**'.function.@name.each {
            core.add(it)
            println it
        }
        println "\n\n\n"
        
        html = new XmlParser(new Parser()).parse(new File("api/iedoc-core.xml"))
        
        def other = []
        html.'**'.function.@name.each {
            other.add(it)
        }
        
        println "\n\nOther>>>"
        other.each {
            if(!(it in core))
                println it 
        }
        
        println "\n\nCore>>>"
        core.each {
            if(!(it in other))
                println it
        }
        core.addAll(other)
        
        println "\n\nCore not Selenium>>>"
        List methods = Selenium.declaredMethods*.name
        core.each {
            if(!(it in methods))
                println it
        }
        
        println "\n\nSelenium not Core>>>"
        methods.each {
            if(!(it in core))
                println it
        }
        */
                
    }

}

/*
click
doubleClick
contextMenu
clickAt
doubleClickAt
contextMenuAt
fireEvent
focus
keyPress
shiftKeyDown
shiftKeyUp
metaKeyDown
metaKeyUp
altKeyDown
altKeyUp
controlKeyDown
controlKeyUp
keyDown
keyUp
mouseOver
mouseOut
mouseDown
mouseDownRight
mouseDownAt
mouseDownRightAt
mouseUp
mouseUpRight
mouseUpAt
mouseUpRightAt
mouseMove
mouseMoveAt
type
typeKeys
setSpeed
getSpeed
check
uncheck
select
addSelection
removeSelection
removeAllSelections
submit
open
openWindow
selectWindow
selectPopUp
deselectPopUp
selectFrame
getWhetherThisFrameMatchFrameExpression
getWhetherThisWindowMatchWindowExpression
waitForPopUp
chooseCancelOnNextConfirmation
chooseOkOnNextConfirmation
answerOnNextPrompt
goBack
refresh
close
isAlertPresent
isPromptPresent
isConfirmationPresent
getAlert
getConfirmation
getPrompt
getLocation
getTitle
getBodyText
getValue
getText
highlight
getEval
isChecked
getTable
getSelectedLabels
getSelectedLabel
getSelectedValues
getSelectedValue
getSelectedIndexes
getSelectedIndex
getSelectedIds
getSelectedId
isSomethingSelected
getSelectOptions
getAttribute
isTextPresent
isElementPresent
isVisible
isEditable
getAllButtons
getAllLinks
getAllFields
getAttributeFromAllWindows
dragdrop
setMouseSpeed
getMouseSpeed
dragAndDrop
dragAndDropToObject
windowFocus
windowMaximize
getAllWindowIds
getAllWindowNames
getAllWindowTitles
getHtmlSource
setCursorPosition
getElementIndex
isOrdered
getElementPositionLeft
getElementPositionTop
getElementWidth
getElementHeight
getCursorPosition
getExpression
getXpathCount
getCssCount
assignId
allowNativeXpath
ignoreAttributesWithoutValue
waitForCondition
setTimeout
waitForPageToLoad
waitForFrameToLoad
getCookie
getCookieByName
isCookiePresent
createCookie
deleteCookie
deleteAllVisibleCookies
setBrowserLogLevel
runScript
addLocationStrategy
captureEntirePageScreenshot
rollup
addScript
removeScript
useXpathLibrary
setContext
attachFile
captureScreenshot
captureScreenshotToString
captureEntirePageScreenshotToString
shutDownSeleniumServer
retrieveLastRemoteControlLogs
keyDownNative
keyUpNative
keyPressNative
 

Other>>>
pause
break
store
echo
assertSelected
assertFailureOnNext
assertErrorOnNext


Core>>>
setContext
attachFile
captureScreenshot
captureScreenshotToString
captureEntirePageScreenshotToString
shutDownSeleniumServer
retrieveLastRemoteControlLogs
keyDownNative
keyUpNative
keyPressNative


Core not Selenium>>>
pause
break
store
echo
assertSelected
assertFailureOnNext
assertErrorOnNext


Selenium not Core>>>
start
start
start
stop
setExtensionJs
showContextualBanner
showContextualBanner
getLog
captureNetworkTraffic
addCustomRequestHeader

 */
