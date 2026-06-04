package rs.raf.showtime.profile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_user")
data class LocalUserEntity(
    @PrimaryKey val id: String = "current",
    val remoteId: String? = null,
    val fullName: String,
    val username: String,
    val updatedAt: Long? = null,
)
