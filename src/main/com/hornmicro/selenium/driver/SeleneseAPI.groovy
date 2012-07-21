package com.hornmicro.selenium.driver

class SeleneseAPI {
/*
 Command.prototype.getDefinition = function() {
    if (this.command == null) return null;
    var commandName = this.command.replace(/AndWait$/, '');
    var api = Command.loadAPI();
    var r = /^(assert|verify|store|waitFor)(.*)$/.exec(commandName);
    if (r) {
        var suffix = r[2];
        var prefix = "";
        if ((r = /^(.*)NotPresent$/.exec(suffix)) != null) {
            suffix = r[1] + "Present";
            prefix = "!";
        } else if ((r = /^Not(.*)$/.exec(suffix)) != null) {
            suffix = r[1];
            prefix = "!";
        }
        var booleanAccessor = api[prefix + "is" + suffix];
        if (booleanAccessor) {
            return booleanAccessor;
        }
        var accessor = api[prefix + "get" + suffix];
        if (accessor) {
            return accessor;
        }
    }
    return api[commandName];
}

 
 Command.loadAPI = function() {
  if (!this.functions) {
    var document;
    var documents = this.apiDocuments;
    var functions = {};
    // document.length will be 1 by default, but will grow with plugins
    for (var d = 0; d < documents.length; d++) {
      // set the current document. again, by default this is the iedoc-core.xml
      document = documents[d];
      
      // <function name="someName">
      //   <param name="targetName">description</param>
      //   <param name="valueName">description</param> -- optional
      //   <return type="string">description</return> -- optional
      //   <comment>description for ide here</comment>
      // </function>
      var functionElements = document.documentElement.getElementsByTagName("function");
      for (var i = 0; i < functionElements.length; i++) {
        var element = functionElements.item(i);
        var def = new CommandDefinition(String(element.attributes.getNamedItem('name').value));
        
        var returns = element.getElementsByTagName("return");
        if (returns.length > 0) {
          var returnType = new String(returns.item(0).attributes.getNamedItem("type").value);
          returnType = returnType.replace(/string/, "String");
          returnType = returnType.replace(/number/, "Number");
          def.returnType = returnType;
          def.returnDescription = this.innerHTML(returns.item(0));
        }
        
        var comments = element.getElementsByTagName("comment");
        if (comments.length > 0) {
          def.comment = this.innerHTML(comments.item(0));
        }
        
        var params = element.getElementsByTagName("param");
        for (var j = 0; j < params.length; j++) {
          var paramElement = params.item(j);
          var param = {};
          param.name = String(paramElement.attributes.getNamedItem('name').value);
          param.description = this.innerHTML(paramElement);
          def.params.push(param);
        }
        
        functions[def.name] = def;

        // generate negative accessors
        if (def.name.match(/^(is|get)/)) {
          def.isAccessor = true;
          functions["!" + def.name] = def.negativeAccessor();
        }
        if (def.name.match(/^assert/)) { // only assertSelected should match
          var verifyDef = new CommandDefinition(def.name);
          verifyDef.params = def.params;
          functions["verify" + def.name.substring(6)] = verifyDef;
        }
      }
    }
    functions['assertFailureOnNext'] = new CommandDefinition('assertFailureOnNext');
    functions['verifyFailureOnNext'] = new CommandDefinition('verifyFailureOnNext');
    functions['assertErrorOnNext'] = new CommandDefinition('assertErrorOnNext');
    functions['verifyErrorOnNext'] = new CommandDefinition('verifyErrorOnNext');
    this.functions = functions;
  }
  return this.functions;
}


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
            ....

 */
}
