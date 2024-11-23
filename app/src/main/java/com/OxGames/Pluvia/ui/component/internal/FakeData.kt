package com.OxGames.Pluvia.ui.component.internal

import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.data.ConfigInfo
import com.OxGames.Pluvia.data.DepotInfo
import com.OxGames.Pluvia.data.LibraryAssetsInfo
import com.OxGames.Pluvia.data.LibraryCapsuleInfo
import com.OxGames.Pluvia.data.LibraryHeroInfo
import com.OxGames.Pluvia.data.LibraryLogoInfo
import com.OxGames.Pluvia.data.ManifestInfo
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.enums.ControllerSupport
import com.OxGames.Pluvia.enums.Language
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.ReleaseState
import java.util.EnumSet

// TODO keyvalue wrapper for SteamService and Fake Data. Check ../keyvalues/
internal fun fakeAppInfo(): AppInfo =
    AppInfo(
        appId = 546560,
        receiveIndex = 568,
        packageId = 414170,
        depots = mapOf(
            546561 to DepotInfo(
                depotId = 546561,
                depotFromApp = 2147483647,
                sharedInstall = false,
                osList = EnumSet.noneOf(OS::class.java),
                manifests = mapOf(
                    "public" to ManifestInfo(
                        name = "public",
                        gid = 6340340699246199351,
                        size = 71955575627,
                        download = 53448313376
                    )
                ),
                encryptedManifests = mapOf(),
            )
        ),
        name = "Half-Life: Alyx",
        type = AppType.game,
        osList = EnumSet.of(OS.windows, OS.linux),
        releaseState = ReleaseState.released,
        metacriticScore = 93,
        metacriticFullUrl = "https://www.metacritic.com/game/pc/half-life-alyx?ftag=MCD-06-10aaa1f",
        logoHash = "f7269f4b14f921e9dff13c05caf133ffe92b58ab",
        logoUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/546560/f7269f4b14f921e9dff13c05caf133ffe92b58ab.jpg",
        logoSmallHash = "f7269f4b14f921e9dff13c05caf133ffe92b58ab_thumb",
        logoSmallUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/546560/f7269f4b14f921e9dff13c05caf133ffe92b58ab_thumb.jpg",
        iconHash = "225032ac2ad1aca8f5fd98baa2b9daf1eebea5ca",
        iconUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/546560/225032ac2ad1aca8f5fd98baa2b9daf1eebea5ca.jpg",
        clientIconHash = "836b72ec556fd56815a977140bb27cfe9ce12d06",
        clientIconUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/546560/836b72ec556fd56815a977140bb27cfe9ce12d06.ico",
        clientTgaHash = "d523faf1d3b6a91882923a9a0c42a95593cc2dac",
        clientTgaUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/apps/546560/d523faf1d3b6a91882923a9a0c42a95593cc2dac.tga",
        smallCapsule = mapOf(Language.english to "capsule_231x87.jpg"),
        headerImage = mapOf(Language.english to "header.jpg"),
        libraryAssets = LibraryAssetsInfo(
            libraryCapsule = LibraryCapsuleInfo(
                image = mapOf(Language.english to "library_600x900.jpg"),
                image2x = mapOf(Language.english to "library_600x900_2x.jpg")
            ),
            libraryHero = LibraryHeroInfo(
                image = mapOf(Language.english to "library_hero.jpg"),
                image2x = mapOf(Language.english to "library_hero_2x.jpg")
            ),
            libraryLogo = LibraryLogoInfo(
                image = mapOf(Language.english to "logo.png"),
                image2x = mapOf(Language.english to "logo_2x.png")
            )
        ),
        primaryGenre = true,
        reviewScore = 9,
        reviewPercentage = 98,
        controllerSupport = ControllerSupport.none,
        demoOfAppId = 0,
        developer = "",
        publisher = "",
        homepageUrl = "",
        gameManualUrl = "",
        loadAllBeforeLaunch = false,
        dlcAppIds = intArrayOf(),
        isFreeApp = false,
        dlcForAppId = 0,
        mustOwnAppToPurchase = 0,
        dlcAvailableOnStore = false,
        optionalDlc = false,
        gameDir = "",
        installScript = "",
        noServers = false,
        order = false,
        primaryCache = 0,
        validOSList = EnumSet.noneOf(OS::class.java),
        thirdPartyCdKey = false,
        visibleOnlyWhenInstalled = false,
        visibleOnlyWhenSubscribed = false,
        launchEulaUrl = "",
        requireDefaultInstallFolder = false,
        contentType = 0,
        installDir = "",
        useLaunchCmdLine = false,
        launchWithoutWorkshopUpdates = false,
        useMms = false,
        installScriptSignature = "",
        installScriptOverride = false,
        config = ConfigInfo(
            installDir = "Half - Life Alyx",
            launch = arrayOf(),
            steamControllerTemplateIndex = 0,
            steamControllerTouchTemplateIndex = 0
        )
    )

 