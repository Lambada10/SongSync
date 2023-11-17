package pl.lambada.songsync.util.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.serialization.json.Json

object Ktor {
    val client = HttpClient(CIO) {
        //In case of adding plugins, add them here
    }

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}