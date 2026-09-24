package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val followerId: Long? = null,
    val followerName: String? = null,
    val priority: String = "متوسطة", // عاجل، عالية الأهمية، متوسطة، عادية
    val dueDate: Long = System.currentTimeMillis() + 86400000L, // default 1 day later
    val isCompleted: Boolean = false,
    val category: String = "متابعة", // اتصال هاتفي، اجتماع، تسليم، إرسال عرض، متابعة دورية
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
