package com.OxGames.Pluvia.data

import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.enums.ControllerSupport
import com.OxGames.Pluvia.enums.Language
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.ReleaseState
import java.nio.file.Files
import java.util.EnumSet
import kotlin.io.path.Path

data class AppInfo(
    val appId: Int,
    val receiveIndex: Int,
    val packageId: Int,

    val depots: Map<Int, DepotInfo>,

    // Common
    val name: String,
    val type: AppType,
    val osList: EnumSet<OS>,
    val releaseState: ReleaseState,
    val metacriticScore: Byte,
    val metacriticFullUrl: String,
    // source: https://github.com/JosefNemec/PlayniteExtensions/blob/f2ad8c9b2ca206195e2c94b75606b56e2f6281df/source/Libraries/SteamLibrary/SteamShared/MetadataProvider.cs#L164
    val logoHash: String, // https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/{appId}/{logoHash}.jpg
    val logoUrl: String = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$logoHash.jpg",
    val logoSmallHash: String, // https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/{appId}/{logoSmallHash}.jpg
    val logoSmallUrl: String = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$logoSmallHash.jpg",
    val iconHash: String, // https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/{appId}/{iconHash}.jpg
    val iconUrl: String = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$iconHash.jpg",
    val clientIconHash: String, // https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/{appId}/{clientIconHash}.ico
    val clientIconUrl: String = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$clientIconHash.ico",
    val clientTgaHash: String, // https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/{appId}/{clientTgaHash}.tga
    val clientTgaUrl: String = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/$appId/$clientTgaHash.tga",
    val smallCapsule: Map<Language, String>,
    val headerImage: Map<Language, String>,
    val libraryAssets: LibraryAssetsInfo,
    val primaryGenre: Boolean,
    val reviewScore: Byte,
    val reviewPercentage: Byte,
    val controllerSupport: ControllerSupport,

    // Extended
    val demoOfAppId: Int,
    val developer: String,
    val publisher: String,
    val homepageUrl: String,
    val gameManualUrl: String,
    val loadAllBeforeLaunch: Boolean,
    val dlcAppIds: IntArray,
    val isFreeApp: Boolean,
    val dlcForAppId: Int,
    val mustOwnAppToPurchase: Int,
    val dlcAvailableOnStore: Boolean,
    val optionalDlc: Boolean,
    val gameDir: String,
    val installScript: String,
    val noServers: Boolean,
    val order: Boolean,
    val primaryCache: Int,
    val validOSList: EnumSet<OS>,
    val thirdPartyCdKey: Boolean,
    val visibleOnlyWhenInstalled: Boolean,
    val visibleOnlyWhenSubscribed: Boolean,
    val launchEulaUrl: String,

    // Config
    val requireDefaultInstallFolder: Boolean,
    val contentType: Int,
    val installDir: String,
    val useLaunchCmdLine: Boolean,
    val launchWithoutWorkshopUpdates: Boolean,
    val useMms: Boolean,
    val installScriptSignature: String,
    val installScriptOverride: Boolean,

    val config: ConfigInfo,

    val ufs: UFS,
) {
    // source: https://github.com/Nemirtingas/games-infos/blob/3915100198bac34553b3c862f9e295d277f5520a/steam_retriever/Program.cs#L589C43-L589C89
    fun getSmallCapsuleUrl(language: Language = Language.english): String? {
        return smallCapsule[language]?.let {
            "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
        }
    }
    fun getHeaderImageUrl(language: Language = Language.english): String? {
        return headerImage[language]?.let {
            "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
        }
    }
    fun getCapsuleUrl(language: Language = Language.english, large: Boolean = false): String? {
        return if (large) {
            libraryAssets.libraryCapsule.image2x[language]?.let {
                "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
            }
        } else {
            libraryAssets.libraryCapsule.image[language]?.let {
                "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
            }
        }
    }
    fun getHeroUrl(language: Language = Language.english, large: Boolean = false): String? {
        return if (large) {
            libraryAssets.libraryHero.image2x[language]?.let {
                "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
            }
        } else {
            libraryAssets.libraryHero.image[language]?.let {
                "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
            }
        }
    }
    fun getLogoUrl(language: Language = Language.english, large: Boolean = false): String? {
        return if (large) {
            libraryAssets.libraryLogo.image2x[language]?.let {
                "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
            }
        } else {
            libraryAssets.libraryLogo.image[language]?.let {
                "https://cdn.akamai.steamstatic.com/steam/apps/$appId/$it"
            }
        }
    }
}