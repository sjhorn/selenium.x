package com.hornmicro.selenium.model

import groovy.beans.Bindable
import groovy.xml.MarkupBuilder

import java.util.regex.Matcher

import org.ccil.cowan.tagsoup.Parser

@Bindable
class TestCaseModel {
    File file
    String selenese = ""
    String name = "Untitled"
    String baseURL = ""
    ObservableList tests = [ new TestModel(command:"hello", target:"what", value:"cool") ]
    TestModel selectedTest
    int currentTest
    TestState state = TestState.UNKNOWN
    boolean paused = false
    
    
    static TestCaseModel load(File file, String source=null, Node html=null) {
        if(source == null || html == null) {
            if(!file.exists()) {
                return null
            }
            source = file.text
            html = new XmlParser(new Parser()).parseText(source)
        }
        TestCaseModel testCase = new TestCaseModel()
        ObservableList cases = []
        html.'**'.tr.each { tr ->
            if(tr.children().size() == 3) {
                cases.add(new TestModel(
                    command: tr.td[0].text(),
                    target: tr.td[1].text(),
                    value: tr.td[2].text()
                ))
            } else {
                Matcher fileName = (file.name =~ /([^\.]+).*/)
                testCase.name = fileName.matches() ? fileName.group(1) : file.name    //tr.td[0].text()
            }
        }
        
        // <link rel="selenium.base" href="http://www.wotif.com/" />
        testCase.baseURL = html.head.link.find { it.@rel == 'selenium.base' }.@href
        testCase.selenese = source
        testCase.tests = cases
        testCase.file = file
        return testCase
    }
    
    Status save() {
        if(file) {
            String header = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head profile="http://selenium-ide.openqa.org/profiles/test-case">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="selenium.base" href="http://www.wotif.com/" />
<title>${name}</title>
</head>
<body>
<table cellpadding="1" cellspacing="1" border="1">
<thead>
<tr><td rowspan="1" colspan="3">${name}</td></tr>
</thead>
"""
            StringWriter writer = new StringWriter()  
            MarkupBuilder builder = new MarkupBuilder(writer) 
            builder.tbody() {
                tests.each { TestModel test ->
                    tr() {
                        td(test.command)
                        td(test.target ?: '')
                        td(test.value ?: '')
                    } 
                }
            }
            String footer = '''</table>
</body>
</html>
'''
            try {
                file.text = header + writer.toString() + footer
                return new Status(success:true, code: 0, message:"All good")
            } catch(Exception e) {
                return new Status(success:false, code: 2, message:e.message)
            }
        } else {
            return new Status(success:false, code: 1, message:"No file supplied")
        }
    }
}
