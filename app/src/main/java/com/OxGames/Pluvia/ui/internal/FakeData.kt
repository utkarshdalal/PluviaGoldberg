package com.OxGames.Pluvia.ui.internal

import com.OxGames.Pluvia.BuildConfig
import com.OxGames.Pluvia.data.AppInfo
import com.OxGames.Pluvia.data.ConfigInfo
import com.OxGames.Pluvia.data.LibraryAssetsInfo
import com.OxGames.Pluvia.data.LibraryCapsuleInfo
import com.OxGames.Pluvia.data.LibraryHeroInfo
import com.OxGames.Pluvia.data.LibraryLogoInfo
import com.OxGames.Pluvia.data.UFS
import com.OxGames.Pluvia.enums.AppType
import com.OxGames.Pluvia.enums.ControllerSupport
import com.OxGames.Pluvia.enums.OS
import com.OxGames.Pluvia.enums.ReleaseState
import java.util.EnumSet

/**
 * Fata data for Compose previewing.
 */
internal fun fakeAppInfo(idx: Int): AppInfo {
    if (!BuildConfig.DEBUG) {
        throw RuntimeException("Fake app info shouldn't be used in release")
    }

    return AppInfo(
        appId = 736260,
        receiveIndex = 1,
        packageId = 112233,
        depots = mapOf(),
        branches = mapOf(),
        name = "Baba Is You $idx",
        type = AppType.game,
        osList = EnumSet.of(OS.windows, OS.macos, OS.linux),
        releaseState = ReleaseState.released,
        metacriticScore = 0,
        metacriticFullUrl = "",
        logoHash = "",
        logoSmallHash = "",
        iconHash = "",
        clientIconHash = "",
        clientTgaHash = "",
        smallCapsule = mapOf(),
        headerImage = mapOf(),
        libraryAssets = LibraryAssetsInfo(
            libraryCapsule = LibraryCapsuleInfo(image = mapOf(), image2x = mapOf()),
            libraryHero = LibraryHeroInfo(image = mapOf(), image2x = mapOf()),
            libraryLogo = LibraryLogoInfo(image = mapOf(), image2x = mapOf()),
        ),
        primaryGenre = false,
        reviewScore = 0,
        reviewPercentage = 0,
        controllerSupport = ControllerSupport.partial,
        demoOfAppId = 0,
        developer = "Hempuli Oy",
        publisher = "Hempuli Oy",
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
        validOSList = EnumSet.of(OS.none),
        thirdPartyCdKey = false,
        visibleOnlyWhenInstalled = false,
        visibleOnlyWhenSubscribed = false,
        launchEulaUrl = "",
        requireDefaultInstallFolder = false,
        contentType = 0,
        installDir = "Baba Is You",
        useLaunchCmdLine = false,
        launchWithoutWorkshopUpdates = false,
        useMms = false,
        installScriptSignature = "",
        installScriptOverride = false,
        config = ConfigInfo(
            installDir = "Baba Is You",
            launch = arrayOf(),
            steamControllerTemplateIndex = 4,
            steamControllerTouchTemplateIndex = 1,
        ),
        ufs = UFS(
            quota = 0,
            maxNumFiles = 0,
            saveFilePatterns = emptyArray(),
        ),
    )
}
