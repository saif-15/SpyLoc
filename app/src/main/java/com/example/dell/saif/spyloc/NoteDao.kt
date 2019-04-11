package com.example.dell.saif.spyloc

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Insert
    fun insert(note: LocNote)

    @Update
    fun update(note: LocNote)

    @Delete
    fun delete(note: LocNote)

    @Query("SELECT COUNT (id) FROM note_table")
    fun getNumber(): Int

    @Query("SELECT * FROM note_table")
    fun getLocations(): List<LocNote>

    @Query("DELETE FROM note_table")
    fun deleteAllNotes()

    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAllNotes(): LiveData<List<LocNote>>

}