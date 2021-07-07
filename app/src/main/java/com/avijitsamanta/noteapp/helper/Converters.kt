package com.avijitsamanta.noteapp.helper

import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun fromDateToLong(value: Date) = value.time

    @TypeConverter
    fun fromLongToDate(value: Long) = Date(value)
}