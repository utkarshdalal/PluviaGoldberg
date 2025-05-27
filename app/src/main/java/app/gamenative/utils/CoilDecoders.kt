package app.gamenative.utils

import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.apng.decode.APNGParser
import com.github.penfeizhou.animation.io.ByteBufferReader
import com.github.penfeizhou.animation.io.StreamReader
import java.nio.ByteBuffer
import okio.BufferedSource
import okio.ByteString.Companion.toByteString
import timber.log.Timber

/**
 * Custom [Decoder]'s for the Coil-Kt image loading library
 */

// .ico file decoder
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
                drawable = bitmap.toDrawable(options.context.resources),
                isSampled = false,
            )
        } catch (e: Exception) {
            Timber.e(e, "Something happened while decoding an ico file.")
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
            val validHeader = result.source
                .source()
                .peek()
                .rangeEquals(0, ICO_HEADER.toByteString())

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

// .png (Animated) PNG file decoder
// Reference: https://github.com/coil-kt/coil/issues/506#issuecomment-952526682
class AnimatedPngDecoder(private val source: ImageSource) : Decoder {
    override suspend fun decode(): DecodeResult {
        val buffer = source.source().squashToDirectByteBuffer()
        return DecodeResult(
            drawable = APNGDrawable { ByteBufferReader(buffer) },
            isSampled = false,
        )
    }

    private fun BufferedSource.squashToDirectByteBuffer(): ByteBuffer {
        request(Long.MAX_VALUE)

        val byteBuffer = ByteBuffer.allocateDirect(buffer.size.toInt())
        while (!buffer.exhausted()) {
            buffer.read(byteBuffer)
        }

        byteBuffer.flip()

        return byteBuffer
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceResult,
            options: Options,
            imageLoader: ImageLoader,
        ): Decoder? {
            val stream = result.source.source().peek().inputStream()
            return if (APNGParser.isAPNG(StreamReader(stream))) {
                AnimatedPngDecoder(result.source)
            } else {
                null
            }
        }
    }
}
