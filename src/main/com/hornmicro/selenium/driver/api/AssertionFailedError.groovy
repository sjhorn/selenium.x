package com.hornmicro.selenium.driver.api

class AssertionFailedError extends RuntimeException {
    AssertionFailedError(Throwable e) {
        super(e)
    }
}
