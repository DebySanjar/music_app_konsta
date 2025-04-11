package com.example.muzik.myapplication.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.muzik.myapplication.R
import com.example.muzik.myapplication.databinding.FragmentShowBinding
import com.example.muzik.myapplication.models.MediaPlayerSingleton
import com.example.muzik.myapplication.models.Music

class ShowFragment : Fragment() {

    private val binding by lazy { FragmentShowBinding.inflate(layoutInflater) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var musicList: Array<Music> = emptyArray()
    private var currentPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = ShowFragmentArgs.fromBundle(requireArguments())
        musicList = args.songList
        currentPosition = getCurrentPosition(args.fileResId)

        playMusicAt(currentPosition)
        setupSeekBar()

        binding.btnPlayPause.setOnClickListener {
            if (MediaPlayerSingleton.isPlaying()) {
                pauseMusic()
            } else {
                continueMusic()
            }
        }

        binding.btnNext.setOnClickListener {
            playNextSong()
        }

        binding.btnPrev.setOnClickListener {
            playPreviousSong()
        }
    }

    private fun playMusicAt(position: Int) {
        val music = musicList[position]
        binding.autorName.text = music.artistName
        binding.soundName.text = music.songName

        MediaPlayerSingleton.playNewSong(requireContext(), music.fileResId!!) {
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            playNextSong()
        }

        binding.lottieView.playAnimation()
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        binding.seekBar.max = MediaPlayerSingleton.getDuration()

        runnable = object : Runnable {
            override fun run() {
                val currentPos = MediaPlayerSingleton.getCurrentPosition()
                binding.seekBar.progress = currentPos
                handler.postDelayed(this, 500)
            }
        }
        handler.post(runnable)
    }

    private fun playNextSong() {
        currentPosition++
        if (currentPosition >= musicList.size) {
            currentPosition = 0
        }
        handler.removeCallbacks(runnable)
        playMusicAt(currentPosition)
    }

    private fun playPreviousSong() {
        currentPosition--
        if (currentPosition < 0) {
            currentPosition = musicList.size - 1
        }
        handler.removeCallbacks(runnable)
        playMusicAt(currentPosition)
    }

    private fun pauseMusic() {
        binding.lottieView.pauseAnimation()
        MediaPlayerSingleton.pauseSong()
        binding.btnPlayPause.setImageResource(R.drawable.ic_play)
    }

    private fun continueMusic() {
        binding.lottieView.playAnimation()
        MediaPlayerSingleton.continueSong()
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MediaPlayerSingleton.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getCurrentPosition(fileResId: Int): Int {
        for (i in musicList.indices) {
            if (musicList[i].fileResId == fileResId) return i
        }
        return 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }
}