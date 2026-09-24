package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FollowerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowerDao {
    @Query("SELECT * FROM followers ORDER BY lastContactDate DESC")
    fun getAllFollowers(): Flow<List<FollowerEntity>>

    @Query("SELECT * FROM followers WHERE id = :id LIMIT 1")
    suspend fun getFollowerById(id: Long): FollowerEntity?

    @Query("SELECT * FROM followers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY lastContactDate DESC")
    fun searchFollowers(query: String): Flow<List<FollowerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollower(follower: FollowerEntity): Long

    @Update
    suspend fun updateFollower(follower: FollowerEntity)

    @Delete
    suspend fun deleteFollower(follower: FollowerEntity)

    @Query("SELECT COUNT(*) FROM followers")
    fun getFollowersCount(): Flow<Int>
}
