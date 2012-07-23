package com.hornmicro.selenium.driver.api

class PredicateResult {
    public Boolean isTrue
    String message
    
    PredicateResult(Boolean isTrue,String message) {
        this.isTrue = isTrue;
        this.message = message;
    }
}
