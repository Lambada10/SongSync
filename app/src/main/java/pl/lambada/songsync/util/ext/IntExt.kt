package pl.lambada.songsync.util.ext

fun Int.toLrcTimestamp(): String {
    val minutes = this / 60000
    val seconds = (this % 60000) / 1000
    val milliseconds = this % 1000

    val leadingZeros: Array<String> = arrayOf(
        if (minutes < 10) "0" else "",
        if (seconds < 10) "0" else "",
        if (milliseconds < 10) "00" else if (milliseconds < 100) "0" else ""
    )

    return "${leadingZeros[0]}$minutes:${leadingZeros[1]}$seconds.${leadingZeros[2]}$milliseconds"
}