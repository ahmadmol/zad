package com.example.feature.azkar.presentation

data class AzkarUiState(
    val zikerId:Long=0L,
    val zikertext:String="",
    val currentCount:Int=0,
    val targetCount:Int=0,
    val isLoading:Boolean=false,
    val error:String? = null
)
sealed interface AzkarAction{
    object OnIncrement: AzkarAction
    object OnReset: AzkarAction
}