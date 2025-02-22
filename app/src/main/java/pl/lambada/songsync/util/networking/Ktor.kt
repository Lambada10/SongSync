package pl.lambada.songsync.util.networking

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object Ktor {
    val client = HttpClient(CIO.create {
        // Here goes all the Engine config
        // TODO: Add proxy support
        // proxy = ProxyConfig(
        //     type = Proxy.Type.SOCKS,
        //     sa = java.net.InetSocketAddress(3030)
        // )
    }) {
        // In case of adding plugins, add them here
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}