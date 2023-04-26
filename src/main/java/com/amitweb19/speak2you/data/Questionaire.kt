package com.amitweb19.speak2you.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "questionaire_table")
data class Questionaire(
    var questionaire: String = "",
    var timestamp: Long = 0L,
    var tcount: Int = 0,
    var rcount: Int = 0,
    var ecount: Int = 0
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @Ignore
    var isChecked:Boolean = false
}