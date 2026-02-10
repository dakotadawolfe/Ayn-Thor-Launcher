package com.launcher.aynthords.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSpecV1ParserTest {

    @Test
    fun `parse valid spec without fallback`() {
        val result = ThemeSpecV1Parser.parse(
            """
            {
              "schemaVersion": 1,
              "metadata": {
                "name": "Valid",
                "version": "1.0.0",
                "author": "QA",
                "compatibility": {"minSdk": 31, "minAppVersion": "1.0.0"}
              },
              "palette": {
                "light": {
                  "primary": "#000000", "onPrimary": "#FFFFFF", "secondary": "#111111", "onSecondary": "#FFFFFF",
                  "tertiary": "#222222", "onTertiary": "#FFFFFF", "background": "#FFFFFF", "onBackground": "#000000",
                  "surface": "#FFFFFF", "onSurface": "#000000"
                },
                "dark": {
                  "primary": "#FFFFFF", "onPrimary": "#000000", "secondary": "#EEEEEE", "onSecondary": "#000000",
                  "tertiary": "#DDDDDD", "onTertiary": "#000000", "background": "#000000", "onBackground": "#FFFFFF",
                  "surface": "#000000", "onSurface": "#FFFFFF"
                }
              },
              "typography": {
                "bodyLarge": {"fontSizeSp": 16, "lineHeightSp": 24, "letterSpacingSp": 0.5, "fontWeight": 400},
                "titleLarge": {"fontSizeSp": 22, "lineHeightSp": 28, "letterSpacingSp": 0, "fontWeight": 500},
                "labelSmall": {"fontSizeSp": 11, "lineHeightSp": 16, "letterSpacingSp": 0.5, "fontWeight": 500}
              },
              "motion": {"shortMs": 100, "mediumMs": 200, "longMs": 300, "easing": "linear"},
              "layout": {
                "interaction": {"paddingDp": 16, "spacingDp": 12, "cornerRadiusDp": 8},
                "presentation": {"paddingDp": 20, "spacingDp": 16, "cornerRadiusDp": 0, "maxWidthDp": 720}
              }
            }
            """.trimIndent()
        )

        assertFalse(result.usedFallback)
        assertTrue(result.validation.isValid)
        assertEquals("Valid", result.spec.metadata.name)
    }

    @Test
    fun `fallback on unsupported keys`() {
        val result = ThemeSpecV1Parser.parse(
            """
            {
              "schemaVersion": 1,
              "unsupported": true
            }
            """.trimIndent()
        )

        assertTrue(result.usedFallback)
        assertFalse(result.validation.isValid)
        assertTrue(result.validation.errors.any { it.contains("unsupported key") })
    }
}
