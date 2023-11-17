package pl.lambada.songsync.data.remote.github

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import pl.lambada.songsync.domain.model.Release


object GithubAPI {
    private const val baseURL = "https://api.github.com/"
    private val jsonDec = Json { ignoreUnknownKeys = true }

    /**
     * Gets latest GitHub release information.
     * @return The latest release version.
     */
    suspend fun getLatestRelease(): Release {
        val client = HttpClient(CIO)
        val response = client.get(baseURL + "repos/Lambada10/SongSync/releases/latest")
        val responseBody = response.bodyAsText(Charsets.UTF_8)
        client.close()

        return jsonDec.decodeFromString<Release>(responseBody)
    }
}