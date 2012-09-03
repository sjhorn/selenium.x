package com.hornmicro.selenium.model

import groovy.beans.Bindable
import groovy.transform.ToString
import groovy.xml.MarkupBuilder

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
}
