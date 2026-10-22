package com.nuvio.tv.data.stalker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class StalkerChannel(
    val id: String,
    val name: String,
    val logo: String?,
    val cmd: String
)

class StalkerClient(portalUrl: String, mac: String) {
    private val api = portalUrl.trim().trimEnd('/')
        .removeSuffix("/c") + "/server/load.php"
    private val cookie = "mac=${mac.trim()}; stb_lang=en; timezone=Europe/Istanbul"
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private var token: String? = null

    private suspend fun call(vararg params: Pair<String, String>): JSONObject =
        withContext(Dispatchers.IO) {
            val url = api.toHttpUrl().newBuilder().apply {
                params.forEach { addQueryParameter(it.first, it.second) }
                addQueryParameter("JsHttpRequest", "1-xml")
            }.build()
            val req = Request.Builder().url(url)
                .header("Cookie", cookie)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (QtEmbedded; U; Linux; C) AppleWebKit/533.3 " +
                        "(KHTML, like Gecko) MAG200 stbapp ver: 2 rev: 250 Safari/533.3"
                )
                .header("X-User-Agent", "Model: MAG250; Link: WiFi")
                .apply { token?.let { header("Authorization", "Bearer $it") } }
                .build()
            http.newCall(req).execute().use { r ->
                if (!r.isSuccessful) error("HTTP ${r.code}")
                val body = r.body?.string() ?: error("Bos yanit")
                JSONObject(body).getJSONObject("js")
            }
        }

    suspend fun connect() {
        token = call("type" to "stb", "action" to "handshake", "token" to "")
            .getString("token")
        runCatching { call("type" to "stb", "action" to "get_profile") }
    }

    suspend fun channels(): List<StalkerChannel> {
        val arr = call("type" to "itv", "action" to "get_all_channels")
            .getJSONArray("data")
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            StalkerChannel(
                id = o.optString("id"),
                name = o.optString("name"),
                logo = o.optString("logo").ifBlank { null },
                cmd = o.optString("cmd")
            )
        }
    }

    suspend fun streamUrl(cmd: String): String {
        val s = call("type" to "itv", "action" to "create_link", "cmd" to cmd)
            .getString("cmd")
        return s.substringAfterLast(' ')
    }
}
