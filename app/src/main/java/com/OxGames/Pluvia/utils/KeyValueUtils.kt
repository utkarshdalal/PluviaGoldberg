package com.OxGames.Pluvia.utils

import com.OxGames.Pluvia.data.ManifestInfo
import com.OxGames.Pluvia.enums.Language
import `in`.dragonbra.javasteam.types.KeyValue
import timber.log.Timber

/**
 * Extension functions relating to [KeyValue] as the receiver type.
 */

fun List<KeyValue>.generateManifest(): Map<String, ManifestInfo> = associate { manifest ->
    manifest.name to ManifestInfo(
        name = manifest.name,
        gid = manifest["gid"].asLong(),
        size = manifest["size"].asLong(),
        download = manifest["download"].asLong(),
    )
}

fun List<KeyValue>.toLangImgMap(): Map<Language, String> = mapNotNull { kv ->
    runCatching { Language.valueOf(kv.name) }
        .onFailure {
            Timber.w("Language ${kv.name} does not exist in enum")
        }
        .getOrNull()
        ?.takeIf { it != Language.unknown }
        ?.to(kv.value)
}.toMap()

@Suppress("unused")
fun KeyValue.printAllKeyValues(depth: Int = 0) {
    val parent = this
    var tabString = ""

    for (i in 0..depth) {
        tabString += "\t"
    }

    if (parent.children.isNotEmpty()) {
        Timber.i("$tabString${parent.name}")

        for (child in parent.children) {
            child.printAllKeyValues(depth + 1)
        }
    } else {
        Timber.i("$tabString${parent.name}: ${parent.value}")
    }
}
