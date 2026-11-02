package com.example.mol.widget
import com.example.mol.navigation.Screen
import org.junit.Assert.*
import org.junit.Test
class Batch05WidgetRouteTest {
 @Test fun `missing or invalid reading opens the existing Quran list`() {
  assertEquals(Screen.Quran.route,widgetReadingRoute(null,null));assertEquals(Screen.Quran.route,widgetReadingRoute(1 to 8,7))
  assertEquals(Screen.Quran.route,widgetReadingRoute(0 to 1,7));assertEquals(Screen.Quran.route,widgetReadingRoute(1 to 0,7))
 }
 @Test fun `valid local pair opens the existing reader without changing progress`() {
  assertEquals(Screen.QuranReader.createRoute(1,7),widgetReadingRoute(1 to 7,7))
 }
}
