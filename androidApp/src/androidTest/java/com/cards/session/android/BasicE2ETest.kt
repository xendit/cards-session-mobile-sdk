package com.cards.session.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BasicE2ETest {
    
    @Test
    fun testContextLoads() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.cards.session.android", appContext.packageName)
    }
}