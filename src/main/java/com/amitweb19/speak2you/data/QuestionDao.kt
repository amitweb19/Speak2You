package com.amitweb19.speak2you.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question_table WHERE qn_id = :qnID ORDER BY id ASC")
    //fun getAllQ(qnID: Int): LiveData<List<Question>>
    fun getAllQ(qnID: Int): LiveData<List<Question>>

    @Query("SELECT * FROM question_table WHERE qn_id = :qnID ORDER BY id ASC")
    //fun getAllQ(qnID: Int): LiveData<List<Question>>
    fun getAllQwL(qnID: Int): Array<Question>

    @Query("SELECT COUNT(id) FROM question_table WHERE qn_id = :qn LIMIT 1")
    fun getQCount(qn: Int):Int

    @Query("SELECT isRecord FROM question_table WHERE id = :q LIMIT 1")
    fun isRecord(q: Int):Boolean

    // @Query("SELECT COUNT(id) FROM question_table WHERE id = :q LIMIT 1")
    // fun isExists(q: Int):Int

    /*
    @Query("SELECT * FROM question_table WHERE qn_id = :qid ORDER BY id ASC")
    //fun getAllQnQuestion(qid: Int): LiveData<List<Question>>
    fun getAllQnQuestion(qid: Int): List<Question>
     */

    @Query("SELECT question FROM question_table WHERE qn_id = :qid ORDER BY id ASC")
    fun getAllQSpinner(qid: Int): LiveData<List<String>>
    //fun getAllQSpinner(qid: Int): List<String>

    @Query("SELECT id FROM question_table WHERE question = :q")
    fun getQID(q: String): Int

    @Query("SELECT * FROM question_table WHERE question LIKE :query AND qn_id = :qnID")
    fun searchQDB(query: String, qnID: Int): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQ(vararg q: Question): Void

    @Delete
    fun deleteQ(q: Array<Question>): Void

    @Delete
    fun deleteAllQ(q: Array<Question>): Void

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateQ(vararg q: Question): Void

}