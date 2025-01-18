package com.OxGames.Pluvia

import android.content.pm.ActivityInfo
import com.OxGames.Pluvia.ui.enums.Orientation
import java.util.EnumSet
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class EnumOrientationTest {

    @Test
    fun testOrientationFunctions() {
        assertEquals(Orientation.PORTRAIT, Orientation.fromActivityInfoValue(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT))
        assertEquals(Orientation.UNSPECIFIED, Orientation.fromActivityInfoValue(-999))

        val orientationSet = EnumSet.of(Orientation.PORTRAIT, Orientation.LANDSCAPE)
        val asInt = Orientation.toInt(orientationSet)
        val backToSet = Orientation.fromInt(asInt)
        assertEquals(orientationSet, backToSet)

        assertTrue(Orientation.PORTRAIT.angleRanges.any { 0 in it })
        assertTrue(Orientation.LANDSCAPE.angleRanges.any { 90 in it })
        assertTrue(Orientation.REVERSE_PORTRAIT.angleRanges.any { 180 in it })
        assertTrue(Orientation.REVERSE_LANDSCAPE.angleRanges.any { 270 in it })
    }
}
