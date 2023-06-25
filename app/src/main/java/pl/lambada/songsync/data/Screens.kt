package pl.lambada.songsync.data

import pl.lambada.songsync.MainActivity.Companion.context
import pl.lambada.songsync.R

/*
Enum class for navigation
 */
enum class Screens {
    Home,
    Browse,
    About;

    override fun toString(): String {
        return when (this) {
            Home -> context.getString(R.string.home)
            Browse -> context.getString(R.string.browse)
            About -> context.getString(R.string.about)
        }
    }
}