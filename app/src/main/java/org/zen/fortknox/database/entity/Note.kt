package org.zen.fortknox.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Notes")
data class Note(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var userId: Int = 0,
    var name: String,
    var content: String,
    var modifyDate: String,
    var createDate: String
) : Parcelable {
    override fun toString(): String {
        return """
            Note
            --------------------
            Id: $id
            User id: $userId
            
            Name: $name
            Content: $content
            
            Modify date: $modifyDate
            Create date: $createDate
        """.trimIndent()
    }
}
