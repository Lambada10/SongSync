package pl.lambada.songsync.util

/**
 * A sealed class representing the state of a screen.
 *
 * @param T The type of data associated with the success state.
 */
sealed class ScreenState<out T> {
    /**
     * Represents a loading state.
     */
    data object Loading : ScreenState<Nothing>()

    /**
     * Represents a success state with optional data.
     *
     * @param T The type of data.
     * @property data The data associated with the success state.
     */
    data class Success<T>(val data: T?) : ScreenState<T>()

    /**
     * Represents an error state with an exception.
     *
     * @property exception The exception associated with the error state.
     */
    data class Error(val exception: Throwable) : ScreenState<Nothing>()
}