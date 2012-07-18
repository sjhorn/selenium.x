package com.hornmicro.selenium.model

import groovy.beans.Bindable
import org.ccil.cowan.tagsoup.Parser

@Bindable
class TestCaseModel {
    File file
    String selenese = ""
    String name = "Untitled"
    String baseURL = ""
    ObservableList tests = [ new TestModel(command:"hello", target:"what", value:"cool") ]
    
    
    static TestCaseModel load(File file, String source=null, Node html=null) {
        if(source == null || html == null) {
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
                testCase.name = tr.td[0].text()
            }
        }
        
        // <link rel="selenium.base" href="http://www.wotif.com/" />
        testCase.baseURL = html.head.link.find { it.@rel == 'selenium.base' }.@href
        testCase.selenese = source
        testCase.tests = cases
        testCase.file = file
        return testCase
    }
}
