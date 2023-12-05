package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    private val _schedule = MutableLiveData<Schedule?>()
    val schedule: LiveData<Schedule?> = _schedule

    fun loadSchedule(date: String) {
        repository.loadScheduleFromFirebase(date) { schedule ->
            _schedule.value = schedule
        }
    }

    fun addSchedule(date: String, content: String) {
        repository.addScheduleToFirebase(date, content) { success ->
            if (success) {
                // Handle success
            } else {
                // Handle failure
            }
        }
    }

    @Suppress("unused")
    constructor() : this(MainRepository(FirebaseDatabase.getInstance())) {
        // 여기서 더 필요한 초기화 작업을 수행할 수 있습니다.
    }
}