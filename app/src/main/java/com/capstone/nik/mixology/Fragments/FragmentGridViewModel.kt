package com.capstone.nik.mixology.Fragments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.Network.MyApplication
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentGridViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as MyApplication).applicationComponent.drinkRepository()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun refresh(filter: DrinkFilter) {
        val kind = filter.kind ?: return
        val query = filter.query ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.fetchAndCache(filter.contentUri, query, kind)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh ${filter.name}", e)
                _error.postValue(getApplication<Application>().getString(R.string.network_error))
            }
        }
    }

    fun errorShown() {
        _error.value = null
    }

    companion object {
        private const val TAG = "FragmentGridViewModel"
    }
}
