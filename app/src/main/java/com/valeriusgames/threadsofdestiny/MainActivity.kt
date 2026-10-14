package com.valeriusgames.threadsofdestiny

import android.app.Activity
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import com.valeriusgames.threadsofdestiny.settings.GameSettings
import kotlin.math.max

/**
 * Alur pembuka game:
 *   1. SPLASH : gambar pembuka tampil selama [SPLASH_DURATION_MS]
 *   2. VIDEO  : transisi (fade) ke video intro, langsung diputar
 *   3. MENU   : setelah video selesai, masuk ke halaman utama
 */
class MainActivity : Activity() {

    private enum class Stage { SPLASH, VIDEO, MENU }

    companion object {
        /** Lama gambar splash tampil (milidetik). */
        const val SPLASH_DURATION_MS = 3000L

        /** Lama transisi splash -> video (milidetik). */
        const val SPLASH_TO_VIDEO_FADE_MS = 700L

        /** Lama transisi video -> menu (milidetik). */
        const val VIDEO_TO_MENU_FADE_MS = 900L

        /** true = video bisa dilewati dengan mengetuk layar. */
        const val ALLOW_SKIP_VIDEO = true

        private const val KEY_STAGE = "stage"
        private const val KEY_VIDEO_POS = "video_pos"
        private const val KEY_SPLASH_LEFT = "splash_left"
    }

    private var stage = Stage.SPLASH
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var splashView: ImageView
    private lateinit var videoView: TextureView
    private lateinit var menu: MainMenu

    private var player: MediaPlayer? = null
    private var playerPrepared = false
    private var videoSurface: Surface? = null
    private var videoPosition = 0
    private var isActive = false
    private var videoFailed = false

    private var splashRemainingMs = SPLASH_DURATION_MS
    private var splashStartedAt = 0L

    private val goToVideo = Runnable { startVideoStage() }

    // ------------------------------------------------------------------ lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enterImmersiveMode()

        splashView = findViewById(R.id.splash)
        videoView = findViewById(R.id.video)
        menu = MainMenu(this, findViewById(R.id.menu_root))

        // Pulihkan posisi kalau Android sempat menutup aplikasi di latar belakang
        if (savedInstanceState != null) {
            stage = Stage.valueOf(savedInstanceState.getString(KEY_STAGE, Stage.SPLASH.name))
            videoPosition = savedInstanceState.getInt(KEY_VIDEO_POS, 0)
            splashRemainingMs = savedInstanceState.getLong(KEY_SPLASH_LEFT, SPLASH_DURATION_MS)
        }

        when (stage) {
            Stage.MENU -> {
                splashView.visibility = View.GONE
                videoView.visibility = View.GONE
                menu.show(animate = false)
                return
            }
            Stage.VIDEO -> {
                splashView.visibility = View.GONE
                videoView.alpha = 1f
            }
            Stage.SPLASH -> Unit
        }

