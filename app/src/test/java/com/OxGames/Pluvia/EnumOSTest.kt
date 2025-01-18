package com.OxGames.Pluvia

import com.OxGames.Pluvia.enums.OS
import java.util.EnumSet
import junit.framework.TestCase.assertEquals
import org.junit.Test

class EnumOSTest {

    @Test
    fun testOSFromString() {
        assertEquals(EnumSet.of(OS.windows, OS.macos, OS.linux), OS.from("windows,macos,linux"))
        assertEquals(EnumSet.of(OS.windows, OS.macos), OS.from("windows,macos"))
        assertEquals(EnumSet.of(OS.windows), OS.from("windows"))
        assertEquals(EnumSet.of(OS.none), OS.from(null))
        assertEquals(EnumSet.of(OS.none), OS.from(""))
        assertEquals(EnumSet.of(OS.none), OS.from("invalid"))
    }
}
