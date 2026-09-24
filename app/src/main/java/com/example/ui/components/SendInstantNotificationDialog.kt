package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BurgundyPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SendInstantNotificationDialog(
    initialTitle: String = "",
    initialMessage: String = "",
    onDismiss: () -> Unit,
    onSend: (title: String, message: String, type: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle.ifBlank { "تنبيه فوري عاجل" }) }
    var message by remember {
        mutableStateOf(
            initialMessage.ifBlank { "تنبيه من منصة مُنجز: يرجى متابعة التحديثات والمهام المطلوبة فوراً." }
        )
    }

    val types = listOf("عاجل", "تذكير بمهمة", "تنبيه متابع", "إعلان للمستخدمين")
    var selectedType by remember { mutableStateOf(types[0]) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = AmberGold
            )
        },
        title = {
            Text(
                text = "إرسال تنبيه فوري للمستخدمين",
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
                Text(
                    text = "سيتم بث التنبيه الفوري بهزة وصوت في شريط الإشعارات وتسجيله في سجل التنبيهات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    label = { Text("عنوان التنبيه *") },
                    leadingIcon = { Icon(Icons.Default.Campaign, contentDescription = null) },
                    isError = isError && title.isBlank(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_alert_title"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = {
                        message = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    label = { Text("نص التنبيه الفوري *") },
                    leadingIcon = { Icon(Icons.Default.Message, contentDescription = null) },
                    isError = isError && message.isBlank(),
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_alert_message"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "نوع التنبيه:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    types.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BurgundyPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) {
                        isError = true
                    } else {
                        onSend(title, message, selectedType)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                modifier = Modifier.testTag("submit_instant_alert")
            ) {
                Text("إرسال فوري الآن")
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
