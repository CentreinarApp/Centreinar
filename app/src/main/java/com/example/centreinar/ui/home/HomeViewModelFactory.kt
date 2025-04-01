package com.example.centreinar.ui.home

import com.example.centreinar.LimitDao
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.centreinar.ClassificationDao

class HomeViewModelFactory(private val limitDao: LimitDao,private val classificationDao: ClassificationDao ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(limitDao,classificationDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}