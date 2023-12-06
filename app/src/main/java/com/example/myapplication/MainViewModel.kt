package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    // LiveData를 통해 UI에 데이터 전달
    private val _schedule = MutableLiveData<Schedule?>()
    val schedule: LiveData<Schedule?> = _schedule

    // 일정 로드
    fun loadSchedule(date: String) {
        repository.loadScheduleFromFirebase(date) { schedule ->
            _schedule.value = schedule
        }
    }

    // 일정 추가
    fun addSchedule(date: String, content: String) {
        repository.addScheduleToFirebase(date, content) { success ->
            if (success) {
                // 성공
            } else {
                // 실패
            }
        }
    }

    //기본 생성자
    constructor() : this(MainRepository(FirebaseDatabase.getInstance()))
}