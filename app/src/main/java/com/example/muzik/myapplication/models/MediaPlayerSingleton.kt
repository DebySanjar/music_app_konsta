package com.example.muzik.myapplication.models

import android.content.Context
import android.media.MediaPlayer


object MediaPlayerSingleton {
    var mediaPlayer: MediaPlayer? = null

    fun playNewSong(context: Context, resId: Int, onCompletion: () -> Unit) {
        releasePlayer()
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            start()
            setOnCompletionListener {
                if (isLooping) {
                    seekTo(0)
                    start()
                } else {
                    onCompletion()
                }
            }
        }
    }

    fun pauseSong() {
        mediaPlayer?.pause()
    }

    fun continueSong() {
        mediaPlayer?.start()
    }

    fun stopSong() {
        mediaPlayer?.stop()
        releasePlayer()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
