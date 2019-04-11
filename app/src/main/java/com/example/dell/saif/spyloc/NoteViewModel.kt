package com.example.dell.saif.spyloc

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: NoteRepository = NoteRepository(application)
    private var allNotes: LiveData<List<LocNote>> = repository.getAllNotes()

    private var allLocations:List<LocNote> = repository.getLocations()
    private var number:Int=repository.getNumbers()

    fun getNumber():Int
    {
        return number
    }
    fun insert(note: LocNote?) {
        repository.insert(note)
    }

    fun update(note: LocNote) {
        repository.update(note)
    }

    fun delete(note: LocNote) {
        repository.delete(note)
    }

    fun deleteAllNotes() {
        repository.deleteAllNotes()
    }

    fun getAllNotes(): LiveData<List<LocNote>> {
        Log.d("Mai nActitvty","LIve Data")
        return allNotes
    }

    fun getLocations(): List<LocNote>
    {    Log.d("MainActitvty","LIst")
        return allLocations
    }
}