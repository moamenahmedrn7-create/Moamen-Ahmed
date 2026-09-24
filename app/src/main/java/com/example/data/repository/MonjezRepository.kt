package com.example.data.repository

import com.example.data.local.FollowerDao
import com.example.data.local.NotificationAlertDao
import com.example.data.local.TaskDao
import com.example.data.model.FollowerEntity
import com.example.data.model.NotificationAlertEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

class MonjezRepository(
    private val followerDao: FollowerDao,
    private val taskDao: TaskDao,
    private val notificationDao: NotificationAlertDao
) {
    // Followers
    val allFollowers: Flow<List<FollowerEntity>> = followerDao.getAllFollowers()
    val followersCount: Flow<Int> = followerDao.getFollowersCount()

    fun searchFollowers(query: String): Flow<List<FollowerEntity>> =
        followerDao.searchFollowers(query)

    suspend fun getFollowerById(id: Long): FollowerEntity? =
        followerDao.getFollowerById(id)

    suspend fun insertFollower(follower: FollowerEntity): Long =
        followerDao.insertFollower(follower)

    suspend fun updateFollower(follower: FollowerEntity) =
        followerDao.updateFollower(follower)

    suspend fun deleteFollower(follower: FollowerEntity) =
        followerDao.deleteFollower(follower)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasksCount: Flow<Int> = taskDao.getPendingTasksCount()
    val completedTasksCount: Flow<Int> = taskDao.getCompletedTasksCount()

    fun getTasksForFollower(followerId: Long): Flow<List<TaskEntity>> =
        taskDao.getTasksByFollower(followerId)

    suspend fun insertTask(task: TaskEntity): Long =
        taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) =
        taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) =
        taskDao.deleteTask(task)

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val newStatus = !task.isCompleted
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(task.id, newStatus, completedAt)
    }

    // Notifications & Alerts
    val allNotifications: Flow<List<NotificationAlertEntity>> =
        notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> =
        notificationDao.getUnreadCount()

    suspend fun insertNotification(notification: NotificationAlertEntity): Long =
        notificationDao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long) =
        notificationDao.markAsRead(id)

    suspend fun markAllNotificationsAsRead() =
        notificationDao.markAllAsRead()

    suspend fun deleteNotification(notification: NotificationAlertEntity) =
        notificationDao.deleteNotification(notification)

    suspend fun clearAllNotifications() =
        notificationDao.clearAllNotifications()
}
