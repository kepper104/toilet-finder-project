package com.kepper104.toiletseverywhere

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletDistanceString
import com.kepper104.toiletseverywhere.data.getToiletNameString
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletPriceString
import com.kepper104.toiletseverywhere.data.getToiletWorkingHoursString
import com.kepper104.toiletseverywhere.domain.model.Toilet

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class ToiletFormattersTests {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.kepper104.toiletseverywhere", appContext.packageName)
    }

    @Test
    fun toiletDistanceMeters_isCorrect1() {
        val userPosition = LatLng(55.751244, 37.618423)
        val toiletPosition = LatLng(55.762980, 37.668998)
        val expectedDistance = 3433
        assertEquals(expectedDistance, getToiletDistanceMeters(userPosition, toiletPosition))
    }

    @Test
    fun toiletDistanceMeters_isCorrect2() {
        val userPosition = LatLng(55.762980, 37.668998)
        val toiletPosition = LatLng(55.770140, 37.626819)
        val expectedDistance = 2764
        assertEquals(expectedDistance, getToiletDistanceMeters(userPosition, toiletPosition))
    }

    @Test
    fun toiletDistanceString_isCorrect1() {
        val distance = 2
        val expectedString = "2m"
        assertEquals(expectedString, getToiletDistanceString(distance))
    }

    @Test
    fun toiletDistanceString_isCorrect2() {
        val distance = 999
        val expectedString = "999m"
        assertEquals(expectedString, getToiletDistanceString(distance))
    }

    @Test
    fun toiletDistanceString_isCorrect3() {
        val distance = 1000
        val expectedString = "1km"
        assertEquals(expectedString, getToiletDistanceString(distance))
    }

    @Test
    fun toiletDistanceString_isCorrect4() {
        val distance = 1500
        val expectedString = "1km"
        assertEquals(expectedString, getToiletDistanceString(distance))
    }

    @Test
    fun toiletDistanceString_isCorrect5() {
        val distance = 2000
        val expectedString = "2km"
        assertEquals(expectedString, getToiletDistanceString(distance))
    }

    @Test
    fun toiletOpenString_isCorrect1() {
        val toilet = Toilet(
            openingTime = LocalTime.of(8, 0),
            closingTime = LocalTime.of(21, 0)
        )
        val currentTime = LocalTime.of(15, 0)
        val expectedString = "Open"
        assertEquals(expectedString, getToiletOpenString(toilet, currentTime))
    }

    @Test
    fun toiletOpenString_isCorrect2() {
        val toilet = Toilet(
            openingTime = LocalTime.of(8, 0),
            closingTime = LocalTime.of(21, 0)
        )
        val currentTime = LocalTime.of(6, 0)
        val expectedString = "Closed"
        assertEquals(expectedString, getToiletOpenString(toilet, currentTime))
    }
    @Test
    fun toiletOpenString_isCorrect3() {
        val toilet = Toilet(
            openingTime = LocalTime.of(8, 0),
            closingTime = LocalTime.of(21, 0)
        )
        val currentTime = LocalTime.of(8, 0)
        val expectedString = "Open"
        assertEquals(expectedString, getToiletOpenString(toilet, currentTime))
    }

    @Test
    fun toiletPriceString_isCorrect1() {
        val toilet = Toilet(cost = 0)
        val expectedString = "Free"
        assertEquals(expectedString, getToiletPriceString(toilet))
    }

    @Test
    fun toiletPriceString_isCorrect2() {
        val toilet = Toilet(cost = 1)
        val expectedString = "1₽"
        assertEquals(expectedString, getToiletPriceString(toilet))
    }
    @Test
    fun toiletPriceString_isCorrect3() {
        val toilet = Toilet(cost = 1234567890)
        val expectedString = "1234567890₽"
        assertEquals(expectedString, getToiletPriceString(toilet))
    }

    @Test
    fun toiletNameString_isCorrect1() {
        val toilet = Toilet(isPublic = true, placeName = "Test failed if you see this")
        val expectedString = "Public Toilet"
        assertEquals(expectedString, getToiletNameString(toilet))
    }
    @Test
    fun toiletNameString_isCorrect2() {
        val toilet = Toilet(isPublic = false, placeName = "ToiletNameHere")
        val expectedString = "ToiletNameHere"
        assertEquals(expectedString, getToiletNameString(toilet))
    }

    @Test
    fun toiletWorkingHoursString_isCorrect1() {
        val toilet = Toilet(
            openingTime = LocalTime.of(8, 0),
            closingTime = LocalTime.of(21, 0)
        )
        val expectedString = "08:00 - 21:00"
        assertEquals(expectedString, getToiletWorkingHoursString(toilet))
    }

    @Test
    fun toiletWorkingHoursString_isCorrect2() {
        val toilet = Toilet(
            openingTime = LocalTime.of(4, 0),
            closingTime = LocalTime.of(23, 0)
        )
        val expectedString = "04:00 - 23:00"
        assertEquals(expectedString, getToiletWorkingHoursString(toilet))
    }

    @Test
    fun toiletWorkingHoursString_isCorrect3() {
        val toilet = Toilet(
            openingTime = LocalTime.of(8, 0),
            closingTime = LocalTime.of(21, 0)
        )
        val expectedString = "From 08:00 to 21:00"
        assertEquals(
            expectedString,
            getToiletWorkingHoursString(
                toilet,
                includeFromToLabels = true)
        )
    }
    @Test
    fun toiletWorkingHoursString_isCorrect4() {
        val toilet = Toilet(
            openingTime = LocalTime.of(0, 0),
            closingTime = LocalTime.of(23, 59)
        )
        val expectedString = "From 00:00 to 23:59"
        assertEquals(
            expectedString,
            getToiletWorkingHoursString(
                toilet,
                includeFromToLabels = true)
        )
    }
}