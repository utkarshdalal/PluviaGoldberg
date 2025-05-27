package app.gamenative.db.serializers

import app.gamenative.enums.OS
import java.util.EnumSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object OsEnumSetSerializer : KSerializer<EnumSet<OS>> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EnumSet<OS>", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: EnumSet<OS>) = encoder.encodeInt(OS.code(value))

    override fun deserialize(decoder: Decoder): EnumSet<OS> = OS.from(decoder.decodeInt())
}
