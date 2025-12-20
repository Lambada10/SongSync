package pl.lambada.songsync.data.remote.lyrics_providers.apple

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.lambada.songsync.util.networking.Ktor.client

class AppleTokenManager {
    private var cachedToken: String? = null
    private val mutex = Mutex()

    suspend fun getToken(): String {
        mutex.withLock {
            cachedToken?.let { return it }

            try {
                val mainPageResponse = client.get("https://beta.music.apple.com")
                val mainPageBody = mainPageResponse.bodyAsText()

                val indexJsRegex = Regex("""/assets/index~[^/]+\.js""")
                val indexJsMatch = indexJsRegex.find(mainPageBody)
                    ?: throw Exception("Could not find index-legacy script URL")
                
                val indexJsUri = indexJsMatch.value

                val indexJsResponse = client.get("https://beta.music.apple.com$indexJsUri")
                val indexJsBody = indexJsResponse.bodyAsText()

                val tokenRegex = Regex("""eyJh([^"]*)""")
                val tokenMatch = tokenRegex.find(indexJsBody)
                    ?: throw Exception("Could not find token")
                
                val token = tokenMatch.value
                cachedToken = token
                return token
            } catch (e: Exception) {
                throw Exception("Error fetching Apple Music token: ${e.message}", e)
            }
        }
    }

    fun clearToken() {
        cachedToken = null
    }
}