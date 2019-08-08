package com.spyloc.viewModel

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.spyloc.model.LocNote
import com.spyloc.model.NoteDao
import com.spyloc.model.NoteDatabase

class NoteRepository(application: Application) {

    private var noteDao: NoteDao

    private var allNotes: LiveData<List<LocNote>>
    var number:Int=0
    lateinit var livedataNumber:LiveData<Int>

    init {
        val database= NoteDatabase.getInstance(application.applicationContext)!!
        noteDao = database.noteDao()
        allNotes = noteDao.getAllNotes()
    }

    fun getNumbers():Int
    {
        number= GetNumberAsyncTask(noteDao).execute().get()
        return number
    }

    fun getLiveDataNumbers():LiveData<Int>
    {
        livedataNumber= GeLiveDatatNumberAsyncTask(noteDao).execute().get()
        return livedataNumber
    }

    fun insert(note: LocNote?) {
        InsertNoteAsyncTask(noteDao).execute(note)
    }

    fun update(note: LocNote) {
        UpdateNoteAsyncTask(noteDao).execute(note)
    }


    fun delete(note: LocNote) {
        DeleteNoteAsyncTask(noteDao).execute(note)
    }

    fun deleteAllNotes() {
        DeleteAllNotesAsyncTask(noteDao).execute()
    }

    fun getAllNotes(): LiveData<List<LocNote>> {
        return allNotes
    }

    fun getLocations():List<LocNote>
    {
        return GetAllLocationsAsyncTask(noteDao).execute().get()
    }

    companion object {
        private class InsertNoteAsyncTask(val noteDao: NoteDao) : AsyncTask<LocNote, Unit, Unit>() {


            override fun doInBackground(vararg p0: LocNote?) {
                noteDao.insert(p0[0]!!)
            }
        }

        private class UpdateNoteAsyncTask(val noteDao: NoteDao) : AsyncTask<LocNote, Unit, Unit>() {

            override fun doInBackground(vararg p0: LocNote?) {
                noteDao.update(p0[0]!!)
            }
        }

        private class DeleteNoteAsyncTask(val noteDao: NoteDao) : AsyncTask<LocNote, Unit, Unit>() {

            override fun doInBackground(vararg p0: LocNote?) {
                noteDao.delete(p0[0]!!)
            }
        }

        private class DeleteAllNotesAsyncTask(val noteDao: NoteDao) : AsyncTask<Unit, Unit, Unit>() {


            override fun doInBackground(vararg p0: Unit?) {
                noteDao.deleteAllNotes()
            }
        }
        private class GetAllLocationsAsyncTask(val noteDao: NoteDao):AsyncTask<Unit,Unit,List<LocNote>>()
        {
            override fun doInBackground(vararg params: Unit?): List<LocNote> {
                return noteDao.getLocations()

            }

        }
        private class GetNumberAsyncTask(val noteDao: NoteDao):AsyncTask<Unit,Unit,Int>()
        {

            override fun doInBackground(vararg params: Unit?): Int {
                return noteDao.getNumber()
            }
        }
        private class GeLiveDatatNumberAsyncTask(val noteDao: NoteDao):AsyncTask<Unit,Unit,LiveData<Int>>()
        {

            override fun doInBackground(vararg params: Unit?): LiveData<Int> {
                return noteDao.getLiveDataNumber()
            }
        }

    }
}