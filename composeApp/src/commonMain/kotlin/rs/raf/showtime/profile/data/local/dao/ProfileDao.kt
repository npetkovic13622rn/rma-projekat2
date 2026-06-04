package rs.raf.showtime.profile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import rs.raf.showtime.profile.data.local.entity.LocalUserEntity

@Dao
interface ProfileDao {

    @Query("SELECT * FROM local_user WHERE id = 'current'")
    fun observeLocalUser(): Flow<LocalUserEntity?>

    @Upsert
    suspend fun upsertLocalUser(user: LocalUserEntity)

    @Query("DELETE FROM local_user")
    suspend fun clearLocalUser()
}
