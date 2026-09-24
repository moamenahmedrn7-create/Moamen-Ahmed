package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FollowerEntity
import com.example.data.model.NotificationAlertEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.AddFollowerDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.SendInstantNotificationDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FollowersScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BurgundyPrimary
import com.example.ui.theme.MonjezTheme
import com.example.ui.viewmodel.MonjezViewModel
import com.example.ui.viewmodel.MonjezViewModelFactory
import com.example.util.CommunicationHelper

sealed class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : AppTab("الرئيسية", Icons.Default.Dashboard, "tab_dashboard")
    object Followers : AppTab("المتابعون", Icons.Default.People, "tab_followers")
    object Tasks : AppTab("المهام", Icons.Default.TaskAlt, "tab_tasks")
    object Alerts : AppTab("التنبيهات", Icons.Default.NotificationsActive, "tab_alerts")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MonjezApp
        val repository = app.repository

        setContent {
            MonjezTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: MonjezViewModel = viewModel(
                        factory = MonjezViewModelFactory(repository)
                    )
                    MonjezMainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonjezMainScreen(viewModel: MonjezViewModel) {
    val context = LocalContext.current

    // Request Notification permission for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "يرجى منح إذن التنبيهات لاستلام الإشعارات الفورية", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // State collections
    val followers by viewModel.filteredFollowers.collectAsStateWithLifecycle()
    val allFollowersList by viewModel.filteredFollowers.collectAsStateWithLifecycle()
    val followersCount by viewModel.followersCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedFollowerCategory.collectAsStateWithLifecycle()

    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val pendingTasksCount by viewModel.pendingTasksCount.collectAsStateWithLifecycle()
    val completedTasksCount by viewModel.completedTasksCount.collectAsStateWithLifecycle()
    val selectedTaskFilter by viewModel.selectedTaskFilter.collectAsStateWithLifecycle()

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadAlertsCount.collectAsStateWithLifecycle()

    // Navigation & Dialog states
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(AppTab.Dashboard, AppTab.Followers, AppTab.Tasks, AppTab.Alerts)

    var showAddFollowerDialog by remember { mutableStateOf(false) }
    var followerToEdit by remember { mutableStateOf<FollowerEntity?>(null) }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskPreselectedFollower by remember { mutableStateOf<FollowerEntity?>(null) }

    var showInstantAlertDialog by remember { mutableStateOf(false) }
    var alertPreFillTitle by remember { mutableStateOf("") }
    var alertPreFillMsg by remember { mutableStateOf("") }

    var followerToDelete by remember { mutableStateOf<FollowerEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AmberGold,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "م",
                                    color = Color(0xFF3B000C),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مُنجز",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 21.sp
                            ),
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Quick Instant Alert Shortcut
                    IconButton(
                        onClick = {
                            alertPreFillTitle = "تنبيه فوري عاجل"
                            alertPreFillMsg = "إشعار عاجل من منصة منجز"
                            showInstantAlertDialog = true
                        },
                        modifier = Modifier.testTag("quick_alert_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "إرسال تنبيه",
                            tint = AmberGold
                        )
                    }

                    // Notification Bell with unread badge
                    IconButton(
                        onClick = { currentTab = 3 },
                        modifier = Modifier.testTag("top_bar_notifications_bell")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = AmberGold,
                                        contentColor = Color(0xFF2B1D00)
                                    ) {
                                        Text("$unreadCount", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "التنبيهات",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BurgundyPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = index },
                        icon = {
                            if (tab == AppTab.Alerts && unreadCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = BurgundyPrimary) {
                                        Text("$unreadCount")
                                    }
                                }) {
                                    Icon(tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BurgundyPrimary,
                            selectedTextColor = BurgundyPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.outline,
                            unselectedTextColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            when (currentTab) {
                1 -> { // Followers tab
                    ExtendedFloatingActionButton(
                        onClick = {
                            followerToEdit = null
                            showAddFollowerDialog = true
                        },
                        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                        text = { Text("إضافة متابع", fontWeight = FontWeight.Bold) },
                        containerColor = BurgundyPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_follower")
                    )
                }
                2 -> { // Tasks tab
                    ExtendedFloatingActionButton(
                        onClick = {
                            taskPreselectedFollower = null
                            showAddTaskDialog = true
                        },
                        icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                        text = { Text("مهمة جديدة", fontWeight = FontWeight.Bold) },
                        containerColor = BurgundyPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_task")
                    )
                }
                3 -> { // Alerts tab
                    FloatingActionButton(
                        onClick = {
                            alertPreFillTitle = "تنبيه فوري عاجل"
                            alertPreFillMsg = "إشعار فوري من منصة منجز"
                            showInstantAlertDialog = true
                        },
                        containerColor = AmberGold,
                        contentColor = Color(0xFF2B1D00),
                        modifier = Modifier.testTag("fab_send_alert")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "إرسال تنبيه فوري")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> {
                    DashboardScreen(
                        followersCount = followersCount,
                        pendingTasksCount = pendingTasksCount,
                        completedTasksCount = completedTasksCount,
                        urgentTasks = tasks.filter { !it.isCompleted }.take(3),
                        recentFollowers = followers.take(3),
                        onNavigateToFollowers = { currentTab = 1 },
                        onNavigateToTasks = { currentTab = 2 },
                        onAddNewFollower = {
                            followerToEdit = null
                            showAddFollowerDialog = true
                        },
                        onAddNewTask = {
                            taskPreselectedFollower = null
                            showAddTaskDialog = true
                        },
                        onSendInstantAlert = {
                            alertPreFillTitle = "تنبيه فوري عاجل"
                            alertPreFillMsg = "تذكير للمستخدمين بمتابعة الأعمال عبر تطبيق مُنجز"
                            showInstantAlertDialog = true
                        },
                        onShareReport = {
                            val report = """
                                📊 تقرير إنجازات منصة مُنجز:
                                👥 إجمالي المتابعين: $followersCount
                                ⏳ المهام قيد التنفيذ: $pendingTasksCount
                                ✅ المهام المنجزة: $completedTasksCount
                                🚀 تم إنشاؤه عبر تطبيق مُنجز
                            """.trimIndent()
                            CommunicationHelper.shareText(context, "تقرير إنجاز مُنجز", report)
                        },
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = { taskToDelete = it },
                        onCallClick = { CommunicationHelper.makePhoneCall(context, it) },
                        onWhatsAppClick = { phone, msg -> CommunicationHelper.openWhatsAppChat(context, phone, msg) },
                        onSmsClick = { CommunicationHelper.sendSms(context, it) },
                        onEmailClick = { CommunicationHelper.sendEmail(context, it) }
                    )
                }

                1 -> {
                    FollowersScreen(
                        followers = followers,
                        searchQuery = searchQuery,
                        selectedCategory = selectedCategory,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategoryChange = { viewModel.setFollowerCategory(it) },
                        onCallClick = {
                            CommunicationHelper.makePhoneCall(context, it)
                        },
                        onWhatsAppClick = { phone, msg ->
                            CommunicationHelper.openWhatsAppChat(context, phone, msg)
                        },
                        onSmsClick = {
                            CommunicationHelper.sendSms(context, it)
                        },
                        onEmailClick = {
                            CommunicationHelper.sendEmail(context, it)
                        },
                        onAddTaskClick = { follower ->
                            taskPreselectedFollower = follower
                            showAddTaskDialog = true
                        },
                        onEditClick = { follower ->
                            followerToEdit = follower
                            showAddFollowerDialog = true
                        },
                        onDeleteClick = { follower ->
                            followerToDelete = follower
                        },
                        onSendInstantAlert = { follower ->
                            alertPreFillTitle = "تنبيه لمتابعة: ${follower.name}"
                            alertPreFillMsg = "يرجى التواصل الفوري مع ${follower.name} (${follower.category}) بخصوص المهام المعلقة."
                            showInstantAlertDialog = true
                        },
                        onAddNewFollower = {
                            followerToEdit = null
                            showAddFollowerDialog = true
                        }
                    )
                }

                2 -> {
                    TasksScreen(
                        tasks = tasks,
                        selectedFilter = selectedTaskFilter,
                        onFilterChange = { viewModel.setTaskFilter(it) },
                        onToggleTask = { viewModel.toggleTask(it) },
                        onSendInstantReminder = { task ->
                            alertPreFillTitle = "تذكير بمهمة: ${task.title}"
                            alertPreFillMsg = "تنبيه فوري: مهمة '${task.title}' ${if (task.followerName != null) "الخاصة بـ ${task.followerName}" else ""} بحاجة لإنجاز سريع."
                            showInstantAlertDialog = true
                        },
                        onDeleteTask = { task ->
                            taskToDelete = task
                        },
                        onAddNewTask = {
                            taskPreselectedFollower = null
                            showAddTaskDialog = true
                        }
                    )
                }

                3 -> {
                    AlertsScreen(
                        notifications = notifications,
                        unreadCount = unreadCount,
                        onSendNewInstantAlert = {
                            alertPreFillTitle = "تنبيه فوري عاجل"
                            alertPreFillMsg = "إشعار وتنبيه فوري للمستخدمين"
                            showInstantAlertDialog = true
                        },
                        onMarkAsRead = { viewModel.markNotificationAsRead(it) },
                        onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                        onDeleteNotification = { viewModel.deleteNotification(it) },
                        onClearAll = { viewModel.clearAllNotifications() }
                    )
                }
            }
        }
    }

    // Add / Edit Follower Dialog
    if (showAddFollowerDialog) {
        AddFollowerDialog(
            followerToEdit = followerToEdit,
            onDismiss = {
                showAddFollowerDialog = false
                followerToEdit = null
            },
            onSave = { name, phone, whatsapp, email, category, status, notes, city ->
                if (followerToEdit == null) {
                    viewModel.addFollower(name, phone, whatsapp, email, category, status, notes, city)
                    Toast.makeText(context, "تمت إضافة المتابع بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    val updated = followerToEdit!!.copy(
                        name = name,
                        phone = phone,
                        whatsapp = whatsapp,
                        email = email,
                        category = category,
                        status = status,
                        notes = notes,
                        city = city
                    )
                    viewModel.updateFollower(updated)
                    Toast.makeText(context, "تم تحديث البيانات", Toast.LENGTH_SHORT).show()
                }
                showAddFollowerDialog = false
                followerToEdit = null
            }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            preselectedFollower = taskPreselectedFollower,
            availableFollowers = allFollowersList,
            onDismiss = {
                showAddTaskDialog = false
                taskPreselectedFollower = null
            },
            onSave = { title, description, followerId, followerName, priority, dueDate, category ->
                viewModel.addTask(title, description, followerId, followerName, priority, dueDate, category)
                Toast.makeText(context, "تمت إضافة المهمة بنجاح", Toast.LENGTH_SHORT).show()
                showAddTaskDialog = false
                taskPreselectedFollower = null
            }
        )
    }

    // Instant Notification Dialog
    if (showInstantAlertDialog) {
        SendInstantNotificationDialog(
            initialTitle = alertPreFillTitle,
            initialMessage = alertPreFillMsg,
            onDismiss = { showInstantAlertDialog = false },
            onSend = { title, message, type ->
                val delivered = viewModel.sendInstantNotification(
                    context = context,
                    title = title,
                    message = message,
                    type = type
                )
                if (delivered) {
                    Toast.makeText(context, "تم بث التنبيه الفوري بنجاح! 🔔", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "تم تسجيل التنبيه في السجل (يرجى التحقق من أذونات الإشعارات)", Toast.LENGTH_LONG).show()
                }
                showInstantAlertDialog = false
            }
        )
    }

    // Delete Follower Confirmation Dialog
    followerToDelete?.let { follower ->
        AlertDialog(
            onDismissRequest = { followerToDelete = null },
            title = { Text("تأكيد حذف المتابع") },
            text = { Text("هل أنت متأكد من رغبتك في حذف '${follower.name}' نهائياً؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFollower(follower)
                        followerToDelete = null
                        Toast.makeText(context, "تم حذف المتابع", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { followerToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Task Confirmation Dialog
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("تأكيد حذف المهمة") },
            text = { Text("هل أنت متأكد من حذف مهمة '${task.title}'؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(task)
                        taskToDelete = null
                        Toast.makeText(context, "تم حذف المهمة", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
