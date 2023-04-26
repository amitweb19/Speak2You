package com.amitweb19.speak2you.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QuestionaireDao {

    @Query("SELECT * FROM questionaire_table ORDER BY id ASC")
    //fun getAllQn(): LiveData<List<Questionaire>>
    fun getAllQn(): LiveData<List<Questionaire>>

    @Query("SELECT questionaire FROM questionaire_table ORDER BY id ASC")
    fun getAllQnSpinner():LiveData<List<String>>
    //fun getAllQnSpinner(): List<String>

    @Query("SELECT COUNT(id) FROM questionaire_table LIMIT 1")
    fun getQnCount():Int
    //fun getAllQnSpinner(): List<String>

    // @Query("SELECT COUNT(id) FROM questionaire_table WHERE id = :qn LIMIT 1")
    // fun isExists(qn: Int):Int

    @Query("SELECT id FROM questionaire_table WHERE questionaire = :qn LIMIT 1")
    fun getQnID(qn: String): Int
    //fun getQnID(qn: String): Int

    @Query("SELECT * FROM questionaire_table WHERE id LIKE :id LIMIT 1")
    fun selectQn(id: Int): Questionaire

    @Query("SELECT * FROM questionaire_table WHERE questionaire LIKE :query")
    fun searchQnDB(query: String): List<Questionaire>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQn(vararg qn: Questionaire)

    @Delete
    fun deleteQn(qn: Array<Questionaire>)

    @Delete
    fun deleteAllQn(qn: Array<Questionaire>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateQn(qn: Questionaire)

}