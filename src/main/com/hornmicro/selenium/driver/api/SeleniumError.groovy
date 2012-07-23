package com.hornmicro.selenium.driver.api

class SeleniumError extends RuntimeException {
    SeleniumError(Throwable e) {
        super(e)
    }
}