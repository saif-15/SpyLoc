package com.example.dell.saif.spyloc

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class LocNote(var country_code:String?,
                   var country_name:String?,
                   var admin_area :String?,
                   var feature_name:String?,
                   var locality:String?,
                   var subadmin_area:String?,
                   var sub_locality:String?,
                   var lat:Double?,
                   var lng:Double?)
{

    @PrimaryKey(autoGenerate = true)
     var id: Int = 0

    var wifi:Int=1
    var bluetooth:Int=0
    var ringtone:Int=1
    var alarm:Int=0
    var notification:Int=1

}
