package app.gamenative.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import `in`.dragonbra.javasteam.enums.ELicenseFlags
import `in`.dragonbra.javasteam.enums.ELicenseType
import `in`.dragonbra.javasteam.enums.EPaymentMethod
import `in`.dragonbra.javasteam.steam.handlers.steamapps.License
import java.util.Date
import java.util.EnumSet

/**
 * Data class to store [License] to room database,
 * with the addition of [appIds] and [depotIds] that the license pertains to.
 */
@Entity("steam_license")
data class SteamLicense(
    @PrimaryKey val packageId: Int,
    @ColumnInfo("last_change_number")
    val lastChangeNumber: Int,
    @ColumnInfo("time_created")
    val timeCreated: Date,
    @ColumnInfo("time_next_process")
    val timeNextProcess: Date,
    @ColumnInfo("minute_limit")
    val minuteLimit: Int,
    @ColumnInfo("minutes_used")
    val minutesUsed: Int,
    @ColumnInfo("payment_method")
    val paymentMethod: EPaymentMethod,
    @ColumnInfo("license_flags")
    val licenseFlags: EnumSet<ELicenseFlags>,
    @ColumnInfo("purchase_code")
    val purchaseCode: String,
    @ColumnInfo("license_type")
    val licenseType: ELicenseType,
    @ColumnInfo("territory_code")
    val territoryCode: Int,
    @ColumnInfo("access_token")
    val accessToken: Long,
    @ColumnInfo("owner_account_id")
    val ownerAccountId: List<Int>,
    @ColumnInfo("master_package_id")
    val masterPackageID: Int,

    @ColumnInfo("app_ids")
    var appIds: List<Int> = emptyList(),
    @ColumnInfo("depot_ids")
    var depotIds: List<Int> = emptyList(),
)
