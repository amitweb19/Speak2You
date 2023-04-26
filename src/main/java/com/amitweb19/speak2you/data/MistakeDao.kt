package com.amitweb19.speak2you.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MistakeDao {

    @Query("SELECT * FROM mistake_table WHERE 1 ORDER BY mcount ASC")
    fun getAllM(): LiveData<List<Mistake>>


    @Query("SELECT * FROM mistake_table WHERE mistake LIKE :query")
    fun searchMDB(query: String): List<Mistake>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQ(vararg q: Mistake): Void

    @Delete
    fun deleteQ(q: Array<Mistake>): Void

    @Delete
    fun deleteAllQ(q: Array<Mistake>): Void

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateQ(vararg q: Mistake): Void

}