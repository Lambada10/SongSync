package pl.lambada.songsync.util.ext

import java.io.File
import java.util.Locale

fun String.lowercaseWithLocale(): String {
    return this.lowercase(Locale.getDefault())
}

fun String?.toLrcFile(): File? {
    if (this == null) return null
    val idx = lastIndexOf('.')
    return File(
        substring(
            0,
            if (idx == -1) length else idx
        ) + ".lrc"
    )
}

inline fun <reified T : Enum<T>> String?.toEnum(defaultValue: T): T =
    if (this == null) defaultValue
    else try {
        enumValueOf(this)
    } catch (e: IllegalArgumentException) {
        defaultValue
    }