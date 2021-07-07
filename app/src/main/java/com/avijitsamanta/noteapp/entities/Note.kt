package com.avijitsamanta.noteapp.entities

import androidx.room.*
import org.jetbrains.annotations.NotNull
import java.io.Serializable
import java.util.*

@Entity(tableName = "notes")
class Note : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "title")
    var title: String? = null

    @ColumnInfo(name = "date_time")
    var dateTime: String? = null

    @ColumnInfo(name = "note_text")
    var noteText: String? = null

    @ColumnInfo(name = "image_path")
    var imagePath: String? = null

    @ColumnInfo(name = "color")
    var myColor: String? = null

    @ColumnInfo(name = "web_link")
    var webLink: String? = null

    @ColumnInfo(name = "time_stamp")
    var timeStamp: Date? = null

    @NotNull
    override fun toString(): String {
        return "$title : $dateTime\n"
    }
}