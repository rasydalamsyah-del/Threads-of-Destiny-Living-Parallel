package com.valeriusgames.threadsofdestiny.audio

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer

/**
 * Musik latar (assets/musik) dan efek suara (assets/suara).
 * Volume mengikuti halaman Pengaturan.
 */
class AudioPlayer(private val ctx: Context) {

    private var bgm: MediaPlayer? = null
    var musicName: String? = null
        private set

    private var musicVolume = 1f
    private var sfxVolume = 1f
    private val sfxPlayers = mutableListOf<MediaPlayer>()
    private var paused = false

    private val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    fun setVolumes(music: Float, sfx: Float) {
        musicVolume = music
        sfxVolume = sfx
        bgm?.setVolume(music, music)
    }

    /** Putar musik berulang. Kalau file tidak ada, diam saja. */
    fun playMusic(name: String) {
        if (name == musicName && bgm != null) return
        stopMusic()
        musicName = name
        val afd = open("musik", name) ?: return
        try {
            bgm = MediaPlayer().apply {
                setAudioAttributes(attrs)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = true
                setVolume(musicVolume, musicVolume)
                prepare()
                if (!paused) start()
            }
        } catch (_: Exception) {
            bgm = null
        } finally {
            afd.close()
        }
    }

    fun stopMusic() {
        bgm?.release()
        bgm = null
        musicName = null
    }

    /** Putar efek suara sekali. Kalau file tidak ada, diam saja. */
    fun playSound(name: String) {
        if (sfxVolume <= 0f) return
        val afd = open("suara", name) ?: return
        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(attrs)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            mp.setVolume(sfxVolume, sfxVolume)
            mp.setOnCompletionListener {
                sfxPlayers.remove(it)
                it.release()
            }
            mp.prepare()
            mp.start()
            sfxPlayers += mp
        } catch (_: Exception) {
        } finally {
            afd.close()
        }
    }

    fun pause() {
        paused = true
        bgm?.let { if (it.isPlaying) it.pause() }
    }

    fun resume() {
        paused = false
        bgm?.start()
    }

    fun release() {
        stopMusic()
        sfxPlayers.forEach { it.release() }
        sfxPlayers.clear()
    }

    private fun open(folder: String, name: String): AssetFileDescriptor? {
        for (ext in listOf("mp3", "ogg", "m4a", "wav")) {
            try {
                return ctx.assets.openFd("$folder/$name.$ext")
            } catch (_: Exception) {
            }
        }
        return null
    }
}
