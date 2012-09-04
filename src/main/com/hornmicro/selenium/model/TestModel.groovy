package com.hornmicro.selenium.model

import groovy.beans.Bindable
import groovy.transform.ToString
import groovy.xml.MarkupBuilder
import org.ccil.cowan.tagsoup.Parser

@Bindable
@ToString
class TestModel {
    String command
    String target
    String value
    TestState state = TestState.UNKNOWN
    
    
    String toString() {
        StringWriter writer = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(writer)
        builder.tr() {
            td(command)
            td(target ?: '')
            td(value ?: '')
        }
        return writer.toString()
    }
    
    static List<TestModel> parse(String str) {
        try {
            Node html = new XmlParser(new Parser()).parseText(str)
            List<TestModel> tests = []
            html.'**'.tr.each { tr ->
                if(tr?.children()?.size() == 3) {
                    tests.add(new TestModel(
                        command: tr.td[0].text(),
                        target: tr.td[1].text(),
                        value: tr.td[2].text()
                    ))
                }
            }
            return tests
            
        } catch(e) {
            e.printStackTrace()
            return null
        }
    }
}
