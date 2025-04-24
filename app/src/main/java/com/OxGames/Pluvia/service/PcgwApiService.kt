package com.OxGames.Pluvia.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

class PcgwApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        // Optional: Add default request parameters or headers if needed
    }

    @Serializable
    private data class CargoQueryResponse(
        val cargoquery: List<CargoQueryResult>? = null
    )

    @Serializable
    private data class CargoQueryResult(
        val title: CargoQueryTitle? = null
    )

    @Serializable
    private data class CargoQueryTitle(
        @SerialName("DRM") // Use SerialName if JSON field name differs or contains spaces
        val drm: List<String>? = null,
        @SerialName("DRM notes")
        val drmNotes: String? = null
        // Add other fields if needed, like PageID or title itself
    )

    suspend fun getDrmStatus(steamAppId: Int): Boolean? {
        try {
            val response: CargoQueryResponse = client.get("https://www.pcgamingwiki.com/w/api.php") {
                parameter("action", "cargoquery")
                parameter("tables", "Infobox_game")
                parameter("fields", "Infobox_game.DRM, Infobox_game.'DRM notes'") // Query both fields
                parameter("where", "Infobox_game.Steam_AppID HOLDS \"$steamAppId\"")
                parameter("format", "json")
            }.body()

            val result = response.cargoquery?.firstOrNull()?.title
            if (result != null) {
                val drmTypes = result.drm ?: emptyList()
                val drmNotes = result.drmNotes ?: ""

                Timber.d("PCGW DRM Info for $steamAppId: DRM=$drmTypes, Notes='$drmNotes'")

                // Define logic to determine if DRM-free
                // Simple initial check: Look for "DRM-free" explicitly or common patterns.
                // This logic will likely need refinement based on observed API responses.
                val isExplicitlyDrmFree = drmTypes.any { it.equals("DRM-free", ignoreCase = true) } ||
                                          drmNotes.contains("DRM-free", ignoreCase = true)

                // Consider cases where the only DRM is "Manual download" or similar?
                val isOnlyManualDownload = drmTypes.size == 1 && drmTypes.first().equals("Manual download", ignoreCase = true)

                // If Steam is listed alongside DRM-free, assume it still needs Steam (not truly DRM-free *from Steam*)
                val hasSteamDrm = drmTypes.any { it.equals("Steam", ignoreCase = true) }

                return if (hasSteamDrm) {
                    false // If Steam DRM is present, it's not DRM-free in the context needed
                } else {
                    isExplicitlyDrmFree || isOnlyManualDownload
                }
            } else {
                Timber.w("PCGW API returned no result for $steamAppId")
                return null // Indicate unknown status
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch DRM status for $steamAppId from PCGW API")
            return null // Indicate unknown status due to error
        }
    }

    fun close() {
        client.close()
    }
}
