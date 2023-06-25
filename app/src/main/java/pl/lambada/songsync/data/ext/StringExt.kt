package pl.lambada.songsync.data.ext

import java.util.Locale

fun String.lowercaseWithLocale(): String {
    return this.lowercase(Locale.getDefault())
}
