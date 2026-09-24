package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationAlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String = "عاجل", // عاجل، تذكير بمهمة، تنبيه متابع، إشعار عام
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetRecipient: String = "جميع المستخدمين",
    val relatedTaskId: Long? = null,
    val relatedFollowerId: Long? = null
)
