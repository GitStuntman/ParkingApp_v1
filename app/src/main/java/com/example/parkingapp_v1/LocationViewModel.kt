package com.example.parkingapp_v1

import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LocationViewModel(private val repository: LocationRepository): ViewModel() {
    var locationItems: LiveData<List<Location>> = repository.allLocation.asLiveData()
    var lastLocation: LiveData<Location> = repository.lastLocation.asLiveData()

    lateinit var locationItem: LiveData<Location>

    fun addLocationItem(newLocation: Location) = viewModelScope.launch {
        repository.insertLocationItem(newLocation)
    }

    fun removeLocationItem(newLocation: Location) = viewModelScope.launch {
        repository.deleteLocationItem(newLocation.id)
    }

    fun removeAllLocationItem() = viewModelScope.launch {
        repository.deleteAllItem()
    }
    fun getLocationFromID(id: Int) = viewModelScope.launch {
        repository.getLocationFromID(id)
        locationItem = repository.currentLocation.asLiveData()
    }

}

class LocationItemModelFactory(private val repository: LocationRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java))
            return LocationViewModel(repository) as T
        throw IllegalArgumentException("Unknown class for View Model")
    }
}