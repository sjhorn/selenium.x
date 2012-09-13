package com.hornmicro.selenium.model

import groovy.beans.Bindable
import groovy.xml.MarkupBuilder

import org.ccil.cowan.tagsoup.Parser

@Bindable
class TestSuiteModel {
    
    String browser = "htmlunit"
    File file
    String name = "Test Suite"
    ObservableList testCases = new ObservableList([ new TestCaseModel() ])
    
    TestCaseModel selectedTestCase = testCases[0]
    TestCaseModel lastRun
    Integer runs = 0
    Integer failures = 0
    Boolean dirty = false
    long delay = 0
    
    void clear() {
        setFile(null)
        setName("Test Suite")
        testCases.clear()
        testCases.add(new TestCaseModel())
        setSelectedTestCase(testCases[0])
        setLastRun(null)
        setRuns(0)
        setFailures(0)
        setDirty(false)
    }
    
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
    
    Status save() {
        if(file) {
            String header = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <meta content="text/html; charset=UTF-8" http-equiv="content-type" />
  <title>${name}</title>
</head>
<body>
<table id="suiteTable" cellpadding="1" cellspacing="1" border="1" class="selenium">
"""
            StringWriter writer = new StringWriter()
            MarkupBuilder builder = new MarkupBuilder(writer)
            builder.tbody() {
                tr() { b(name) }
                testCases.each { TestCaseModel testCase ->
                    tr() {
                        td() {
                            a(href:relativize(file, testCase.file)) { 
                                fileWithoutExtension(testCase.file)
                            }
                        }
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
                return new Status(success:false, code: 1, message:e.message)
            }
        } else {
            return new Status(success:false, code: 1, message:"No file supplied")
        }
    }
    
    private String relativize(File base, File relative) {
        return base.toURI().relativize(relative.toURI())
    }
    private String fileWithoutExtension(File file) {
        return file?.name?.split('\\.')?.getAt(0) ?: ''
    }
    
}
