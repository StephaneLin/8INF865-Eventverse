package com.boulin.eventverse.data.network.adapters

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

/**
 * Adapter used to convert dates from API returned as long into actual Java Dates
 */
class DateAdapter : JsonDeserializer<Date>, JsonSerializer<Date> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Date {
        val date = Date()

        json?.let {
            date.time = it.asLong
        }

        return date
    }

    override fun serialize(
        src: Date?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.time)
    }
}