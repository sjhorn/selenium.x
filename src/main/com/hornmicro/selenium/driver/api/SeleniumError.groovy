package com.hornmicro.selenium.driver.api

class SeleniumError extends RuntimeException {
    SeleniumError(String message) {
        super(message)
    }
}