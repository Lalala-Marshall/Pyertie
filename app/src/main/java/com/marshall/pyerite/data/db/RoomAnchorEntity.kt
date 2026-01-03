package com.marshall.pyerite.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "__room_anchor")
data class RoomAnchorEntity(
    @PrimaryKey
    val id: Int = 0
)

