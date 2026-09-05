package com.capstone.nik.mixology.play

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PlayStoreIntentsTest {

    @Test
    fun openPlayStoreListing_startsMarketDetails() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        openPlayStoreListing(activity)
        val started = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals("com.android.vending", started.`package`)
        assertEquals("market://details?id=${activity.packageName}", started.dataString)
    }

    @Test
    fun findActivity_unwrapsContextWrapper() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        assertEquals(activity, activity.findActivity())
        assertEquals(activity, ContextWrapper(activity).findActivity())
        assertNull(activity.applicationContext.findActivity())
    }
}
