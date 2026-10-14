package com.valeriusgames.threadsofdestiny.game

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.valeriusgames.threadsofdestiny.FantasyButton
import com.valeriusgames.threadsofdestiny.R
import com.valeriusgames.threadsofdestiny.SparkleView
import com.valeriusgames.threadsofdestiny.enterImmersiveMode
import com.valeriusgames.threadsofdestiny.story.StoryScript
import com.valeriusgames.threadsofdestiny.story.StoryScript.Cmd

/**
 * Layar permainan visual novel.
 * Urutan: isi nama → jalankan naskah assets/naskah/prolog.txt
 */
class GameActivity : Activity() {

    companion object {
        /** File naskah pertama yang dijalankan. */
        const val FIRST_SCRIPT = "naskah/prolog.txt"

        /** Kecepatan teks muncul (milidetik per huruf). */
        const val TYPE_DELAY_MS = 28L

        const val PREFS = "player"
        const val KEY_NAME = "name"
    }

    private enum class Wait { BUSY, TAP, PHONE }

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var bg: BackgroundLayer
    private lateinit var vignette: View
    private lateinit var clock: TextView
    private lateinit var dialog: View
    private lateinit var dialogBox: View
    private lateinit var dialogText: TextView
    private lateinit var nameTag: TextView
    private lateinit var nextIndicator: View
    private lateinit var phoneIcon: PhoneIconView
    private lateinit var phoneHint: View
    private lateinit var black: View
    private lateinit var eyelid: EyelidView
    private lateinit var timecard: TextView
    private lateinit var phone: PhoneScreen
    private lateinit var nameEntry: View
    private lateinit var nameInput: EditText
    private lateinit var nameError: View
    private lateinit var nameSparkles: SparkleView

    private var script: List<Cmd> = emptyList()
    private var pc = 0
    private var wait = Wait.BUSY
    private var playerName = ""
    private var memoryMode = false
    private var clockText: String? = null

    private var fullText: CharSequence = ""
    private var typed = 0
    private var typing = false
    private var indicatorAnim: ValueAnimator? = null

