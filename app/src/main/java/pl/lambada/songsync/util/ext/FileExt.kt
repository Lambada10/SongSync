package pl.lambada.songsync.util.ext

import android.util.Log
import java.io.File

/**
 * Replace all characters in the file name that are not allowed to exist in a file name with an underscore.
 *
 * @return The sanitized [File] instance.
 */
fun File.sanitize(): File {
    return File(this.parent, this.name.replace(Regex("[/\\\\:*?\"<>|\\t\\n]"), "_"))
}

/**
 * Converts the file path to an LRC file path.
 *
 * @return The [File] instance with the LRC extension.
 */
fun String.toLrcFile(): File? {
    return if (this.isNotEmpty()) {
        File(this.substringBeforeLast('.') + ".lrc")
    } else {
        null
    }
}