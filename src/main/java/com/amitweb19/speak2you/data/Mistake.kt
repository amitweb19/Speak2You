package com.amitweb19.speak2you.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "mistake_table")
data class Mistake(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var mistake: String = "",
    var correction: String = "",
    var mcount: Int = 0,
    var q_id: Int = 0,
): Parcelable