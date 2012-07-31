package com.hornmicro.selenium.model

import groovy.beans.Bindable
import org.ccil.cowan.tagsoup.Parser

@Bindable
class TestSuiteModel {
    Boolean dirty = false
    String browser = "safari"
    File file
    String name = "Test Suite"
    ObservableList testCases = new ObservableList([ new TestCaseModel() ])
    TestCaseModel selectedTestCase = testCases[0]
    TestCaseModel lastRun
    Integer runs = 0
    Integer failures = 0
    
    Status open(File file) {
        if(file.exists()) {
            String source = file.text
            Node html = new XmlParser(new Parser()).parseText(source)
            def profile = html?.head?.@profile
            def table = html?.body?.table
            if(profile && profile.size() && profile.get(0) == 'http://selenium-ide.openqa.org/profiles/test-case') {
                
                setTestCases(new ObservableList([TestCaseModel.load(file, source, html)]))
                setSelectedTestCase(this.testCases[0])
                
                return new Status(success:true, code:0, message:"All good")
                
            } else if(table && table.size() && table.@id?.get(0) == 'suiteTable') {
                this.name = html.'**'?.b?.get(0)?.text()
                List testCases = [] 
                html.'**'.a.each { a ->
                    if(a.@href) {
                        TestCaseModel tcm = TestCaseModel.load(new File(file.getParentFile(), a.@href))
                        if(tcm) {
                            testCases.add(tcm)
                        }
                    }
                }
                setTestCases(new ObservableList(testCases))
                setSelectedTestCase(this.testCases[0]) 
                setFile(file)
                return new Status(success: true, code: 0, message: "All good")
            } else {
                return new Status(
                    success: false, 
                    code: 1, 
                    message: "Failed to open file $file\n\n"+
                        "It does not appear to be a Test Suite or Test Case"
                )
            }
        } else {
            return new Status(
                success: false, 
                code: 2, 
                message: "Failed to open file $file\n\nI can't seem to find it :("
            )
        }  
    }
}
