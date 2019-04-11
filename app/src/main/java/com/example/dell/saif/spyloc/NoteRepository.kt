package com.example.dell.saif.spyloc

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData

class NoteRepository(application: Application) {

    private var noteDao: NoteDao

    private var allNotes: LiveData<List<LocNote>>
    lateinit private var allLoc:List<LocNote>
    var number:Int=0

    init {
        val database= NoteDatabase.getInstance(application.applicationContext)!!
        noteDao = database.noteDao()
        allNotes = noteDao.getAllNotes()
        Log.d("NOte Repo","new func")
        Log.d("MainActitvty","repo")
    }

    fun getNumbers():Int
    {
        number=GetNumberAsynTask(noteDao).execute().get()
        return number
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
        private class InsertNoteAsyncTask(noteDao: NoteDao) : AsyncTask<LocNote, Unit, Unit>() {
            val noteDao = noteDao

            override fun doInBackground(vararg p0: LocNote?) {
                noteDao.insert(p0[0]!!)
            }
        }

        private class UpdateNoteAsyncTask(noteDao: NoteDao) : AsyncTask<LocNote, Unit, Unit>() {
            val noteDao = noteDao

            override fun doInBackground(vararg p0: LocNote?) {
                noteDao.update(p0[0]!!)
            }
        }

        private class DeleteNoteAsyncTask(noteDao: NoteDao) : AsyncTask<LocNote, Unit, Unit>() {
            val noteDao = noteDao

            override fun doInBackground(vararg p0: LocNote?) {
                noteDao.delete(p0[0]!!)
            }
        }

        private class DeleteAllNotesAsyncTask(noteDao: NoteDao) : AsyncTask<Unit, Unit, Unit>() {
            val noteDao = noteDao

            override fun doInBackground(vararg p0: Unit?) {
                noteDao.deleteAllNotes()
            }
        }
        private class GetAllLocationsAsyncTask(noteDao: NoteDao):AsyncTask<Unit,Unit,List<LocNote>>()
        {
            val noteDao = noteDao
            override fun doInBackground(vararg params: Unit?): List<LocNote> {
                return noteDao.getLocations()

            }

        }
        private class GetNumberAsynTask(noteDao: NoteDao):AsyncTask<Unit,Unit,Int>()
        {
            var noteDao=noteDao
            override fun doInBackground(vararg params: Unit?): Int {
                return noteDao.getNumber()
            }
        }
    }
}