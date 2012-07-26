package com.hornmicro.selenium.driver.api

import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.codehaus.groovy.runtime.StackTraceUtils


class PatternMatcher {
    static Map strategies = PatternMatcher.declaredMethods.inject([:]) { map, method ->
        map[method.name] = method
        return map
    }
    String pattern
    Method strategy
    Closure matches
    
    static Boolean matches(pattern, actual) {
        return new PatternMatcher(pattern).matches(actual)
    }
        
    PatternMatcher(pattern) {
        this.selectStrategy(pattern)
    }
    
    void selectStrategy(pattern) {
        this.pattern = pattern;
        def strategyName = 'glob' // by default
        Matcher r
        if ( (r = (pattern =~ /^([a-z-]+):(.*)/)).matches() ) {
            def possibleNewStrategyName = r.group(1)
            def possibleNewPattern = r.group(2)
            if (PatternMatcher.strategies[possibleNewStrategyName]) {
                strategyName = possibleNewStrategyName;
                pattern = possibleNewPattern;
            }
        }
        Method matchStrategy = PatternMatcher.strategies[strategyName]
        if (!matchStrategy) {
            throw new SeleniumError("Cannot find PatternMatcher.strategies." + strategyName);
        }
        this.strategy = matchStrategy
        matchStrategy.invoke(this, pattern) //new matchStrategy(pattern);
    }
        
    /**
     * Exact matching, e.g. "exact:***"
     */
    void exact(expected) {
        this.matches = { actual ->
            return actual == expected
        }
    }
        
    /**
     * Match by regular expression, e.g. "regexp:^[0-9]+$"
     */
    void regexp(regexpString) {
        Pattern regexp = Pattern.compile(regexpString)
        this.matches = { actual ->
            return regexp.matcher(actual).matches()
        }
    }
    
    void regex(regexpString) {
        Pattern regexp = Pattern.compile(regexpString)
        this.matches = { actual ->
            return regexp.matcher(actual).matches()
        }
    }
            
    void regexpi(regexpString) {
        Pattern regexp = Pattern.compile(regexpString, Pattern.CASE_INSENSITIVE)
        this.matches = { actual ->
            return regexp.matcher(actual).matches()
        }
    }

    void regexi(regexpString) {
        Pattern regexp = Pattern.compile(regexpString, Pattern.CASE_INSENSITIVE)
        this.matches =  { actual ->
            return regexp.matcher(actual).matches()
        }
    }

    /**
     * "globContains" (aka "wildmat") patterns, e.g. "glob:one,two,*",
     * but don't require a perfect match; instead succeed if actual
     * contains something that matches globString.
     * Making this distinction is motivated by a bug in IE6 which
     * leads to the browser hanging if we implement *TextPresent tests
     * by just matching against a regular expression beginning and
     * ending with ".*".  The globcontains strategy allows us to satisfy
     * the functional needs of the *TextPresent ops more efficiently
     * and so avoid running into this IE6 freeze.
     */
    void globContains(globString) {
        Pattern regexp = Pattern.compile(regexpFromGlobContains(globString))
        this.matches = { actual ->
            return regexp.matcher(actual).matches()
        }
    }

    /**
     * "glob" (aka "wildmat") patterns, e.g. "glob:one,two,*"
     */
    void glob(globString) {
        Pattern regexp = Pattern.compile(regexpFromGlob(globString))
        this.matches = { actual ->
            return regexp.matcher(actual).matches()
        }
    }
        
    private String convertGlobMetaCharsToRegexpMetaChars(String glob) {
        String re = glob
        re = re.replaceAll(/([.^$+(){}\[\]\\|])/, "\\\\\$1")
        re = re.replaceAll(/\?/, "(.|[\\\\r\\\\n])")
        re = re.replaceAll(/\*/, "(.|[\\\\r\\\\n])*")
        return re
    }
    
    private String regexpFromGlobContains(globContains) {
        return convertGlobMetaCharsToRegexpMetaChars(globContains);
    }
    
    private String regexpFromGlob(glob) {
        return "^" + convertGlobMetaCharsToRegexpMetaChars(glob) + "\$"
    }
    
    static main(args) {
        try {
            assert PatternMatcher.matches('glob:some*', "something awesome") == true
            assert PatternMatcher.matches('glob:nope*', "something awesome") == false
            assert PatternMatcher.matches('regexpi:WhatEver', 'whatever') == true
            assert PatternMatcher.matches('regexpi:Nope', 'whatever') == false
            assert PatternMatcher.matches('regexp:WhatEver', 'whatever') == false
            assert PatternMatcher.matches('regexp:whatever', 'whatever') == true
            
            assert PatternMatcher.matches('From*Brisbane Airport * 4009, Australia',"From\nBrisbane Airport (BNE), 1 Alpinia Dr, Brisbane Airport QLD 4009, Australia") == true
            println "All good!" 
        } catch(e) {
            StackTraceUtils.sanitize(e)
            e.printStackTrace()
        }
    }
}
