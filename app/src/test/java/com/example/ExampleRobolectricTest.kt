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
    assertEquals("MediaMaster_#MYKSON#", appName)
  }

  @Test
  fun `document type detection handles various extensions`() {
    assertEquals(com.example.data.model.DocumentType.PDF, com.example.data.model.DocumentType.fromExtensionOrMime("report.pdf"))
    assertEquals(com.example.data.model.DocumentType.MARKDOWN, com.example.data.model.DocumentType.fromExtensionOrMime("notes.md"))
    assertEquals(com.example.data.model.DocumentType.CSV, com.example.data.model.DocumentType.fromExtensionOrMime("data.csv"))
    assertEquals(com.example.data.model.DocumentType.JSON, com.example.data.model.DocumentType.fromExtensionOrMime("config.json"))
    assertEquals(com.example.data.model.DocumentType.CODE, com.example.data.model.DocumentType.fromExtensionOrMime("Main.kt"))
  }

  @Test
  fun `csv parser parses headers and rows properly`() {
    val sampleCsv = "Name,Age,Role\nAlice,29,Engineer\nBob,34,Designer"
    val parsed = com.example.document.DocumentParser.parseCsv(sampleCsv)
    assertEquals(listOf("Name", "Age", "Role"), parsed.headers)
    assertEquals(2, parsed.rows.size)
    assertEquals(listOf("Alice", "29", "Engineer"), parsed.rows[0])
  }
}
