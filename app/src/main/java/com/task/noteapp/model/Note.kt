package com.task.noteapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val title: String,
    val content: String,
    val date: String,
    val edited : String,
    val color: Int = -1,
    val imagePath: String?,
    val isEdited : Boolean = false
) : Serializable