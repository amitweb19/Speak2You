package com.amitweb19.speak2you.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "question_table")
data class Question(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var question: String = "",
    var filePath: String = "",
    var timestamp: Long = 0L,
    var duration: String = "",
    var ampsPath: String = "",
    var speechToTxt: String = "",
    var qn_id: Int = 0,
    var isRecord:Boolean = false
): Parcelable {
    @Ignore
    var isChecked:Boolean = false
}