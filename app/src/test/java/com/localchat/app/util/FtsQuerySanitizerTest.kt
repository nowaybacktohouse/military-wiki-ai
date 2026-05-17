package com.localchat.app.util

import org.junit.Assert.*
import org.junit.Test

class FtsQuerySanitizerTest {

    @Test
    fun emptyString_returnsEmpty() {
        assertEquals("", FtsQuerySanitizer.sanitize(""))
    }

    @Test
    fun blankString_returnsEmpty() {
        assertEquals("", FtsQuerySanitizer.sanitize("   "))
    }

    @Test
    fun singleChar_returnsEmpty() {
        assertEquals("", FtsQuerySanitizer.sanitize("a"))
        assertEquals("", FtsQuerySanitizer.sanitize("т"))
    }

    @Test
    fun cyrillic_wrapsInQuotes() {
        val result = FtsQuerySanitizer.sanitize("танк")
        assertEquals("\"танк\"", result)
    }

    @Test
    fun multipleWords_wrapsEachInQuotes() {
        val result = FtsQuerySanitizer.sanitize("боевой танк")
        assertEquals("\"боевой\" \"танк\"", result)
    }

    @Test
    fun dashInWord_splitIntoTokens() {
        val result = FtsQuerySanitizer.sanitize("Т-90 танк")
        assertTrue(result.contains("\"Т\""))
        assertTrue(result.contains("\"90\""))
        assertTrue(result.contains("\"танк\""))
    }

    @Test
    fun specialChars_removed() {
        val result = FtsQuerySanitizer.sanitize("test*query:here")
        assertFalse(result.contains("*"))
        assertFalse(result.contains(":"))
    }

    @Test
    fun quotesInInput_removed() {
        val result = FtsQuerySanitizer.sanitize("\"abc\"")
        assertTrue(result.contains("\"abc\""))
    }

    @Test
    fun veryLongString_doesNotCrash() {
        val long = "абвгд ".repeat(1000)
        val result = FtsQuerySanitizer.sanitize(long)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun emoji_handledGracefully() {
        val result = FtsQuerySanitizer.sanitize("hello world")
        assertTrue(result.contains("\"hello\""))
        assertTrue(result.contains("\"world\""))
    }

    @Test
    fun onlySpecialChars_returnsEmpty() {
        assertEquals("", FtsQuerySanitizer.sanitize("--"))
        assertEquals("", FtsQuerySanitizer.sanitize("**"))
    }
}
