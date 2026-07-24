package com.marshall.pyerite.infra.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/** JWT `aud`: string or array of strings (do not split on spaces). */
object JsonStringOrArraySerializer : KSerializer<List<String>> {
    private val listSerializer = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("JsonStringOrArraySerializer only works with JSON")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> element.map { it.jsonPrimitive.content }
            is JsonPrimitive -> listOf(element.content)
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("JsonStringOrArraySerializer only works with JSON")
        jsonEncoder.encodeJsonElement(JsonArray(value.map { JsonPrimitive(it) }))
    }
}

/** JWT `scp`: string (space-separated) or array of strings. */
object JsonScopeListSerializer : KSerializer<List<String>> {
    private val listSerializer = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): List<String> {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("JsonScopeListSerializer only works with JSON")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> element.map { it.jsonPrimitive.content }.filter { it.isNotBlank() }
            is JsonPrimitive -> element.content.split(' ').filter { it.isNotBlank() }
            else -> emptyList()
        }
    }

    override fun serialize(encoder: Encoder, value: List<String>) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("JsonScopeListSerializer only works with JSON")
        jsonEncoder.encodeJsonElement(JsonArray(value.map { JsonPrimitive(it) }))
    }
}
