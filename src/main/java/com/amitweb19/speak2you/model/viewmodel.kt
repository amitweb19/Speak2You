package com.amitweb19.speak2you.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.amitweb19.speak2you.data.Mistake
import com.amitweb19.speak2you.data.Question
import com.amitweb19.speak2you.data.Questionaire

class viewmodel(private val repo: repository):ViewModel() {

    fun getQuestionaireSn():LiveData<List<String>>{
        return repo.getQuestionaireSn()
    }

    fun getQuestionaire():LiveData<List<Questionaire>>{
        return repo.getQuestionaire()
    }

    fun getQuestion(qnID: Int):LiveData<List<Question>>{
        return repo.getQuestion(qnID)
    }
    /*
    fun getQuestionSn(qnID:Int):LiveData<List<String>>{


    }

     */

    fun getQuestionSn(qnID:Int): LiveData<List<String>>
    {
        return repo.getQuestionSn(qnID)
    }

    fun getAllMistake():LiveData<List<Mistake>>{
        return repo.getMistake()
    }

    fun getQnID(qn: String):Int{
        return repo.getQnID(qn)
    }

    fun getQID(q: String):Int{
        return repo.getQID(q)
    }


}