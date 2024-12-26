package com.OxGames.Pluvia.utils

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.fetch.SourceResult
import coil.request.Options
import okio.BufferedSource
import okio.ByteString.Companion.toByteString

/**
 * Coil .ico file decoder
 */
class IconDecoder(
    private val source: SourceResult,
    private val options: Options,
) : Decoder {
    override suspend fun decode(): DecodeResult? {
        return try {
            val bufferedSource: BufferedSource = source.source.source() // nice
            val bytes = bufferedSource.readByteArray()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            DecodeResult(
                drawable = BitmapDrawable(options.context.resources, bitmap),
                isSampled = false,
            )
        } catch (e: Exception) {
            logE("Something happened while decoding an ico file.", e)
            null
        }
    }
    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            val mimeType = result.mimeType ?: return null
            val validMimeType = mimeType.contains("ico", ignoreCase = true)
            val validHeader = result.source.source()
                .peek().rangeEquals(0, ICO_HEADER.toByteString())
            if (validMimeType || validHeader) {
                return IconDecoder(result, options)
            }
            return null
        }
        companion object {
            // https://en.wikipedia.org/wiki/ICO_(file_format)#Header
            private val ICO_HEADER = byteArrayOf(0, 0, 1, 0)
        }
    }
}
