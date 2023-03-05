package com.xpaulnim.nearby

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xpaulnim.nearby.network.NearByApi
import com.xpaulnim.nearby.network.Position
import kotlinx.coroutines.launch

class NearbyViewModel : ViewModel() {
    //    TODO: What is this pattern?
    private val _status = MutableLiveData<String>()
    val status: LiveData<String> = _status

//    fun searchPointRadius(position: Position) {
//        viewModelScope.launch {
//            try {
//                val result = NearByApi.retrofitService.searchPointRadius(position.ggscoord)
//                _status.value = "Success: Found ${result.query?.pages?.size} results within ${position.radius} of ${position.ggscoord}"
//            } catch (e: Exception) {
//                _status.value = "Failure: ${e.message}"
//            }
//        }
//    }
//
//    fun searchBoundingBox(position: Position) {
//        viewModelScope.launch {
//            try {
//                val result = NearByApi.retrofitService.searchBoundingBox(position.ggscoord)
//                _status.value = "Success: Found ${result.query?.pages?.size} results within ${position.radius} of ${position.ggscoord}"
//            } catch (e: Exception) {
//                _status.value = "Failure: ${e.message}"
//            }
//        }
//    }
}