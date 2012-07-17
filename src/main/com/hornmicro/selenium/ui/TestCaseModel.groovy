package com.hornmicro.selenium.ui

import groovy.beans.Bindable

@Bindable
class TestCaseModel {
    File file
    String selenese = ""
    String name = "Untitled"
    ObservableList tests = [ new TestModel(command:"hello", target:"what", value:"cool") ]
}
