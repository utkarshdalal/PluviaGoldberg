package com.OxGames.Pluvia.data

import `in`.dragonbra.javasteam.enums.ELicenseFlags
import `in`.dragonbra.javasteam.enums.ELicenseType
import `in`.dragonbra.javasteam.enums.EPaymentMethod
import java.util.EnumSet

data class PackageInfo(
    // val original: KeyValue,
    val packageId: Int,
    val receiveIndex: Int,
    val ownerAccountId: Int,
    val lastChangeNumber: Int,
    val accessToken: Long,
    val territoryCode: Int,
    val licenseFlags: EnumSet<ELicenseFlags>,
    val licenseType: ELicenseType,
    val paymentMethod: EPaymentMethod,
    val purchaseCountryCode: String,

    var appIds: IntArray,
    var depotIds: IntArray,
)