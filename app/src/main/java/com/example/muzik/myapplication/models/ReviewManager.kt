package com.example.muzik.myapplication.models

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Google Play In-App Review trigger logikasi (2025 API — hali o'zgarmagan):
 *
 * Ko'rsatish sharti (HAMMASI bir vaqtda):
 *   1. Kamida 3 ta qo'shiq tinglangan
 *   2. Ilova o'rnatilganidan 2 kun o'tgan
 *   3. Hech ko'rsatilmagan (bir marta)
 *   4. So'nggi so'rovdan 30 daqiqa o'tgan (throttle)
 *
 * MUHIM: Google o'zi ham throttle qiladi — ko'p so'rasangiz dialog chiqmaydi.
 */
class ReviewManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("review_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PLAY_COUNT     = "play_count"
        private const val KEY_INSTALL_DATE   = "install_date"
        private const val KEY_REVIEW_SHOWN   = "review_shown"
        private const val KEY_LAST_REQUEST   = "last_request_time"
        private const val MIN_PLAY_COUNT     = 3
        private const val MIN_DAYS_MS        = 2L * 24 * 60 * 60 * 1000   // 2 kun ms
        private const val MIN_THROTTLE_MS    = 30L * 60 * 1000             // 30 daqiqa ms
    }

    init {
        if (!prefs.contains(KEY_INSTALL_DATE)) {
            prefs.edit().putLong(KEY_INSTALL_DATE, System.currentTimeMillis()).apply()
        }
    }

    fun incrementPlayCount() {
        // Ko'rsatilgan bo'lsa yanada saqlamaya hojat yo'q
        if (prefs.getBoolean(KEY_REVIEW_SHOWN, false)) return
        val count = prefs.getInt(KEY_PLAY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_PLAY_COUNT, count).apply()
    }

    fun shouldShowReview(): Boolean {
        if (prefs.getBoolean(KEY_REVIEW_SHOWN, false)) return false

        val playCount    = prefs.getInt(KEY_PLAY_COUNT, 0)
        val installDate  = prefs.getLong(KEY_INSTALL_DATE, System.currentTimeMillis())
        val lastRequest  = prefs.getLong(KEY_LAST_REQUEST, 0L)
        val now          = System.currentTimeMillis()

        val enoughPlays  = playCount >= MIN_PLAY_COUNT
        val enoughDays   = (now - installDate) >= MIN_DAYS_MS
        val notThrottled = (now - lastRequest) >= MIN_THROTTLE_MS

        return enoughPlays && enoughDays && notThrottled
    }

    fun launchReview(activity: Activity) {
        // So'rov vaqtini saqlaymiz — throttle uchun
        prefs.edit().putLong(KEY_LAST_REQUEST, System.currentTimeMillis()).apply()

        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            manager.launchReviewFlow(activity, task.result)
                .addOnCompleteListener {
                    // Google API muvaffaqiyat/muvaffaqiyatsizligini aytmaydi
                    // Biz shu nuqtada ko'rsatildi deb hisoblaymiz
                    prefs.edit().putBoolean(KEY_REVIEW_SHOWN, true).apply()
                }
        }
    }
}
