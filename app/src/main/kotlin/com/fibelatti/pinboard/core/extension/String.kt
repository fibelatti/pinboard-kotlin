package com.fibelatti.pinboard.core.extension

import androidx.annotation.VisibleForTesting

/**
 * A map with known escaped HTML characters and their unescaped counterparts.
 */
@VisibleForTesting
val HTML_CHAR_MAP: Map<String, String> = mapOf(
    "&lt;" to "<",
    "&gt;" to ">",
    "&quot;" to "\"",
    "&amp;" to "&"
)

/**
 * Search the receiver string for escaped HTML characters.
 *
 * @receiver the String to search
 *
 * @return true if an escaped HTML character was found, false otherwise
 *
 * @see HTML_CHAR_MAP
 */
fun String.containsHtmlChars(): Boolean {
    for (escaped in HTML_CHAR_MAP.keys) {
        if (contains(escaped)) {
            return true
        }
    }

    return false
}

/**
 * Replaces known escaped HTML characters with their unescaped counterparts.
 *
 * @receiver the String containing escaped HTML characters
 *
 * @return a new String with replaced characters
 *
 * @see HTML_CHAR_MAP
 */
fun String.replaceHtmlChars(): String {
    var updatedString = this

    for ((escaped, unescaped) in HTML_CHAR_MAP) {
        updatedString = updatedString.replace(escaped, unescaped)
    }

    return updatedString
}
