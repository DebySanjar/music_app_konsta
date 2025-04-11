package com.example.muzik.myapplication.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.muzik.myapplication.Adapters.MusicAdapter
import com.example.muzik.myapplication.databinding.FragmentListBinding
import com.example.muzik.myapplication.models.muzicList

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!


    private var lastPlayingPosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MusicAdapter(
            muzicList.toMutableList(),
            lastPlayingPosition
        ) { selectedMusic, position ->
            if (lastPlayingPosition != -1 && lastPlayingPosition != position) {
                muzicList[lastPlayingPosition].isPlaying = false
            }
            // yangi item
            muzicList[position].isPlaying = true
            lastPlayingPosition = position

            // Fragmentdan ShowFragmentga o'tish
            val action = ListFragmentDirections.actionListFragmentToShowFragment(
                selectedMusic.artistName,
                selectedMusic.songName,
                selectedMusic.fileResId,
                muzicList.toTypedArray()
            )

            findNavController().navigate(action)
            binding.recy.adapter?.notifyDataSetChanged()
        }
        binding.recy.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
