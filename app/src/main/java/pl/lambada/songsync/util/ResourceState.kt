package pl.lambada.songsync.util

/**
 * A sealed class representing the state of a resource.
 *
 * @param T The type of data associated with the resource state.
 * @property data The data associated with the resource state.
 * @property message An optional message associated with the resource state.
 */
sealed class ResourceState<T>(val data: T? = null, val message: String? = null) {
    /**
     * Represents a loading state.
     *
     * @param T The type of data.
     * @property data The data associated with the loading state.
     */
    class Loading<T>(data: T? = null) : ResourceState<T>(data)

    /**
     * Represents a success state with optional data.
     *
     * @param T The type of data.
     * @property data The data associated with the success state.
     */
    class Success<T>(data: T?) : ResourceState<T>(data)

    /**
     * Represents an error state with an optional message and data.
     *
     * @param T The type of data.
     * @property message The message associated with the error state.
     * @property data The data associated with the error state.
     */
    class Error<T>(message: String, data: T? = null) : ResourceState<T>(data, message)
}