    private val serifItalic: Typeface by lazy { Typeface.create(Typeface.SERIF, Typeface.ITALIC) }
    private val sansItalic: Typeface by lazy { Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC) }

    // ================================================================== lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        enterImmersiveMode()

        bg = BackgroundLayer(findViewById<FrameLayout>(R.id.bg_container))
        vignette = findViewById(R.id.vignette)
        clock = findViewById(R.id.clock)
        dialog = findViewById(R.id.dialog)
        dialogBox = findViewById(R.id.dialog_box)
        dialogText = findViewById(R.id.dialog_text)
        nameTag = findViewById(R.id.name_tag)
        nextIndicator = findViewById(R.id.next_indicator)
        phoneIcon = findViewById(R.id.phone_icon)
        phoneHint = findViewById(R.id.phone_hint)
        black = findViewById(R.id.black)
        eyelid = findViewById(R.id.eyelid)
        timecard = findViewById(R.id.timecard)
        nameEntry = findViewById(R.id.name_entry)
        nameInput = findViewById(R.id.name_input)
        nameError = findViewById(R.id.name_error)
        nameSparkles = findViewById(R.id.name_sparkles)
        phone = PhoneScreen(findViewById(R.id.phone_overlay)) { onPhoneClosed() }

        findViewById<View>(R.id.game_root).setOnClickListener { onScreenTap() }
        phoneIcon.setOnClickListener { openPhone() }

        try {
            script = assets.open(FIRST_SCRIPT).bufferedReader().use { StoryScript.parse(it.readLines()) }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.script_error, e.message), Toast.LENGTH_LONG).show()
        }

        setupNameEntry()
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
        if (nameEntry.visibility == View.VISIBLE) nameSparkles.start()
    }

    override fun onPause() {
        super.onPause()
        nameSparkles.stop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        indicatorAnim?.cancel()
        bg.release()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            phone.isOpen -> phone.close()
            nameEntry.visibility == View.VISIBLE -> leaveToMenu()
            else -> AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(R.string.quit_title)
                .setMessage(R.string.quit_message)
                .setPositiveButton(R.string.quit_yes) { _, _ -> leaveToMenu() }
                .setNegativeButton(R.string.quit_no, null)
                .setOnDismissListener { enterImmersiveMode() }
                .show()
        }
    }

    private fun leaveToMenu() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // ================================================================== isi nama

    private fun setupNameEntry() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        nameInput.setText(prefs.getString(KEY_NAME, ""))
        nameInput.setSelection(nameInput.text.length)
        nameInput.setOnEditorActionListener { _, actionId, event ->
            val enter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_DONE || enter) {
                confirmName()
                true
            } else false
        }
        findViewById<FantasyButton>(R.id.name_ok).setOnClickListener { confirmName() }

        val card = findViewById<View>(R.id.name_card)
        card.alpha = 0f
        card.translationY = 30 * resources.displayMetrics.density
        card.animate().alpha(1f).translationY(0f).setStartDelay(250).setDuration(700)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun confirmName() {
        val name = nameInput.text.toString().trim().replace(Regex("\\s+"), " ")
        if (name.isEmpty()) {
            nameError.visibility = View.VISIBLE
            nameInput.animate().cancel()
            nameInput.translationX = 0f
            val d = resources.displayMetrics.density
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 380
                addUpdateListener {
                    val t = it.animatedValue as Float
                    nameInput.translationX = (kotlin.math.sin(t * Math.PI * 6) * (1 - t) * 10 * d).toFloat()
                }
                start()
            }
            return
        }
        nameError.visibility = View.INVISIBLE
        playerName = name
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_NAME, name).apply()

        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(nameInput.windowToken, 0)
        nameInput.clearFocus()

        nameEntry.isClickable = false
        nameEntry.animate().alpha(0f).setDuration(900).withEndAction {
            nameEntry.visibility = View.GONE
            nameSparkles.stop()
            enterImmersiveMode()
            handler.postDelayed({ startScript() }, 500)
        }.start()
    }

    // ================================================================== naskah

    private fun startScript() {
        pc = 0
        wait = Wait.BUSY
        next()
    }

    /** Jalankan perintah naskah berikutnya sampai ada yang perlu ditunggu. */
    private fun next() {
        while (pc < script.size) {
            when (val c = script[pc++]) {
                is Cmd.Background -> { changeBackground(c) { next() }; return }
                is Cmd.FadeToBlack -> { hideDialog(); fadeBlack(1f, c.ms) { next() }; return }
                is Cmd.FadeFromBlack -> { fadeBlack(0f, c.ms) { next() }; return }
                is Cmd.TimeCard -> { showTimecard(c.text) { next() }; return }
                is Cmd.Blink -> { blink { next() }; return }
                is Cmd.Clock -> setClock(c.text)
                is Cmd.Pause -> { handler.postDelayed({ next() }, c.ms); return }
                is Cmd.Line -> { showLine(c.speaker, c.text); return }
                is Cmd.PhoneIcon -> setPhoneIconVisible(c.visible)
                is Cmd.PhoneMessage -> {
                    setPhoneIconVisible(true)
                    phone.addMessage(fill(c.sender), fill(c.text))
                    phoneIcon.setRinging(true)
                }
                is Cmd.WaitForPhone -> { waitForPhone(); return }
                is Cmd.End -> { endStory(c.text); return }
            }
        }
        endStory("Bersambung…")
    }

    private fun fill(s: String) = s.replace("{nama}", playerName).replace("{name}", playerName)

    private fun onScreenTap() {
        if (wait != Wait.TAP) return
        if (typing) {
            finishTyping()
        } else {
            wait = Wait.BUSY
            next()
        }
    }

    // ================================================================== background & efek

    private fun changeBackground(c: Cmd.Background, done: () -> Unit) {
        val instant = black.alpha >= 0.99f
        memoryMode = c.memory
        hideDialog {
            vignette.animate().alpha(if (c.memory) 1f else 0f)
                .setDuration(if (instant) 0 else BackgroundLayer.CROSSFADE_MS).start()
            bg.show(loadBackground(c.name), c.memory, c.motion, instant, done)
        }
    }

    private fun loadBackground(name: String): Bitmap? {
        val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        for (ext in listOf("jpg", "jpeg", "png", "webp")) {
            try {
                return assets.open("latar/$name.$ext").use { BitmapFactory.decodeStream(it, null, opts) }
            } catch (_: Exception) {
            }
        }
        Toast.makeText(this, "Gambar latar/$name tidak ditemukan", Toast.LENGTH_SHORT).show()
        return null
    }

    private fun fadeBlack(target: Float, ms: Long, done: () -> Unit) {
        black.animate().cancel()
        black.animate().alpha(target).setDuration(ms)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { done() }.start()
    }

    private fun showTimecard(text: String, done: () -> Unit) {
        timecard.text = fill(text)
        timecard.alpha = 0f
        timecard.animate().alpha(1f).setDuration(900).withEndAction {
            timecard.animate().alpha(0f).setStartDelay(1600).setDuration(900).withEndAction {
                timecard.animate().setStartDelay(0)
                handler.postDelayed({ done() }, 400)
            }.start()
        }.start()
    }

    /** Efek bangun tidur: mata membuka-menutup, pandangan buram lalu jelas. */
    private fun blink(done: () -> Unit) {
        eyelid.openness = 0f
        eyelid.visibility = View.VISIBLE
        black.animate().cancel()
        black.alpha = 0f

        // (keterbukaan mata, durasi ms)
        val steps = listOf(
            0f to 700L,
            0.32f to 800L,
            0f to 280L,
            0.55f to 750L,
            0.05f to 240L,
            1f to 1300L,
        )
        val total = steps.sumOf { it.second }
        ValueAnimator.ofFloat(28f, 0f).apply {
            duration = total
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { bg.setBlur(it.animatedValue as Float) }
            start()
        }

        fun run(i: Int) {
            if (i >= steps.size) {
                eyelid.visibility = View.GONE
                bg.setBlur(0f)
                handler.postDelayed({ done() }, 300)
                return
            }
            val (target, ms) = steps[i]
            ValueAnimator.ofFloat(eyelid.openness, target).apply {
                duration = ms
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener { eyelid.openness = it.animatedValue as Float }
                withEnd { run(i + 1) }
                start()
            }
        }
        run(0)
    }

    private fun setClock(text: String?) {
        clockText = text?.let { fill(it) }
        phone.setClock(clockText)
        if (clockText == null) {
            clock.animate().alpha(0f).setDuration(400).start()
        } else {
            clock.text = clockText
            clock.animate().alpha(1f).setDuration(600).start()
        }
    }

    // ================================================================== dialog

    private fun showLine(speaker: String?, raw: String) {
        val text = fill(raw)
        val narration = speaker == null
        applyDialogStyle(narration)

        if (narration) {
            nameTag.visibility = View.INVISIBLE
        } else {
            nameTag.visibility = View.VISIBLE
            nameTag.text = fill(speaker!!)
        }

        if (dialog.alpha < 1f) {
            dialog.animate().cancel()
            dialog.translationY = 12 * resources.displayMetrics.density
            dialog.animate().alpha(1f).translationY(0f).setDuration(260).start()
        }

        fullText = text
        typed = 0
        typing = true
        setIndicator(false)
        dialogText.text = typedText()
        handler.removeCallbacks(typeTick)
        handler.postDelayed(typeTick, TYPE_DELAY_MS)
        wait = Wait.TAP
    }

    private val typeTick = object : Runnable {
        override fun run() {
            if (!typing) return
            typed++
            if (typed >= fullText.length) {
                finishTyping()
            } else {
                dialogText.text = typedText()
                handler.postDelayed(this, TYPE_DELAY_MS)
            }
        }
    }

    /** Teks penuh, sisa huruf dibuat transparan agar susunan baris tidak meloncat. */
    private fun typedText(): CharSequence {
        val s = SpannableString(fullText)
        if (typed < s.length) {
            s.setSpan(ForegroundColorSpan(Color.TRANSPARENT), typed, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return s
    }

    private fun finishTyping() {
        typing = false
        handler.removeCallbacks(typeTick)
        typed = fullText.length
        dialogText.text = fullText
        setIndicator(true)
    }

    private fun applyDialogStyle(narration: Boolean) {
        if (memoryMode) {
            dialogBox.setBackgroundResource(R.drawable.dialog_dark)
            nameTag.setBackgroundResource(R.drawable.name_tag_dark)
            nameTag.setTextColor(0xFFF3E3BF.toInt())
            dialogText.setTextColor(0xFFF3E9D6.toInt())
            dialogText.typeface = if (narration) serifItalic else Typeface.SERIF
        } else {
            dialogBox.setBackgroundResource(R.drawable.dialog_light)
            nameTag.setBackgroundResource(R.drawable.name_tag_light)
            nameTag.setTextColor(0xFF1E1E22.toInt())
            dialogText.setTextColor(if (narration) 0xFF4A4A54.toInt() else 0xFF2A2A2E.toInt())
            dialogText.typeface = if (narration) sansItalic else Typeface.DEFAULT
        }
    }

    private fun setIndicator(show: Boolean) {
        indicatorAnim?.cancel()
        nextIndicator.translationY = 0f
        nextIndicator.visibility = if (show) View.VISIBLE else View.INVISIBLE
        if (!show) return
        val d = resources.displayMetrics.density
        indicatorAnim = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 700
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { nextIndicator.translationY = (it.animatedValue as Float) * 4 * d }
            start()
        }
    }

    private fun hideDialog(done: (() -> Unit)? = null) {
        if (dialog.alpha <= 0f) {
            done?.invoke()
            return
        }
        dialog.animate().cancel()
        dialog.animate().alpha(0f).setDuration(220).withEndAction { done?.invoke() }.start()
    }

    // ================================================================== HP

    private fun setPhoneIconVisible(visible: Boolean) {
        if (visible && phoneIcon.visibility != View.VISIBLE) {
            phoneIcon.visibility = View.VISIBLE
            phoneIcon.alpha = 0f
            phoneIcon.animate().alpha(1f).setDuration(500).start()
        } else if (!visible) {
            phoneIcon.visibility = View.GONE
        }
    }

    private fun openPhone() {
        if (wait == Wait.BUSY && !phoneIcon.ringing) return
        phoneIcon.setRinging(false)
        phoneHint.animate().cancel()
        phoneHint.alpha = 0f
        phone.open()
    }

    private fun waitForPhone() {
        wait = Wait.PHONE
        hideDialog()
        phoneHint.alpha = 0f
        phoneHint.animate().alpha(1f).setStartDelay(400).setDuration(400).start()
    }

    private fun onPhoneClosed() {
        enterImmersiveMode()
        if (wait == Wait.PHONE) {
            wait = Wait.BUSY
            handler.postDelayed({ next() }, 350)
        }
    }

    // ================================================================== akhir

    private fun endStory(text: String) {
        wait = Wait.BUSY
        hideDialog()
        phoneIcon.setRinging(false)
        fadeBlack(1f, 1800) {
            showTimecard(text) { leaveToMenu() }
        }
    }
}

/** Jalankan aksi saat animator selesai. */
private fun ValueAnimator.withEnd(action: () -> Unit) {
    addListener(object : android.animation.AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: android.animation.Animator) = action()
    })
}
