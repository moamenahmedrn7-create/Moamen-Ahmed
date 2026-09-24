package com.example

import android.app.Application
import com.example.data.local.MonjezDatabase
import com.example.data.model.FollowerEntity
import com.example.data.model.NotificationAlertEntity
import com.example.data.model.TaskEntity
import com.example.data.repository.MonjezRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MonjezApp : Application() {

    lateinit var database: MonjezDatabase
        private set

    lateinit var repository: MonjezRepository
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        database = MonjezDatabase.getDatabase(this)
        repository = MonjezRepository(
            followerDao = database.followerDao(),
            taskDao = database.taskDao(),
            notificationDao = database.notificationAlertDao()
        )

        // Prepopulate with rich sample data if empty
        CoroutineScope(Dispatchers.IO).launch {
            val existingFollowers = repository.allFollowers.first()
            if (existingFollowers.isEmpty()) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        val f1 = FollowerEntity(
            name = "م. أحمد عبد الرحمن",
            phone = "+201012345678",
            whatsapp = "+201012345678",
            email = "ahmed.abdelrahman@example.com",
            category = "عميل مميز",
            status = "نشط",
            notes = "مهتم بالمشروع الجديد، طلب عرض أسعار مفصل قبل نهاية الأسبوع",
            city = "القاهرة",
            rating = 5,
            lastContactDate = System.currentTimeMillis() - 3600000L * 3
        )
        val f2 = FollowerEntity(
            name = "سارة المنصوري",
            phone = "+971501234567",
            whatsapp = "+971501234567",
            email = "sara.mansouri@example.com",
            category = "متابع نشط",
            status = "قيد المتابعة",
            notes = "تتابع المنتجات بانتظام، استفسرت عن موعد التدشين الرسمي",
            city = "دبي",
            rating = 4,
            lastContactDate = System.currentTimeMillis() - 3600000L * 12
        )
        val f3 = FollowerEntity(
            name = "د. خالد السعيد",
            phone = "+966509876543",
            whatsapp = "+966509876543",
            email = "khalid.saeed@example.com",
            category = "شريك استراتيجي",
            status = "نشط",
            notes = "اتفاقية شراكة في قطاع التسويق والتدريب الرقمي",
            city = "الرياض",
            rating = 5,
            lastContactDate = System.currentTimeMillis() - 86400000L
        )
        val f4 = FollowerEntity(
            name = "محمود علي النجار",
            phone = "+201198765432",
            whatsapp = "+201198765432",
            email = "mahmoud.naggar@example.com",
            category = "عضو فريق",
            status = "نشط",
            notes = "مسؤول تطوير الميزات التقنية ومتابعة استفسارات المجتمع",
            city = "الإسكندرية",
            rating = 5,
            lastContactDate = System.currentTimeMillis() - 3600000L * 5
        )

        val id1 = repository.insertFollower(f1)
        val id2 = repository.insertFollower(f2)
        val id3 = repository.insertFollower(f3)
        val id4 = repository.insertFollower(f4)

        // Seed Initial Tasks
        val t1 = TaskEntity(
            title = "إرسال عرض الأسعار النهائي لـ م. أحمد",
            description = "تجهيز ملف الـ PDF وإرساله عبر الواتساب مع جدول الدفعات",
            followerId = id1,
            followerName = f1.name,
            priority = "عاجل",
            dueDate = System.currentTimeMillis() + 3600000L * 4,
            isCompleted = false,
            category = "إرسال عرض"
        )
        val t2 = TaskEntity(
            title = "مكالمة هاتفية لمتابعة استفسار سارة",
            description = "توضيح مميزات الباقة السنوية والرد على أسئلة الدعم الفني",
            followerId = id2,
            followerName = f2.name,
            priority = "عالية الأهمية",
            dueDate = System.currentTimeMillis() + 3600000L * 24,
            isCompleted = false,
            category = "اتصال هاتفي"
        )
        val t3 = TaskEntity(
            title = "اجتماع توقيع مذكرة التفاهم مع د. خالد",
            description = "عقد اجتماع فيديو عبر Google Meet لمناقشة بنود الشراكة",
            followerId = id3,
            followerName = f3.name,
            priority = "متوسطة",
            dueDate = System.currentTimeMillis() + 86400000L * 2,
            isCompleted = false,
            category = "اجتماع"
        )
        val t4 = TaskEntity(
            title = "تسجيل تقرير المتابعة الأسبوعي",
            description = "حصر كافة التفاعلات مع المتابعين وتحديث سجل الملاحظات",
            followerId = id4,
            followerName = f4.name,
            priority = "عادية",
            dueDate = System.currentTimeMillis() - 3600000L * 10,
            isCompleted = true,
            completedAt = System.currentTimeMillis() - 3600000L * 8,
            category = "متابعة دورية"
        )

        repository.insertTask(t1)
        repository.insertTask(t2)
        repository.insertTask(t3)
        repository.insertTask(t4)

        // Seed Initial Notifications/Alerts
        val n1 = NotificationAlertEntity(
            title = "تذكير بمهمة عاجلة",
            message = "موعد إرسال عرض الأسعار لم. أحمد يقترب (متبقي ساعات قليلة)",
            type = "عاجل",
            timestamp = System.currentTimeMillis() - 3600000L * 1,
            isRead = false,
            relatedTaskId = 1,
            relatedFollowerId = id1
        )
        val n2 = NotificationAlertEntity(
            title = "متابع جديد يحتاج تواصل",
            message = "قامت سارة المنصوري بإرسال استفسار جديد بخصوص مواعيد التدشين",
            type = "تنبيه متابع",
            timestamp = System.currentTimeMillis() - 3600000L * 4,
            isRead = false,
            relatedFollowerId = id2
        )
        val n3 = NotificationAlertEntity(
            title = "مرحباً بك في تطبيق مُنجز!",
            message = "منصتك المتكاملة باللون النبيتي لإدارة المتابعين والتواصل السريع وإنجاز المهام فورياً",
            type = "إشعار عام",
            timestamp = System.currentTimeMillis() - 86400000L,
            isRead = true
        )

        repository.insertNotification(n1)
        repository.insertNotification(n2)
        repository.insertNotification(n3)
    }
}
