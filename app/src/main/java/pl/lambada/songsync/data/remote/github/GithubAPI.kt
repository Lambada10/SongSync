package pl.lambada.songsync.data.remote.github

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import pl.lambada.songsync.domain.model.Release
import pl.lambada.songsync.util.networking.Ktor.client
import pl.lambada.songsync.util.networking.Ktor.json


object GithubAPI {
    private const val BASE_URL = "https://api.github.com/"

    /**
     * Gets latest GitHub release information.
     * @return The latest release version.
     */
    suspend fun getLatestRelease(): Release {
        val response = client.get(BASE_URL + "repos/Lambada10/SongSync/releases/latest")
        val responseBody = response.bodyAsText(Charsets.UTF_8)
        client.close()

        return json.decodeFromString<Release>(responseBody)
    }
}