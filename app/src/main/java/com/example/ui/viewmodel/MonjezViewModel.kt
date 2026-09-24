package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FollowerEntity
import com.example.data.model.NotificationAlertEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.MonjezRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MonjezViewModel(
    private val repository: MonjezRepository
) : ViewModel() {

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFollowerCategory = MutableStateFlow("الكل")
    val selectedFollowerCategory: StateFlow<String> = _selectedFollowerCategory.asStateFlow()

    private val _selectedTaskFilter = MutableStateFlow("الكل")
    val selectedTaskFilter: StateFlow<String> = _selectedTaskFilter.asStateFlow()

    // Followers
    val filteredFollowers: StateFlow<List<FollowerEntity>> = combine(
        repository.allFollowers,
        _searchQuery,
        _selectedFollowerCategory
    ) { list, query, category ->
        list.filter { follower ->
            val matchesQuery = query.isBlank() ||
                    follower.name.contains(query, ignoreCase = true) ||
                    follower.phone.contains(query, ignoreCase = true) ||
                    follower.city.contains(query, ignoreCase = true) ||
                    follower.notes.contains(query, ignoreCase = true)
            val matchesCategory = category == "الكل" || follower.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val followersCount: StateFlow<Int> = repository.followersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Tasks
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        repository.allTasks,
        _selectedTaskFilter
    ) { tasks, filter ->
        when (filter) {
            "قيد التنفيذ" -> tasks.filter { !it.isCompleted }
            "تم الإنجاز" -> tasks.filter { it.isCompleted }
            "عاجل ومهم" -> tasks.filter { !it.isCompleted && (it.priority == "عاجل" || it.priority == "عالية الأهمية") }
            else -> tasks
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingTasksCount: StateFlow<Int> = repository.pendingTasksCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedTasksCount: StateFlow<Int> = repository.completedTasksCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Notifications
    val notifications: StateFlow<List<NotificationAlertEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadAlertsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Update query / filter functions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFollowerCategory(category: String) {
        _selectedFollowerCategory.value = category
    }

    fun setTaskFilter(filter: String) {
        _selectedTaskFilter.value = filter
    }

    // Follower Actions
    fun addFollower(
        name: String,
        phone: String,
        whatsapp: String,
        email: String,
        category: String,
        status: String,
        notes: String,
        city: String
    ) {
        viewModelScope.launch {
            val follower = FollowerEntity(
                name = name.trim(),
                phone = phone.trim(),
                whatsapp = whatsapp.ifBlank { phone }.trim(),
                email = email.trim(),
                category = category,
                status = status,
                notes = notes.trim(),
                city = city.trim()
            )
            repository.insertFollower(follower)
        }
    }

    fun updateFollower(follower: FollowerEntity) {
        viewModelScope.launch {
            repository.updateFollower(follower)
        }
    }

    fun deleteFollower(follower: FollowerEntity) {
        viewModelScope.launch {
            repository.deleteFollower(follower)
        }
    }

    fun updateFollowerContactTimestamp(follower: FollowerEntity) {
        viewModelScope.launch {
            repository.updateFollower(follower.copy(lastContactDate = System.currentTimeMillis()))
        }
    }

    // Task Actions
    fun addTask(
        title: String,
        description: String,
        followerId: Long?,
        followerName: String?,
        priority: String,
        dueDate: Long,
        category: String
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                followerId = followerId,
                followerName = followerName,
                priority = priority,
                dueDate = dueDate,
                category = category
            )
            repository.insertTask(task)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Notification / Alert Actions
    fun sendInstantNotification(
        context: Context,
        title: String,
        message: String,
        type: String = "عاجل",
        relatedTaskId: Long? = null,
        relatedFollowerId: Long? = null
    ): Boolean {
        // Trigger system notification
        val delivered = NotificationHelper.sendInstantAlert(
            context = context,
            title = title,
            message = message,
            alertType = type
        )

        // Save into local notification log
        viewModelScope.launch {
            repository.insertNotification(
                NotificationAlertEntity(
                    title = title,
                    message = message,
                    type = type,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    relatedTaskId = relatedTaskId,
                    relatedFollowerId = relatedFollowerId
                )
            )
        }
        return delivered
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(notification: NotificationAlertEntity) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}

class MonjezViewModelFactory(
    private val repository: MonjezRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonjezViewModel::class.java)) {
            return MonjezViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
