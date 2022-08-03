package com.noticemc.noticetransport.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable
data class PlayerLocation(val player: @Serializable(with = UUIDSerializer::class) UUID, val location: Location)

@Serializable
data class Location(val world: String,
    val x: Double,
    val y: Double,
    val z: Double)

@Serializable
data class TemplateLocation(val server: String, val location: Location)

// UUID <==> String
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}