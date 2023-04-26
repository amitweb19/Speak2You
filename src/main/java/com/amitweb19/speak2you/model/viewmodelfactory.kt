package com.amitweb19.speak2you.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
//import com.amitweb19.ask2me.model.ViewModel

class viewmodelfactory(private val repo:repository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewmodel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewmodel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
        //throw IllegalArgumentException(modelClass.toString())
    }
}