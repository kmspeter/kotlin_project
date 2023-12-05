package com.example.myapplication

import com.google.firebase.database.*

class MainRepository(private val database: FirebaseDatabase) {

    fun loadScheduleFromFirebase(date: String, callback: (Schedule?) -> Unit) {
        val formattedDate = DateZero.formatDateWithZeroPadding(date)
        val scheduleRef = database.reference.child("schedules").child(formattedDate)

        scheduleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val content = dataSnapshot.child("content").value?.toString()

                if (content != null) {
                    val schedule = Schedule(formattedDate, content)
                    callback(schedule)
                } else {
                    callback(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        })
    }

    fun addScheduleToFirebase(date: String, content: String, callback: (Boolean) -> Unit) {
        val formattedDate = DateZero.formatDateWithZeroPadding(date)
        val scheduleRef = database.reference.child("schedules").child(formattedDate)
        val newSchedule = Schedule(formattedDate, content)

        scheduleRef.setValue(newSchedule).addOnCompleteListener {
            callback(it.isSuccessful)
        }
    }
}