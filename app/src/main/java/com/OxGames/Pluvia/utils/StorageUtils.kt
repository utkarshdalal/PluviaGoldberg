package com.OxGames.Pluvia.utils

import android.os.StatFs

class StorageUtils {
    companion object {

        fun getAvailableSpace(path: String): Long {
            val stat = StatFs(path)
            return stat.blockSizeLong * stat.availableBlocksLong
        }

        fun formatBinarySize(bytes: Long, decimalPlaces: Int = 2): String {
            require(bytes > Long.MIN_VALUE) { "Out of range" }
            require(decimalPlaces >= 0) { "Negative decimal places unsupported" }

            val isNegative = bytes < 0
            val absBytes = kotlin.math.abs(bytes)

            if (absBytes < 1024) {
                return "$bytes B"
            }

            val units = arrayOf("KiB", "MiB", "GiB", "TiB", "PiB")
            val digitGroups = (63 - absBytes.countLeadingZeroBits()) / 10
            val value = absBytes.toDouble() / (1L shl (digitGroups * 10))

            val result = "%.${decimalPlaces}f %s".format(
                if (isNegative) -value else value,
                units[digitGroups - 1],
            )

            return result
        }
    }
}
