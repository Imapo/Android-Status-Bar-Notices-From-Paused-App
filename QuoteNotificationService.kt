package com.example.motivationalquotefortheday

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager

class QuoteNotificationService : Service() {

    private lateinit var handler: Handler
    private lateinit var quoteUpdaterRunnable: Runnable

    private val quotes: List<String> by lazy {
        listOf(
            getString(R.string.quote_1),
            getString(R.string.quote_2),
            getString(R.string.quote_3),
            getString(R.string.quote_4),
            getString(R.string.quote_5),
            getString(R.string.quote_6),
            getString(R.string.quote_7),
            getString(R.string.quote_8),
            getString(R.string.quote_9),
            getString(R.string.quote_10),
            getString(R.string.quote_11),
            getString(R.string.quote_12),
            getString(R.string.quote_13),
            getString(R.string.quote_14),
            getString(R.string.quote_15),
            getString(R.string.quote_16),
            getString(R.string.quote_17),
            getString(R.string.quote_18),
            getString(R.string.quote_19),
            getString(R.string.quote_20)
        )
    }

    override fun onCreate() {
        super.onCreate()

        // Запуск Foreground Service с уведомлением
        val notificationId = 1
        val channelId = "status_bar_channel"

        // Создание уведомления
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Цитаты дня")
            .setContentText("Ваши цитаты обновляются в фоновом режиме")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Помещение в Foreground
        startForeground(notificationId, notification)

        // Создание канала уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Status Bar Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for displaying background quotes"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        // Инициализация Handler для обновления цитаты
        handler = Handler(Looper.getMainLooper())
        quoteUpdaterRunnable = object : Runnable {
            override fun run() {
                val newQuote = getRandomQuote(exclude = null)
                updateQuote(newQuote)
                handler.postDelayed(this, 60000) // Повторяем через 60 секунд
            }
        }
        handler.postDelayed(quoteUpdaterRunnable, 60000) // Первый запуск через 1 минуту
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(quoteUpdaterRunnable) // Удаляем колбэки при уничтожении сервиса
    }

    private fun updateQuote(currentQuote: String) {
        val newQuote = getRandomQuote(exclude = currentQuote)
        showStatusMessage(newQuote)
    }

    private fun showStatusMessage(message: String) {
        val channelId = "status_bar_channel"
        val notificationId = 101

        // Проверка разрешения на отправку уведомлений для Android 13 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Если разрешение не предоставлено, можно либо запросить его, либо просто выйти
                return
            }
        }

        // Создание уведомления
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.quote_of_day))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Отправка уведомления
        with(NotificationManagerCompat.from(this)) {
            // Проверяем разрешение для Android 13 и выше
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(notificationId, notification)
            }
        }
    }

    private fun getRandomQuote(exclude: String?): String {
        val availableQuotes = if (exclude != null) {
            quotes.filter { it != exclude }
        } else {
            quotes
        }
        return availableQuotes.random()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
