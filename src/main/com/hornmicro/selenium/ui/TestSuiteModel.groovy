package com.hornmicro.selenium.ui

import groovy.beans.Bindable
import groovy.xml.*
import org.ccil.cowan.tagsoup.Parser

@Bindable
class TestSuiteModel {
    File file
    String name = ""
    ObservableList testCases = new ObservableList([ new TestCaseModel() ])
    TestCaseModel selectedTestCase = testCases[0]
    
    
    void open(File file) {
        if(file.exists()) {
            def source = file.text
            def html = new XmlParser(new Parser()).parseText(source)
            if(html.head.@profile?.getAt(0) == 'http://selenium-ide.openqa.org/profiles/test-case') {
                
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
                
                testCase.selenese = source
                testCase.tests = cases
                setFile(file)
                setTestCases(new ObservableList([testCase]))
                setSelectedTestCase(this.testCases[0])
                
            } else if(html.body.table.@id?.getAt(0) == 'suiteTable') {
                println "suite"
                
            } else {
                println "Failed to open file"
            }
        }    
    }
}
