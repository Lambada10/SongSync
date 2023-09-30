package pl.lambada.songsync.data.api

import pl.lambada.songsync.data.dto.GithubReleaseResponse
import pl.lambada.songsync.data.dto.Release
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class GithubAPI {
    private val baseURL = "https://api.github.com/"
    private val jsonDec = Json { ignoreUnknownKeys = true }

    /**
     * Gets latest GitHub release information.
     * @return The latest release version.
     */
    fun getLatestRelease(): Release {
        val url = URL(baseURL + "repos/Lambada10/SongSync/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use(BufferedReader::readText)

        connection.disconnect()

        val json = jsonDec.decodeFromString<GithubReleaseResponse>(response)

        return Release(
            htmlURL = json.htmlURL,
            tagName = json.tagName,
            changelog = json.body
        )
    }
}