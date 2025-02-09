package com.OxGames.Pluvia.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import `in`.dragonbra.javasteam.enums.ELicenseFlags
import `in`.dragonbra.javasteam.enums.ELicenseType
import `in`.dragonbra.javasteam.enums.EPaymentMethod
import java.util.Date
import java.util.EnumSet

@Entity("steam_license")
data class SteamLicense(
    @PrimaryKey val id: Int,
    @ColumnInfo("owner_account_id")
    val ownerAccountId: Int,
    @ColumnInfo("last_change_number")
    val lastChangeNumber: Int,
    @ColumnInfo("access_token")
    val accessToken: Long,
    @ColumnInfo("territory_code")
    val territoryCode: Int,
    @ColumnInfo("license_flags")
    val licenseFlags: EnumSet<ELicenseFlags>,
    @ColumnInfo("license_type")
    val licenseType: ELicenseType,
    @ColumnInfo("payment_method")
    val paymentMethod: EPaymentMethod,
    @ColumnInfo("purchase_country_code")
    val purchaseCountryCode: String,
    @ColumnInfo("app_ids")
    var appIds: List<Int> = emptyList(),
    @ColumnInfo("depot_ids")
    var depotIds: List<Int> = emptyList(),
    @ColumnInfo("time_created")
    val timeCreated: Date,
    @ColumnInfo("time_next_process")
    val timeNextProcess: Date,
    @ColumnInfo("minute_limit")
    val minuteLimit: Int,
    @ColumnInfo("minutes_used")
    val minutesUsed: Int,
    @ColumnInfo("purchase_code")
    val purchaseCode: String,
    @ColumnInfo("master_package_id")
    val masterPackageID: Int,
)
