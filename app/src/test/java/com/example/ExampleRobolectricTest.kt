package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.SampleProjectType
import com.example.validator.SampleProjectHelper
import com.example.validator.ZipProjectValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Android ZIP to APK", appName)
  }

  @Test
  fun `validate valid compose sample project`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val uri = SampleProjectHelper.createSampleZip(context, SampleProjectType.VALID_COMPOSE_PROJECT)
    val result = ZipProjectValidator.validateZipStream(context, uri, "valid-compose.zip")
    assertTrue(result.isValid)
    assertTrue(result.hasSettingsGradle)
    assertTrue(result.hasGradlew)
    assertTrue(result.hasAppModule)
  }

  @Test
  fun `validate incomplete sample project returns false with bengali explanation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val uri = SampleProjectHelper.createSampleZip(context, SampleProjectType.INCOMPLETE_SOURCE_ONLY)
    val result = ZipProjectValidator.validateZipStream(context, uri, "incomplete.zip")
    assertFalse(result.isValid)
    assertTrue(result.bengaliSummary.isNotEmpty())
  }
}
