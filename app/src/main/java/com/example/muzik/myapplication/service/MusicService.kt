package com.example.muzik.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.muzik.myapplication.MainActivity
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.models.Music
import com.example.muzik.myapplication.models.muzicList

class MusicService : Service() {

    companion object {
        const val CHANNEL_ID = "konsta_music_channel"
        const val NOTIF_ID = 1
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_STOP = "ACTION_STOP"
    }

    inner class MusicBinder : Binder() {
        fun getService() = this@MusicService
    }

    private val binder = MusicBinder()

    val songs: List<Music> = muzicList
    var currentIndex: Int = 0
        private set
    var isPlaying: Boolean = false
        private set

    private var player: MediaPlayer? = null

    var onStateChanged: (() -> Unit)? = null
    var onCompletionCallback: (() -> Unit)? = null

    // ── Lifecycle ────────────────────────────────────────────────

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification())
        when (intent?.action) {
            ACTION_PLAY -> resume()
            ACTION_PAUSE -> pause()
            ACTION_NEXT -> playNext()
            ACTION_PREV -> playPrev()
            ACTION_STOP -> {
                stop(); stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    // ── Public API ────────────────────────────────────────────────

    fun playSongAt(index: Int) {
        val i = index.coerceIn(0, songs.lastIndex)
        currentIndex = i
        stop()
        player = MediaPlayer.create(this, songs[i].fileResId) ?: return
        player!!.setOnCompletionListener {
            onCompletionCallback?.invoke() ?: playNext()
        }
        player!!.start()
        isPlaying = true
        updateNotification()
        onStateChanged?.invoke()
    }

    fun pause() {
        player?.pause()
        isPlaying = false
        updateNotification()
        onStateChanged?.invoke()
    }

    fun resume() {
        player?.start()
        isPlaying = true
        updateNotification()
        onStateChanged?.invoke()
    }

    fun playNext() {
        playSongAt((currentIndex + 1) % songs.size)
    }

    fun playPrev() {
        val prev = if (currentIndex - 1 < 0) songs.lastIndex else currentIndex - 1
        playSongAt(prev)
    }

    fun seekTo(ms: Int) {
        player?.seekTo(ms)
    }

    fun getCurrentPosition(): Int = player?.currentPosition ?: 0
    fun getDuration(): Int = player?.duration ?: 0

    private fun stop() {
        player?.release()
        player = null
        isPlaying = false
    }

    // ── Notification ─────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Konsta Music",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music playback controls"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun actionIntent(action: String, reqCode: Int): PendingIntent =
        PendingIntent.getService(
            this, reqCode,
            Intent(this, MusicService::class.java).apply { this.action = action },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun buildNotification(): Notification {
        val song = songs.getOrNull(currentIndex)

        val openPi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Album art — largeIcon uchun bitmap
        val albumBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_2)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)   // status bar — kichik oq icon
            .setLargeIcon(albumBitmap)                   // notification panel — album art
            .setContentTitle(song?.songName ?: "")
            .setContentText(song?.artistName ?: "")
            .setContentIntent(openPi)
            .setOngoing(isPlaying)
            .setSilent(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            // ── Actions (compact view'da ko'rinadigan 3 ta) ──────
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_prev,
                    "Previous",
                    actionIntent(ACTION_PREV, 1)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    if (isPlaying) "Pause" else "Play",
                    actionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY, 2)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_skip,
                    "Next",
                    actionIntent(ACTION_NEXT, 3)
                )
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    fun updateNotification() {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification())
    }
}