        videoView.surfaceTextureListener = surfaceListener
        if (ALLOW_SKIP_VIDEO) {
            videoView.setOnClickListener { if (stage == Stage.VIDEO) finishVideo() }
        }
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        enterImmersiveMode()
        when (stage) {
            Stage.SPLASH -> {
                splashStartedAt = SystemClock.uptimeMillis()
                handler.postDelayed(goToVideo, splashRemainingMs)
            }
            Stage.VIDEO -> if (playerPrepared) player?.start()
            Stage.MENU -> menu.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        isActive = false
        when (stage) {
            Stage.SPLASH -> {
                handler.removeCallbacks(goToVideo)
                val elapsed = SystemClock.uptimeMillis() - splashStartedAt
                splashRemainingMs = (splashRemainingMs - elapsed).coerceAtLeast(0L)
            }
            Stage.VIDEO -> player?.let {
                if (playerPrepared && it.isPlaying) it.pause()
                videoPosition = it.currentPosition
            }
            Stage.MENU -> menu.onPause()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_STAGE, stage.name)
        outState.putInt(KEY_VIDEO_POS, videoPosition)
        outState.putLong(KEY_SPLASH_LEFT, splashRemainingMs)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        releasePlayer()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    // ------------------------------------------------------------------ alur

    private fun startVideoStage() {
        if (stage != Stage.SPLASH) return
        if (videoFailed || GameSettings.load(this).skipIntro) {
            finishVideo()
            return
        }
        stage = Stage.VIDEO

        splashView.animate()
            .alpha(0f)
            .setDuration(SPLASH_TO_VIDEO_FADE_MS)
            .withEndAction { splashView.visibility = View.GONE }
            .start()
        videoView.animate()
            .alpha(1f)
            .setDuration(SPLASH_TO_VIDEO_FADE_MS)
            .start()

        if (playerPrepared) player?.start()
    }

    private fun finishVideo() {
        if (stage == Stage.MENU) return
        stage = Stage.MENU
        handler.removeCallbacks(goToVideo)
        if (splashView.visibility == View.VISIBLE) {
            splashView.animate()
                .alpha(0f)
                .setDuration(VIDEO_TO_MENU_FADE_MS)
                .withEndAction { splashView.visibility = View.GONE }
                .start()
        }

        player?.let { if (playerPrepared && it.isPlaying) it.pause() }
        menu.show(animate = true)
        videoView.animate()
            .alpha(0f)
            .setDuration(VIDEO_TO_MENU_FADE_MS)
            .withEndAction {
                videoView.visibility = View.GONE
                releasePlayer()
            }
            .start()
    }

    // ------------------------------------------------------------------ video

    private val surfaceListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            if (stage != Stage.MENU) preparePlayer(surface)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            player?.let { if (playerPrepared) applyCenterCrop(it.videoWidth, it.videoHeight) }
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            player?.let { if (playerPrepared) videoPosition = it.currentPosition }
            releasePlayer()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
    }

    private fun preparePlayer(texture: SurfaceTexture) {
        releasePlayer()
        val surface = Surface(texture).also { videoSurface = it }
        val mp = MediaPlayer()
        player = mp
        try {
            mp.setDataSource(this, Uri.parse("android.resource://$packageName/${R.raw.intro}"))
            mp.setSurface(surface)
            mp.setOnPreparedListener {
                playerPrepared = true
                val vol = GameSettings.load(this).musicVolume
                it.setVolume(vol, vol)
                applyCenterCrop(it.videoWidth, it.videoHeight)
                if (videoPosition > 0) it.seekTo(videoPosition)
                if (stage == Stage.VIDEO && isActive) it.start()
            }
            mp.setOnCompletionListener { finishVideo() }
            mp.setOnErrorListener { _, _, _ ->
                onVideoError()
                true
            }
            mp.prepareAsync()
        } catch (e: Exception) {
            onVideoError()
        }
    }

    /** Kalau video gagal diputar, jangan macet: lanjut ke menu (splash tetap tampil penuh). */
    private fun onVideoError() {
        if (stage == Stage.SPLASH) videoFailed = true else finishVideo()
    }

    /** Video memenuhi layar tanpa gepeng (seperti centerCrop pada gambar). */
    private fun applyCenterCrop(videoW: Int, videoH: Int) {
        val viewW = videoView.width.toFloat()
        val viewH = videoView.height.toFloat()
        if (viewW == 0f || viewH == 0f || videoW == 0 || videoH == 0) return
        val scale = max(viewW / videoW, viewH / videoH)
        val matrix = Matrix().apply {
            setScale(videoW * scale / viewW, videoH * scale / viewH, viewW / 2f, viewH / 2f)
        }
        videoView.setTransform(matrix)
    }

    private fun releasePlayer() {
        player?.let {
            it.setOnCompletionListener(null)
            it.setOnErrorListener(null)
            it.setOnPreparedListener(null)
            it.release()
        }
        player = null
        playerPrepared = false
        videoSurface?.release()
        videoSurface = null
    }
}
