package com.spyloc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class LocNote(
    var country_code: String?,
    var country_name: String?,
    var admin_area: String?,
    var feature_name: String?,
    var locality: String?,
    var subadmin_area: String?,
    var sub_locality: String?,
    var lat: Double?,
    var lng: Double?,
    var wifi: Boolean = true,
    var bluetooth: Boolean = false,
    var ringtone: Boolean= true,
    var alarm: Boolean = false,
    var notification: Boolean= true,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
