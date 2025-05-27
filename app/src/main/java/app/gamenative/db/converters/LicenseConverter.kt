package app.gamenative.db.converters

import androidx.room.TypeConverter
import `in`.dragonbra.javasteam.enums.ELicenseFlags
import `in`.dragonbra.javasteam.enums.ELicenseType
import `in`.dragonbra.javasteam.enums.EPaymentMethod
import java.util.EnumSet
import kotlinx.serialization.json.Json

class LicenseConverter {

    @TypeConverter
    fun toLicenseFlags(licenseFlags: Int): EnumSet<ELicenseFlags> = ELicenseFlags.from(licenseFlags)

    @TypeConverter
    fun fromLicenseFlags(licenseFlags: EnumSet<ELicenseFlags>): Int = ELicenseFlags.code(licenseFlags)

    @TypeConverter
    fun toLicenseType(licenseType: Int): ELicenseType = ELicenseType.from(licenseType)

    @TypeConverter
    fun fromLicenseType(licenseType: ELicenseType): Int = licenseType.code()

    @TypeConverter
    fun toPaymentMethod(paymentMethod: Int): EPaymentMethod = EPaymentMethod.from(paymentMethod)

    @TypeConverter
    fun fromPaymentMethod(paymentMethod: EPaymentMethod): Int = paymentMethod.code()

    @TypeConverter
    fun toIntList(appIds: String): List<Int> = Json.decodeFromString<List<Int>>(appIds)

    @TypeConverter
    fun fromIntList(appIds: List<Int>): String = Json.encodeToString(appIds)
}
