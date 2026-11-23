package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Samudra", appName)
  }

  @Test
  fun `profile validation enforces rules parity`() {
    assertEquals(null, com.example.data.ProfileValidation.validateFullName("Nabila Samudra"))
    assertEquals(null, com.example.data.ProfileValidation.validateUsername("nabila_laut"))
    assertEquals(
      "Username hanya boleh berisi huruf, angka, titik, atau garis bawah.",
      com.example.data.ProfileValidation.validateUsername("nabila laut")
    )
  }
}
