package pl.lambada.songsync.domain.model

import androidx.annotation.StringRes
import pl.lambada.songsync.R

enum class SortOrders(val queryName: String, @StringRes val displayName: Int) {
    ASCENDING("ASC", R.string.ascending),
    DESCENDING("DESC", R.string.descending),
}

enum class SortValues(@StringRes val displayName: Int) {
    TITLE(R.string.title),
    ARTIST(R.string.artist),
    ALBUM(R.string.album),
    YEAR(R.string.year),
    DURATION(R.string.duration),
}
