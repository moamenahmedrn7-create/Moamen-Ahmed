package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val whatsapp: String = "",
    val email: String = "",
    val category: String = "متابع", // عميل مميز، متابع جديد، عضو فريق، مهتم، شريك
    val status: String = "نشط",     // نشط، قيد المتابعة، مكتمل، مؤجل
    val notes: String = "",
    val city: String = "",
    val rating: Int = 5,
    val lastContactDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
