package com.OxGames.Pluvia.utils

import android.net.Uri
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object PcgwHelper {

    private const val TAG = "PcgwHelper"
    private const val API_BASE_URL = "https://www.pcgamingwiki.com/w/api.php"
    private const val APPID_REDIRECT_URL = "https://www.pcgamingwiki.com/api/appid.php"

    // Keep the pattern for parsing the {{Availability/row|...}} template
    private val steamAvailabilityPattern = Pattern.compile(
        """\{\{Availability/row\s*\|\s*Steam\s*\|([^|}]*)\|([^|}]*)\|([^|}]*)\|([^|}]*)\|([^|}]*)\}\}""",
        Pattern.CASE_INSENSITIVE
    )

    /**
     * Fetches DRM status from PCGamingWiki API using AppID redirect and wikitext parsing.
     * 1. Gets the target wiki page URL using the appid.php redirector.
     * 2. Extracts the page title from the target URL.
     * 3. Gets the page wikitext using the extracted title via the parse API.
     * 4. Parses the wikitext to find Steam/Windows DRM status.
     * IMPORTANT: Performs network I/O, call from a background thread.
     * @return true if confirmed DRM-free (Steam/Windows), false otherwise, null on error/unknown.
     */
    fun getDrmStatusBlocking(steamAppId: Int): Boolean? {
        try {
            // Step 1: Get target URL via redirect
            val targetUrl = getPageUrlViaRedirect(steamAppId)
            if (targetUrl == null) {
                Timber.tag(TAG).w("Could not get PCGW redirect URL for $steamAppId")
                return null // Can't proceed without the target URL
            }
            Timber.tag(TAG).d("PCGW redirect target for $steamAppId: $targetUrl")

            // Step 2: Extract page title from URL
            val pageTitle = extractPageTitleFromUrl(targetUrl)
            if (pageTitle == null) {
                Timber.tag(TAG).w("Could not extract page title from URL: $targetUrl")
                return null
            }
            Timber.tag(TAG).d("Extracted page title '$pageTitle' for $steamAppId")

            // Step 3: Get Wikitext for the page
            val wikitext = getWikitextForPage(pageTitle, steamAppId) // Pass ID for logging
            if (wikitext == null) {
                // Error logged within getWikitextForPage
                return null
            }

            // Step 4: Parse Wikitext
            return parseDrmStatusFromWikitext(wikitext, steamAppId)

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error during PCGW check for $steamAppId")
            return null
        }
    }

    // New function to handle the appid.php redirect
    private fun getPageUrlViaRedirect(steamAppId: Int): String? {
        val urlString = "$APPID_REDIRECT_URL?appid=$steamAppId"
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = false // DO NOT follow redirects automatically
            connection.connectTimeout = 10000
            connection.readTimeout = 10000 // Shorter timeout as we only need headers
            connection.connect()

            val responseCode = connection.responseCode
            // Check for redirect status codes (301, 302, 303, 307, 308)
            if (responseCode in 300..399) {
                val locationHeader = connection.getHeaderField("Location")
                if (locationHeader != null) {
                    return locationHeader
                } else {
                    Timber.tag(TAG).w("Redirect response received ($responseCode) but no Location header found for $steamAppId")
                }
            } else {
                 Timber.tag(TAG).w("Expected redirect but received HTTP $responseCode for $steamAppId URL: $urlString")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error requesting redirect for $steamAppId")
        } finally {
            connection?.disconnect()
        }
        return null
    }
    
    // New function to extract title from a PCGW URL
    private fun extractPageTitleFromUrl(url: String): String? {
        return try {
            // Find the last '/' and take the substring after it
            // Decoding might be needed if titles contain encoded chars, but let's start simple
            val decodedUrl = Uri.decode(url) // Basic decoding for spaces etc.
            val lastSlash = decodedUrl.lastIndexOf('/')
            if (lastSlash != -1 && lastSlash < decodedUrl.length - 1) {
                decodedUrl.substring(lastSlash + 1)
            } else {
                null // Invalid URL format
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error parsing page title from URL: $url")
            null
        }
    }

    private fun getWikitextForPage(pageTitle: String, steamAppIdForLog: Int): String? {
        // No need to encode pageTitle here, it came from the redirect URL path
        val responseJson = makeApiRequest(
            "action" to "parse",
            "page" to pageTitle, // Use title directly
            "prop" to "wikitext",
            "format" to "json"
        ) ?: return null

        return try {
            val jsonObject = JSONObject(responseJson)
            val parseObject = jsonObject.optJSONObject("parse")
            if (parseObject == null || parseObject.has("error")) {
                val errorInfo = parseObject?.optJSONObject("error")?.optString("info", "Unknown parse error")
                Timber.tag(TAG).w("PCGW Parse API error for '$pageTitle' (AppID: $steamAppIdForLog): $errorInfo")
                return null
            }
            parseObject.optJSONObject("wikitext")?.optString("*", null)
        } catch (e: JSONException) {
            Timber.tag(TAG).e(e, "Error parsing wikitext JSON for '$pageTitle' (AppID: $steamAppIdForLog)")
            null
        }
    }

    // parseDrmStatusFromWikitext using line splitting and checking the LAST parameter for platform
    private fun parseDrmStatusFromWikitext(wikitext: String, steamAppIdForLog: Int): Boolean? {
        val availabilitySectionStart = wikitext.indexOf("==Availability==")
        if (availabilitySectionStart == -1) {
            Timber.tag(TAG).w("Could not find ==Availability== section for $steamAppIdForLog.")
            return null
        }
        val availabilitySectionEnd = wikitext.indexOf("\n==", availabilitySectionStart + 1).let { if (it == -1) wikitext.length else it }
        val availabilityContent = wikitext.substring(availabilitySectionStart, availabilitySectionEnd)
        val lines = availabilityContent.split('\n').map { it.trim() }
        var steamRowFound = false
        var isDrmFreeOnSteamWin = false

        for (line in lines) {
            if (line.startsWith("{{Availability/row", ignoreCase = true) && line.endsWith("}}")) {
                val paramsString = line.substring(line.indexOf('|') + 1, line.length - 2)
                val params = paramsString.split('|').map { it.trim() }

                // Need at least 3 params for Store (0), DRM (2), and Platforms (assumed last)
                if (params.size >= 3) { 
                    val store = params[0]
                    val drmType = params[2]
                    // Assume the platform list is the LAST parameter
                    val platforms = params.last()

                    // Check if it's the Steam row
                    if (store.equals("Steam", ignoreCase = true)) {
                        steamRowFound = true
                        // Check if the last parameter includes Windows
                        val includesWindows = platforms.contains("Windows", ignoreCase = true)
                        val isMarkedDrmFree = drmType.contains("DRM-free", ignoreCase = true) || drmType.equals("None", ignoreCase = true)

                        if (includesWindows && isMarkedDrmFree) {
                            Timber.tag(TAG).d("Steam/Windows entry found for $steamAppIdForLog (checking last param) and marked DRM-free.")
                            isDrmFreeOnSteamWin = true
                            break // Found what we need
                        } else if (includesWindows) {
                           Timber.tag(TAG).d("Steam/Windows entry found for $steamAppIdForLog (checking last param) but DRM is '$drmType'.")
                           // Continue loop in case another Steam row IS DRM-free
                        }
                    }
                }
            }
        }

        return if (steamRowFound) {
            isDrmFreeOnSteamWin
        } else {
            Timber.tag(TAG).w("No usable {{Availability/row|Steam...}} line found for $steamAppIdForLog.")
            null
        }
    }

    /**
     * Generic helper to make API requests and return the response body as String.
     * Returns null on error.
     */
    private fun makeApiRequest(vararg params: Pair<String, String>): String? {
        var connection: HttpURLConnection? = null
        val reader: BufferedReader? = null
        val urlString: String
        try {
            val uriBuilder = Uri.parse(API_BASE_URL).buildUpon()
            params.forEach { (key, value) -> uriBuilder.appendQueryParameter(key, value) }
            urlString = uriBuilder.build().toString()
            val url = URL(urlString)

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept-Charset", "UTF-8") // Ensure UTF-8
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Timber.tag(TAG).w("API request failed - HTTP Code: $responseCode URL: $urlString")
                // Log error stream if possible
                try {
                    val errorStream = connection.errorStream
                    if (errorStream != null) {
                        val errorReader = BufferedReader(InputStreamReader(errorStream))
                        val errorBody = errorReader.readText()
                        Timber.tag(TAG).w("API Error Body: $errorBody")
                        errorReader.close()
                    }
                } catch (e: Exception) { /* Ignore */ }
                return null
            }

            val inputStream = connection.inputStream ?: return null
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val responseBody = bufferedReader.readText()
            bufferedReader.close()

            if (responseBody.isBlank()) {
                Timber.tag(TAG).w("API returned empty response. URL: $urlString")
                return null
            }
            return responseBody

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error during API request execution")
            return null
        } finally {
            connection?.disconnect()
            try {
                reader?.close()
            } catch (e: Exception) {
                // Ignore close exception
            }
        }
    }
} 