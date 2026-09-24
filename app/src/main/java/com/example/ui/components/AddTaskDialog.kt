package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FollowerEntity
import com.example.ui.theme.BurgundyPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    preselectedFollower: FollowerEntity? = null,
    availableFollowers: List<FollowerEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        followerId: Long?,
        followerName: String?,
        priority: String,
        dueDate: Long,
        category: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFollower by remember { mutableStateOf(preselectedFollower) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val priorities = listOf("عاجل", "عالية الأهمية", "متوسطة", "عادية")
    var selectedPriority by remember { mutableStateOf("عالية الأهمية") }

    val categories = listOf("اتصال هاتفي", "اجتماع", "إرسال عرض", "متابعة دورية", "تسليم عمل")
    var selectedCategory by remember { mutableStateOf("متابعة دورية") }

    var dueDateOffsetHours by remember { mutableLongStateOf(24L) } // default 24h
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مهمة جديدة",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = BurgundyPrimary
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    label = { Text("عنوان المهمة *") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    isError = isError && title.isBlank(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_task_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("تفاصيل المهمة (اختياري)") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Follower Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFollower?.name ?: "بدون ربط بمتابع (مهمة عامة)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المتابع المرتبط بالمهمة") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون ربط بمتابع (مهمة عامة)") },
                            onClick = {
                                selectedFollower = null
                                dropdownExpanded = false
                            }
                        )
                        availableFollowers.forEach { follower ->
                            DropdownMenuItem(
                                text = { Text("${follower.name} (${follower.category})") },
                                onClick = {
                                    selectedFollower = follower
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority
                Text(
                    text = "مستوى الأهمية:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    priorities.forEach { pr ->
                        FilterChip(
                            selected = selectedPriority == pr,
                            onClick = { selectedPriority = pr },
                            label = { Text(pr, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BurgundyPrimary,
                                selectedLabelColor = androidx.compose.ui.graphics.Color.White
                            )
                        )
                    }
                }

                // Category
                Text(
                    text = "نوع الإجراء:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // Due Date Quick Presets
                Text(
                    text = "موعد الاستحقاق:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "اليوم (4س)" to 4L,
                        "غداً" to 24L,
                        "بعد 3 أيام" to 72L,
                        "بعد أسبوع" to 168L
                    )
                    presets.forEach { (label, hours) ->
                        FilterChip(
                            selected = dueDateOffsetHours == hours,
                            onClick = { dueDateOffsetHours = hours },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isError = true
                    } else {
                        val computedDueDate = System.currentTimeMillis() + (dueDateOffsetHours * 3600000L)
                        onSave(
                            title,
                            description,
                            selectedFollower?.id,
                            selectedFollower?.name,
                            selectedPriority,
                            computedDueDate,
                            selectedCategory
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                modifier = Modifier.testTag("save_task_button")
            ) {
                Text("حفظ المهمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
