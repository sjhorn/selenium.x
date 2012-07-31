package com.hornmicro.selenium.model

import groovy.beans.Bindable
import groovy.transform.ToString

@Bindable
@ToString
class TestModel {
    String command
    String target
    String value
    TestState state = TestState.UNKNOWN
}
