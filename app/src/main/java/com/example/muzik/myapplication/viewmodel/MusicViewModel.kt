package com.example.muzik.myapplication.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzik.myapplication.models.FavoritesManager
import com.example.muzik.myapplication.models.ReviewManager
import com.example.muzik.myapplication.models.Music
import com.example.muzik.myapplication.models.muzicList
import com.example.muzik.myapplication.service.MusicService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlayerState(
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val duration: Int = 0,
    val currentPosition: Int = 0,
    val isLooping: Boolean = false,
    val isShuffle: Boolean = false,
    val activeTab: Int = 0          // 0 = All Songs, 1 = Favorites
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val songs: List<Music> = muzicList

    private val _state       = MutableStateFlow(PlayerState())
    val playerState          = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery          = _searchQuery.asStateFlow()

    private val _favorites   = MutableStateFlow<Set<Int>>(emptySet())
    val favorites            = _favorites.asStateFlow()

    private val favManager    = FavoritesManager(application)
    val reviewManager         = ReviewManager(application)

    private var service: MusicService? = null
    private var bound        = false
    private var progressJob: Job? = null

    // Shuffle queue
    private var shuffleQueue: MutableList<Int> = mutableListOf()
    private var shufflePos: Int = 0

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as MusicService.MusicBinder).getService()
            bound = true
            service!!.onStateChanged = { syncState() }
            service!!.onCompletionCallback = { playNextSong() }
            syncState()
        }
        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
            service = null
        }
    }

    init {
        _favorites.value = favManager.getAll()
        val intent = Intent(application, MusicService::class.java)
        application.startService(intent)
        application.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    // ── Tab ───────────────────────────────────────────────────────

    fun setActiveTab(tab: Int) {
        _state.update { it.copy(activeTab = tab) }
    }

    // ── Search ────────────────────────────────────────────────────

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filteredSongs(query: String): List<Pair<Int, Music>> {
        val q = query.trim().lowercase()
        return songs.mapIndexed { i, m -> i to m }
            .filter { (_, m) ->
                q.isEmpty() ||
                m.songName.lowercase().contains(q) ||
                m.artistName.lowercase().contains(q)
            }
    }

    // ── Favorites ─────────────────────────────────────────────────

    fun toggleFavorite(index: Int) {
        favManager.toggle(index)
        _favorites.value = favManager.getAll()
    }

    fun isFavorite(index: Int) = _favorites.value.contains(index)

    // ── Playback ──────────────────────────────────────────────────

    fun playSongAt(index: Int) {
        if (_state.value.isShuffle) buildShuffleQueue(index)
        reviewManager.incrementPlayCount()   // play count oshiramiz
        ensureService { it.playSongAt(index) }
    }

    fun togglePlayPause() {
        ensureService { if (it.isPlaying) it.pause() else it.resume() }
    }

    fun playNextSong() {
        val currentIdx = _state.value.currentIndex
        val next = when {
            _state.value.isShuffle -> nextShuffle()
            _state.value.activeTab == 1 -> {
                // Favorites tab — faqat favorites ichida keyingiga o'tish
                val favList = _favorites.value.sorted()
                val pos = favList.indexOf(currentIdx)
                if (pos == -1 || favList.isEmpty()) (currentIdx + 1) % songs.size
                else favList[(pos + 1) % favList.size]
            }
            else -> (currentIdx + 1) % songs.size
        }
        ensureService { it.playSongAt(next) }
    }

    fun playPreviousSong() {
        val currentIdx = _state.value.currentIndex
        val prev = when {
            _state.value.isShuffle -> prevShuffle()
            _state.value.activeTab == 1 -> {
                // Favorites tab — faqat favorites ichida oldingiga o'tish
                val favList = _favorites.value.sorted()
                val pos = favList.indexOf(currentIdx)
                if (pos == -1 || favList.isEmpty()) {
                    if (currentIdx - 1 < 0) songs.lastIndex else currentIdx - 1
                } else {
                    favList[if (pos - 1 < 0) favList.lastIndex else pos - 1]
                }
            }
            else -> {
                if (currentIdx - 1 < 0) songs.lastIndex else currentIdx - 1
            }
        }
        ensureService { it.playSongAt(prev) }
    }

    fun seekTo(ms: Int) {
        ensureService { it.seekTo(ms) }
        _state.update { it.copy(currentPosition = ms) }
    }

    fun toggleLoop() {
        _state.update { it.copy(isLooping = !it.isLooping) }
    }

    fun toggleShuffle() {
        val newShuffle = !_state.value.isShuffle
        _state.update { it.copy(isShuffle = newShuffle) }
        if (newShuffle) buildShuffleQueue(_state.value.currentIndex)
    }

    // ── Shuffle logic ─────────────────────────────────────────────

    private fun buildShuffleQueue(currentIndex: Int) {
        shuffleQueue = songs.indices
            .filter { it != currentIndex }
            .shuffled()
            .toMutableList()
        shuffleQueue.add(0, currentIndex)
        shufflePos = 0
    }

    private fun nextShuffle(): Int {
        shufflePos = (shufflePos + 1) % shuffleQueue.size
        return shuffleQueue[shufflePos]
    }

    private fun prevShuffle(): Int {
        shufflePos = if (shufflePos - 1 < 0) shuffleQueue.lastIndex else shufflePos - 1
        return shuffleQueue[shufflePos]
    }

    // ── Internal ──────────────────────────────────────────────────

    private fun ensureService(block: (MusicService) -> Unit) {
        service?.let(block)
    }

    private fun syncState() {
        val svc = service ?: return
        _state.update {
            it.copy(
                currentIndex = svc.currentIndex,
                isPlaying    = svc.isPlaying,
                duration     = svc.getDuration()
            )
        }
        if (svc.isPlaying) startProgress() else stopProgress()
    }

    private fun startProgress() {
        if (progressJob?.isActive == true) return
        progressJob = viewModelScope.launch {
            while (true) {
                delay(300)
                val svc = service ?: break
                val pos = svc.getCurrentPosition()
                val dur = svc.getDuration()
                _state.update { st ->
                    st.copy(
                        currentPosition = pos,
                        duration = dur,
                        progress = if (dur > 0) pos.toFloat() / dur else 0f
                    )
                }
            }
        }
    }

    private fun stopProgress() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun onCleared() {
        stopProgress()
        if (bound) {
            getApplication<Application>().unbindService(connection)
            bound = false
        }
        super.onCleared()
    }
}
