package com.hornmicro.selenium.ui

import groovy.beans.Bindable

@Bindable
class TestSuiteModel {
    File file
    String name = ""
    ObservableList testCases = [ new TestCaseModel()]
}
