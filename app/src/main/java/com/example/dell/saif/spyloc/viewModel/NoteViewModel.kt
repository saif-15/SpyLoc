package com.example.dell.saif.spyloc.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.dell.saif.spyloc.model.LocNote

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: NoteRepository =
        NoteRepository(application)
    private var allNotes: LiveData<List<LocNote>> = repository.getAllNotes()
    private var liveNotes:LiveData<Int> = repository.getLiveDataNumbers()

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

    fun getAllNotes(): LiveData<List<LocNote>> =allNotes

    fun getLiveDataNumber():LiveData<Int> = liveNotes

}