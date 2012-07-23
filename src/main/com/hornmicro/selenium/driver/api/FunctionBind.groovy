package com.hornmicro.selenium.driver.api

import java.lang.reflect.Method;

import com.thoughtworks.selenium.Selenium;

class FunctionBind  {
    static Method[] methods = Selenium.getDeclaredMethods()
    Method __method
    Selenium selenium
    
    FunctionBind(String methodName, Selenium selenium) {
        this.selenium = selenium
        this.__method = methods.find { it.name == methodName}
    }
    
    void call(Object... args) {
        __method.invoke(selenium, args)
    }
}