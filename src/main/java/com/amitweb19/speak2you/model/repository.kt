package com.amitweb19.speak2you.model

import androidx.lifecycle.LiveData
import com.amitweb19.speak2you.data.Mistake
import com.amitweb19.speak2you.data.Question
import com.amitweb19.speak2you.data.Questionaire
import com.amitweb19.speak2you.db.AppDatabase

class repository(val db:AppDatabase) {

    fun getQuestionaireSn():LiveData<List<String>>{
        return db.questionaireDao().getAllQnSpinner()
    }

    fun getQuestionSn(qnID:Int):LiveData<List<String>>{
        return db.questionDao().getAllQSpinner(qnID)
    }

    fun getQnID(qn: String): Int {
        return db.questionaireDao().getQnID(qn)
    }

    fun getQID(q: String): Int {
        return db.questionDao().getQID(q)
    }

    fun getQuestionaire():LiveData<List<Questionaire>>{
        return db.questionaireDao().getAllQn()
    }

    fun getQuestion(qnID:Int):LiveData<List<Question>>{
        return db.questionDao().getAllQ(qnID)
    }

    fun getMistake():LiveData<List<Mistake>>{
        return db.mistakeDao().getAllM()
    }
}