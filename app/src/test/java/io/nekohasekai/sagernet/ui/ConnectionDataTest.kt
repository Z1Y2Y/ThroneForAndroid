package io.nekohasekai.sagernet.ui

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionDataTest {
    @Test
    fun sanitizesMissingAndNullFieldsFromCoreJson() {
        val parsed = Gson().fromJson(
            """{"active":[{"id":"with-null-lists","processPaths":null,"chain":null,"packages":null},{"id":"missing-lists"},null],"closed":null}""",
            ConnectionSnapshot::class.java,
        ).sanitized()

        assertEquals(2, parsed.active.size)
        assertTrue(parsed.closed.isEmpty())
        parsed.active.forEach { record ->
            assertTrue(record.processPaths.isEmpty())
            assertTrue(record.chain.isEmpty())
            assertTrue(record.packages.isEmpty())
            assertEquals("", record.domain)
        }

        val display = ConnectionDisplay(parsed.active.first(), 0L, 0L, active = true).sanitized()
        assertTrue(display.record.chain.isEmpty())
        assertTrue(display.record.processPaths.isEmpty())
        assertTrue(display.record.packages.isEmpty())
    }
}
