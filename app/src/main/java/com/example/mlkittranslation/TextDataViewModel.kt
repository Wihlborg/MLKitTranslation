package com.example.mlkittranslation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TextDataViewModel : ViewModel(){
    private val _text = MutableLiveData("Hello")

    val text: LiveData<String> = _text

    fun updateText(newText: String){
        _text.value = newText
    }

}