package com.example.muzik.myapplication.Adapters

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.databinding.ItemMuzicBinding
import com.example.muzik.myapplication.models.Music

class MusicAdapter(
    private val list: MutableList<Music>,
    private val lastPosition: Int,
    private val onItemClick: (Music, Int) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(val binding: ItemMuzicBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var isTintApplied = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMuzicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MusicViewHolder, @SuppressLint("RecyclerView") position: Int
    ) {
        val music = list[position]
        holder.binding.apply {
            tvName.text = music.songName
            tvArtist.text = music.artistName
            listPosition.text = "${position + 1}."

            // Faqat birinchi marta tint qo‘llash
            if (!holder.isTintApplied) {
                lottieView.addValueCallback(
                    KeyPath("**"),
                    LottieProperty.COLOR_FILTER
                ) {
                    PorterDuffColorFilter(
                        ContextCompat.getColor(holder.itemView.context, R.color.myitem),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
                holder.isTintApplied = true
            }

            // isPlaying ga qarab animatsiya ko‘rsatish
            if (music.isPlaying) {
                lottieView.visibility = View.VISIBLE
                lottieView.playAnimation()
            } else {
                lottieView.visibility = View.INVISIBLE
                lottieView.pauseAnimation()
            }

            holder.itemView.setOnClickListener {
                onItemClick(music, position)
            }
        }
    }

    override fun getItemCount(): Int = list.size
}
