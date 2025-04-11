package com.example.muzik.myapplication.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SongModel(
    val artistName: String,
    val songName: String,
    val fileResId: Int
) : Parcelable
