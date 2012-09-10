package com.hornmicro.selenium.driver

import java.lang.reflect.Method
import java.util.regex.Matcher

import org.ccil.cowan.tagsoup.Parser

import com.thoughtworks.selenium.Selenium

class SeleneseAPI {
    Map<String, CommandDefinition> commands = [:]
    List<String> commandNames = []
    
    SeleneseAPI() {
        parseAPIDoc()
        addGeneratedCommands()
        commandNames.sort(true)
    }
    
    void parseAPIDoc() {
        Node html = new XmlParser( /*new Parser()*/ ).parse(new File("api/iedoc-core.xml"))
        
        // <function name="someName">
        //   <param name="targetName">description</param>
        //   <param name="valueName">description</param> -- optional
        //   <return type="string">description</return> -- optional
        //   <comment>description for ide here</comment>
        // </function>
        html.'**'.function.each { Node function ->
            def params = [:]
            function.param.collect { Node param ->
                
                String description = param.children().collect { it instanceof String ? it : it.text() }.join(" ")
                
                return params[param.@name] = new Param(name: param.@name, description: description)
            }
            CommandDefinition cd = new CommandDefinition(
                name: function.@name,
                returnType: function['return'].@type,
                returnComment: function['return'].text(),
                params: params,
            )
            commands[function.@name] =  cd
            commandNames.add(function.@name)
        }
    } 
    
    void addGeneratedCommand(String name, CommandDefinition commandDefinition = null) {
        commands[name] = commandDefinition
        commandNames.add(name)
    }
    
    void addGeneratedCommands() {
        List ignoredMethods = [ 'start', 'stop', 'setExtensionJs',
            'showContextualBanner', 'getLog', 'captureNetworkTraffic',
            'addCustomRequestHeader']
        
        List nonWaitActions = ['open', 'selectWindow', 'chooseCancelOnNextConfirmation',
            'answerOnNextPrompt', 'close', 'setContext', 'setTimeout', 'selectFrame']
        
        List commandNameList = []
        Matcher r
        
        Map commandsCopy = [:]
        commandsCopy.putAll(commands)
        commandsCopy.each { String func, CommandDefinition commandDefinition ->
            if(func in ignoredMethods) return
            if(func.matches(/do[A-Z]/)) {
                def action = func.substring(2, 1).toLowerCase() + func.substring(3)
                addGeneratedCommand(action)
                if(!action.matches(/^waitFor/) && !(action in nonWaitActions) ) {
                    addGeneratedCommand(action+ "AndWait")
                }
            } else if (func.matches(/^assert.+/)) {
                addGeneratedCommand("verify" + func.substring(6))
            } else if ( (r = (func =~ /^(get|is)(.+)$/)).matches() ) {
                def base = r[0][2]
                addGeneratedCommand("assert" + base, commandDefinition)
                addGeneratedCommand("verify" + base, commandDefinition)
                addGeneratedCommand("store" + base, commandDefinition)
                addGeneratedCommand("waitFor" + base, commandDefinition)
                Matcher r2
                if ( (r2 = (func =~ /^is(.*)Present$/)).matches() ) {
                    base = r2[0][1]
                    addGeneratedCommand("assert" + base + "NotPresent", commandDefinition)
                    addGeneratedCommand("verify" + base + "NotPresent", commandDefinition)
                    addGeneratedCommand("waitFor" + base + "NotPresent", commandDefinition)
                } else {
                    addGeneratedCommand("assertNot" + base, commandDefinition)
                    addGeneratedCommand("verifyNot" + base, commandDefinition)
                    addGeneratedCommand("waitForNot" + base, commandDefinition)
                }
            }
        }
        addGeneratedCommand("pause")
        addGeneratedCommand("store")
        addGeneratedCommand("echo")
        addGeneratedCommand("break")
    }
    
    String getReferenceFor(String commandName) {
        CommandDefinition comDef = commands[commandName]
        def paramNames = []
        comDef.params.each { k,v ->
            paramNames.push(k)
        }
        def originalParamNames = paramNames.join(", ")
        
        if ( comDef && (comDef.name =~ /^is|get/).matches() ) { // accessor
                if ( (commandName =~ /^store/).matches() ) {
                    paramNames.push("variableName")
                } else if ( (commandName =~ /^(assert|verify|waitFor)/).matches() ) {
                    if (comDef.name.startsWith("get")) {
                        paramNames.push("pattern")
                    }
                }
        }
        def note = ""
        
        if (comDef && commandName != comDef.name) {
            note = "<dt>Generated from <strong>" + comDef.name + "(" +
                originalParamNames + ")</strong></dt>"
        }
        
        def params = "";
        if (comDef.params.size() > 0) {
            params += "<div>Arguments:</div><ul>"
            comDef.params.each { name, param ->
                params += "<li>" + name + " - " + param.description + "</li>";
            }
            params += "</ul>"
        }
        def returns = ""
        if (comDef.returnComment) {
            returns += "<dl><dt>Returns:</dt><dd>" + comDef.returnComment + "</dd></dl>"
        }
        return "<dl><dt><strong>" + (commandName ?: comDef.name) + "(" +
            paramNames.join(", ") + ")</strong></dt>" +
            note +
            '<dd style="margin:5px;">' +
            params + returns +
            (comDef.comment ?: '') + "</dd></dl>"
    }
    
    static void main(args) {
        SeleneseAPI sapi = new SeleneseAPI()
        
        println sapi.getReferenceFor("assertElementPresent")
//        sapi.commands.each { k,v ->
//            println "$k -> ${v.method}"
//        }
    }
    
}

class CommandDefinition {
    String name
    String returnType
    String returnComment
    Map<String, Param> params = [:]
    String comment
    Method method
}

class Param {
    String name
    String description
}