package com.chatSDK.SupportSync.core.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@RequiresApi(Build.VERSION_CODES.O)
class LocalDateTimeAdapter : TypeAdapter<LocalDateTime?>() {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    override fun write(out: JsonWriter, value: LocalDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        return if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
            reader.nextNull()
            null
        } else {
            LocalDateTime.parse(reader.nextString(), formatter)
        }
    }
}

