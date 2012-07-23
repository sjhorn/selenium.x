package com.hornmicro.selenium.driver.api

import java.lang.reflect.Method;

import com.thoughtworks.selenium.Selenium;

class FunctionBind  {
    Method __method
    Selenium selenium
    
    FunctionBind(Method method, Selenium selenium) {
        this.selenium = selenium
        this.__method = method
    }
    
    void call(Object... args) {
        __method.invoke(selenium, args)
    }
}