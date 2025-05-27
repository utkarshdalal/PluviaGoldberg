package app.gamenative.db.converters

import androidx.room.TypeConverter
import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.SteamID
import java.util.Date
import java.util.EnumSet

class FriendConverter {

    @TypeConverter
    fun fromTimestamp(value: Long): Date = Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date): Long = date.time

    @TypeConverter
    fun toEFriendRelationship(value: Int): EFriendRelationship = EFriendRelationship.from(value)

    @TypeConverter
    fun fromEFriendRelationship(type: EFriendRelationship): Int = type.code()

    @TypeConverter
    fun toEPersonaState(value: Int): EPersonaState = EPersonaState.from(value)

    @TypeConverter
    fun fromEPersonaState(state: EPersonaState): Int = state.code()

    @TypeConverter
    fun fromEClientPersonaStateFlagSet(flags: EnumSet<EClientPersonaStateFlag>): Int =
        EClientPersonaStateFlag.code(flags)

    @TypeConverter
    fun toEClientPersonaStateFlagSet(flags: Int): EnumSet<EClientPersonaStateFlag> =
        EClientPersonaStateFlag.from(flags)

    @TypeConverter
    fun fromEPersonaStateFlagSet(flags: EnumSet<EPersonaStateFlag>): Int =
        EPersonaStateFlag.code(flags)

    @TypeConverter
    fun toEPersonaStateFlagSet(flags: Int): EnumSet<EPersonaStateFlag> =
        EPersonaStateFlag.from(flags)

    @TypeConverter
    fun fromGameID(gameID: GameID): Long = gameID.convertToUInt64()

    @TypeConverter
    fun toGameID(value: Long): GameID = GameID(value)

    @TypeConverter
    fun fromSteamID(steamID: SteamID): Long = steamID.convertToUInt64()

    @TypeConverter
    fun toSteamID(value: Long): SteamID = SteamID(value)
}
