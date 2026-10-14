package com.valeriusgames.threadsofdestiny.game

import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.valeriusgames.threadsofdestiny.FantasyButton
import com.valeriusgames.threadsofdestiny.R
import com.valeriusgames.threadsofdestiny.SparkleView
import com.valeriusgames.threadsofdestiny.audio.AudioPlayer
import com.valeriusgames.threadsofdestiny.enterImmersiveMode
import com.valeriusgames.threadsofdestiny.save.SaveData
import com.valeriusgames.threadsofdestiny.save.SaveLoadActivity
import com.valeriusgames.threadsofdestiny.save.SaveStore
import com.valeriusgames.threadsofdestiny.settings.GameSettings
import com.valeriusgames.threadsofdestiny.settings.SettingsActivity
import com.valeriusgames.threadsofdestiny.story.StoryScript
import com.valeriusgames.threadsofdestiny.story.StoryScript.Cmd
import com.valeriusgames.threadsofdestiny.story.StoryScript.Motion

/**
 * Layar permainan visual novel.
 * Urutan: isi nama → jalankan naskah assets/naskah/prolog.txt
 * Bisa juga dibuka langsung dari data simpanan (EXTRA_LOAD_SLOT).
 */
class GameActivity : Activity() {

    companion object {
        /** File naskah pertama yang dijalankan. */
        const val FIRST_SCRIPT = "naskah/prolog.txt"

        /** Buka game langsung dari slot simpanan ini. */
        const val EXTRA_LOAD_SLOT = "load_slot"

        const val PREFS = "player"
        const val KEY_NAME = "name"
        const val KEY_GENDER = "gender"
        const val GENDER_MALE = "laki-laki"
        const val GENDER_FEMALE = "perempuan"

        private const val REQ_SAVE = 1
        private const val REQ_LOAD = 2
        private const val LOG_LIMIT = 80
    }

    private enum class Wait { BUSY, TAP, PHONE, CHOICE }

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var bg: BackgroundLayer
    private lateinit var vignette: View
    private lateinit var clock: TextView
    private lateinit var burger: View
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
    private lateinit var gameMenu: View
    private lateinit var logOverlay: View
    private lateinit var nameEntry: View
    private lateinit var nameInput: EditText
    private lateinit var nameError: View
    private lateinit var nameSparkles: SparkleView
    private lateinit var choiceList: LinearLayout

    private lateinit var audio: AudioPlayer
    private var settings = GameSettings.Values()

    // ---- keadaan cerita (ikut disimpan) ----
    private var scriptPath = FIRST_SCRIPT
    private var script: List<Cmd> = emptyList()
    private var labels: Map<String, Int> = emptyMap()
    /** Variabel cerita: nama, kelamin, usia, jalur, dll. Bisa dipakai di teks sebagai {nama_variabel}. */
    private val vars = mutableMapOf<String, String>()
    private var playerGender = ""
    private var pc = 0
    private var currentIndex = 0
    private var playerName = ""
    private var memoryMode = false
    private var bgName: String? = null
    private var bgMotion = Motion.NONE
    private var clockText: String? = null
    private val messages = mutableListOf<Pair<String, String>>()
    private val log = ArrayDeque<Pair<String?, String>>()

    private var storyStarted = false
    private var wait = Wait.BUSY
    private var currentSpeaker: String? = null
    private var narrationLine = true

    private var fullText: CharSequence = ""
    private var typed = 0
    private var typing = false
    private var indicatorAnim: ValueAnimator? = null

    // ================================================================== lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        enterImmersiveMode()

        bg = BackgroundLayer(findViewById<FrameLayout>(R.id.bg_container))
        vignette = findViewById(R.id.vignette)
        clock = findViewById(R.id.clock)
        burger = findViewById(R.id.btn_burger)
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
        gameMenu = findViewById(R.id.game_menu)
        logOverlay = findViewById(R.id.log_overlay)
        nameEntry = findViewById(R.id.name_entry)
        nameInput = findViewById(R.id.name_input)
        nameError = findViewById(R.id.name_error)
        nameSparkles = findViewById(R.id.name_sparkles)
        choiceList = findViewById(R.id.choice_list)
        phone = PhoneScreen(findViewById(R.id.phone_overlay)) { onPhoneClosed() }

        audio = AudioPlayer(this)
        applySettings()

        findViewById<View>(R.id.game_root).setOnClickListener { onScreenTap() }
        phoneIcon.setOnClickListener { openPhone() }
        setupGameMenu()

        // Data simpanan yang mau dibuka (dari Load / Continue, atau Android sempat menutup game)
        val slot = when {
            intent.hasExtra(EXTRA_LOAD_SLOT) -> intent.getIntExtra(EXTRA_LOAD_SLOT, -1)
            savedInstanceState != null -> SaveStore.AUTO_SLOT
            else -> -1
        }
        val save = if (slot >= 0) SaveStore.read(this, slot) else null

        if (save != null) {
            nameEntry.visibility = View.GONE
            restore(save)
        } else {
            if (slot >= 0 && intent.hasExtra(EXTRA_LOAD_SLOT)) {
                Toast.makeText(this, R.string.load_failed, Toast.LENGTH_SHORT).show()
            }
            loadScript(FIRST_SCRIPT)
            setupNameEntry()
        }
    }

    override fun onResume() {
        super.onResume()
        enterImmersiveMode()
        applySettings()
        audio.resume()
        if (nameEntry.visibility == View.VISIBLE) nameSparkles.start()
    }

    override fun onPause() {
        super.onPause()
        nameSparkles.stop()
        audio.pause()
        autoSave()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersiveMode()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        indicatorAnim?.cancel()
        bg.release()
        audio.release()
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            logOverlay.visibility == View.VISIBLE -> closeLog()
            gameMenu.visibility == View.VISIBLE -> closeGameMenu()
            phone.isOpen -> phone.close()
            nameEntry.visibility == View.VISIBLE -> leaveToMenu()
            else -> confirmLeave()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_LOAD && resultCode == RESULT_OK && data != null) {
            val slot = data.getIntExtra(SaveLoadActivity.EXTRA_SLOT, -1)
            if (slot >= 0) {
                // Muat ulang layar permainan dari slot yang dipilih
                startActivity(Intent(this, GameActivity::class.java).putExtra(EXTRA_LOAD_SLOT, slot))
                finish()
                @Suppress("DEPRECATION")
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    private fun confirmLeave() {
        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(R.string.quit_title)
            .setMessage(R.string.quit_message)
            .setPositiveButton(R.string.quit_yes) { _, _ -> leaveToMenu() }
            .setNegativeButton(R.string.quit_no, null)
            .setOnDismissListener { enterImmersiveMode() }
            .show()
    }

    private fun leaveToMenu() {
        autoSave()
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    // ================================================================== pengaturan

    private fun applySettings() {
        settings = GameSettings.load(this)
        audio.setVolumes(settings.musicVolume, settings.sfxVolume)
        dialogText.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.fontSizeSp)
        applyDialogStyle()
    }

    private fun useDarkBox(): Boolean = when (settings.theme) {
        GameSettings.Theme.AUTO -> memoryMode
        GameSettings.Theme.LIGHT -> false
        GameSettings.Theme.DARK -> true
    }

    private fun applyDialogStyle() {
        val dark = useDarkBox()
        val narration = narrationLine
        dialogBox.setBackgroundResource(if (dark) R.drawable.dialog_dark else R.drawable.dialog_light)
        dialogBox.background.mutate().alpha = (settings.boxOpacity * 255 / 100).coerceIn(40, 255)
        nameTag.setBackgroundResource(if (dark) R.drawable.name_tag_dark else R.drawable.name_tag_light)
        nameTag.setTextColor(if (dark) 0xFFF3E3BF.toInt() else 0xFF1E1E22.toInt())
        dialogText.setTextColor(
            when {
                dark -> 0xFFF3E9D6.toInt()
                narration -> 0xFF4A4A54.toInt()
                else -> 0xFF2A2A2E.toInt()
            }
        )
        // Kenangan memakai huruf klasik agar terasa seperti cerita lama
        val style = if (memoryMode && settings.fontStyle == GameSettings.FontStyle.MODERN)
            GameSettings.FontStyle.CLASSIC else settings.fontStyle
        dialogText.typeface = GameSettings.typeface(this, style, italic = narration)
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

        playerGender = prefs.getString(KEY_GENDER, "") ?: ""
        findViewById<View>(R.id.gender_male).setOnClickListener { selectGender(GENDER_MALE) }
        findViewById<View>(R.id.gender_female).setOnClickListener { selectGender(GENDER_FEMALE) }
        refreshGenderButtons()

        val card = findViewById<View>(R.id.name_card)
        card.alpha = 0f
        card.translationY = 30 * resources.displayMetrics.density
        card.animate().alpha(1f).translationY(0f).setStartDelay(250).setDuration(700)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private fun selectGender(g: String) {
        playerGender = g
        refreshGenderButtons()
        if ((nameError as TextView).text == getString(R.string.gender_error)) nameError.visibility = View.INVISIBLE
    }

    private fun refreshGenderButtons() {
        val d = resources.displayMetrics.density
        listOf(R.id.gender_male to GENDER_MALE, R.id.gender_female to GENDER_FEMALE).forEach { (id, g) ->
            val on = playerGender == g
            findViewById<TextView>(id).apply {
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 14 * d
                    if (on) {
                        orientation = android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
                        colors = intArrayOf(0xFFF8E3A8.toInt(), 0xFFD2AE62.toInt())
                    } else {
                        setColor(0xCC0A1029.toInt())
                        setStroke((1.5f * d).toInt(), 0x99D2AE62.toInt())
                    }
                }
                setTextColor(if (on) 0xFF1A1206.toInt() else 0xFFF6E7C8.toInt())
            }
        }
    }

    private fun confirmName() {
        val name = nameInput.text.toString().trim().replace(Regex("\\s+"), " ")
        if (name.isEmpty() || playerGender.isEmpty()) {
            (nameError as TextView).setText(if (name.isEmpty()) R.string.name_error else R.string.gender_error)
            nameError.visibility = View.VISIBLE
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
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_GENDER, playerGender)
            .apply()
        vars.clear()

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

    private fun loadScript(path: String) {
        scriptPath = path
        try {
            val parsed = assets.open(path).bufferedReader().use { StoryScript.parse(it.readLines()) }
            script = parsed.cmds
            labels = parsed.labels
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.script_error, e.message), Toast.LENGTH_LONG).show()
            script = emptyList()
            labels = emptyMap()
        }
    }

    private fun startScript() {
        storyStarted = true
        pc = 0
        wait = Wait.BUSY
        next()
    }

    /** Jalankan perintah naskah berikutnya sampai ada yang perlu ditunggu. */
    private fun next() {
        while (pc < script.size) {
            currentIndex = pc
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
                is Cmd.PhoneMessage -> receiveMessage(fill(c.sender), fill(c.text))
                is Cmd.WaitForPhone -> { waitForPhone(); return }
                is Cmd.Music -> if (c.name == null) audio.stopMusic() else audio.playMusic(c.name)
                is Cmd.Sound -> audio.playSound(c.name)
                is Cmd.Choice -> { showChoices(c); return }
                is Cmd.Jump -> pc = labels[c.label] ?: pc
                is Cmd.JumpIf -> if (check(c)) pc = labels[c.label] ?: pc
                is Cmd.SetVar -> vars[c.variable] = fill(c.value)
                is Cmd.AddVar -> {
                    val now = vars[c.variable]?.toLongOrNull() ?: 0L
                    vars[c.variable] = (now + c.amount).toString()
                }
                is Cmd.End -> { endStory(c.text); return }
            }
        }
        endStory("Bersambung…")
    }

    /**
     * Isi teks: {nama} → nama pemain, {variabel} → nilai variabel,
     * {teks laki-laki|teks perempuan} → sesuai jenis kelamin pemain.
     */
    private fun fill(s: String): String {
        if (!s.contains('{')) return s
        vars["nama"] = playerName
        vars["kelamin"] = playerGender
        return Regex("\\{([^{}]*)\\}").replace(s) { m ->
            val body = m.groupValues[1]
            if (body.contains('|')) {
                if (playerGender == GENDER_FEMALE) body.substringAfter('|') else body.substringBefore('|')
            } else {
                vars[body] ?: (if (body == "name") playerName else m.value)
            }
        }
    }

    private fun check(c: Cmd.JumpIf): Boolean {
        val left = vars[c.variable] ?: ""
        val right = fill(c.value)
        val ln = left.toDoubleOrNull()
        val rn = right.toDoubleOrNull()
        if (ln != null && rn != null) {
            return when (c.op) {
                "==" -> ln == rn
                "!=" -> ln != rn
                ">=" -> ln >= rn
                "<=" -> ln <= rn
                ">" -> ln > rn
                else -> ln < rn
            }
        }
        return when (c.op) {
            "==" -> left.equals(right, ignoreCase = true)
            "!=" -> !left.equals(right, ignoreCase = true)
            else -> false
        }
    }

    private fun onScreenTap() {
        if (wait != Wait.TAP) return
        if (typing) {
            finishTyping()
        } else {
            wait = Wait.BUSY
            next()
        }
    }

    // ================================================================== simpan & muat

    private fun snapshot(): SaveData? {
        if (!storyStarted || wait == Wait.BUSY) return null
        val preview = when {
            wait == Wait.PHONE -> "📱 ${messages.lastOrNull()?.first.orEmpty()}"
            wait == Wait.CHOICE -> "Memilih: " + fill(fullText.toString())
            currentSpeaker != null -> "$currentSpeaker: $fullText"
            else -> fullText.toString()
        }
        return SaveData(
            script = scriptPath,
            index = currentIndex,
            playerName = playerName,
            bg = bgName,
            memory = memoryMode,
            motion = bgMotion.name,
            clock = clockText,
            music = audio.musicName,
            phoneIcon = phoneIcon.visibility == View.VISIBLE,
            ringing = phoneIcon.ringing,
            messages = messages.toList(),
            log = log.toList(),
            preview = preview,
            savedAt = System.currentTimeMillis(),
            vars = vars.toMap() + mapOf("nama" to playerName, "kelamin" to playerGender),
        )
    }

    private fun autoSave() {
        snapshot()?.let { SaveStore.write(this, SaveStore.AUTO_SLOT, it) }
    }

    /** Pulihkan layar persis seperti saat disimpan, lalu lanjutkan dari kalimat itu. */
    private fun restore(d: SaveData) {
        playerName = d.playerName
        vars.clear()
        vars.putAll(d.vars)
        playerGender = d.vars["kelamin"]
            ?: getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_GENDER, GENDER_MALE)
            ?: GENDER_MALE
        loadScript(d.script)
        storyStarted = true
        wait = Wait.BUSY
        black.alpha = 1f

        memoryMode = d.memory
        bgName = d.bg
        bgMotion = runCatching { Motion.valueOf(d.motion) }.getOrDefault(Motion.NONE)
        if (d.bg != null) {
            val motion = if (bgMotion == Motion.PAN) Motion.PAN else Motion.NONE
            bg.show(loadBackground(d.bg), d.memory, motion, instant = true) {}
        }
        vignette.alpha = if (d.memory) 1f else 0f
        setClock(d.clock)
        d.music?.let { audio.playMusic(it) }

        if (d.phoneIcon) setPhoneIconVisible(true)
        d.messages.forEach { (from, text) ->
            messages.add(from to text)
            phone.addMessage(from, text)
        }
        if (d.ringing) phoneIcon.ring(true)

        log.clear()
        log.addAll(d.log)
        updateBurger()
        applyDialogStyle()

        pc = d.index.coerceIn(0, script.size)
        // Kalimat yang disimpan akan tampil lagi, jangan sampai dobel di riwayat
        if (log.isNotEmpty() && script.getOrNull(pc) is Cmd.Line) log.removeLast()
        // Saat menyimpan di tengah pilihan: tampilkan lagi kalimat terakhir sebagai pertanyaan
        val lastLine = log.lastOrNull()
        val resumeChoice = script.getOrNull(pc) is Cmd.Choice && lastLine != null
        handler.postDelayed({
            fadeBlack(0f, 900) {
                if (resumeChoice && lastLine != null) showLine(lastLine.first, lastLine.second, record = false, instant = true)
                next()
            }
        }, 300)
    }

    // ================================================================== background & efek

    private fun changeBackground(c: Cmd.Background, done: () -> Unit) {
        val instant = black.alpha >= 0.99f
        memoryMode = c.memory
        bgName = c.name
        bgMotion = c.motion
        updateBurger()
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

        fun step(i: Int) {
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
                withEnd { step(i + 1) }
                start()
            }
        }
        step(0)
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

    private fun showLine(speaker: String?, raw: String, record: Boolean = true, instant: Boolean = false) {
        val text = fill(raw)
        val who = speaker?.let { fill(it) }
        narrationLine = who == null
        currentSpeaker = who
        applyDialogStyle()

        if (who == null) {
            nameTag.visibility = View.INVISIBLE
        } else {
            nameTag.visibility = View.VISIBLE
            nameTag.text = who
        }

        if (dialog.alpha < 1f) {
            dialog.animate().cancel()
            dialog.translationY = 12 * resources.displayMetrics.density
            dialog.animate().alpha(1f).translationY(0f).setDuration(260).start()
        }

        if (record) {
            log.addLast(who to text)
            while (log.size > LOG_LIMIT) log.removeFirst()
        }

        fullText = text
        typed = 0
        wait = Wait.TAP
        handler.removeCallbacks(typeTick)
        if (instant || settings.typeDelayMs <= 0L) {
            finishTyping()
            return
        }
        typing = true
        setIndicator(false)
        dialogText.text = typedText()
        handler.postDelayed(typeTick, settings.typeDelayMs)
    }

    private val typeTick = object : Runnable {
        override fun run() {
            if (!typing) return
            typed++
            if (typed >= fullText.length) {
                finishTyping()
            } else {
                dialogText.text = typedText()
                handler.postDelayed(this, settings.typeDelayMs.coerceAtLeast(1L))
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

    // ================================================================== menu (burger)

    /** Tombol burger hanya muncul di masa kini (bukan saat adegan kenangan). */
    private fun updateBurger() {
        val show = storyStarted && !memoryMode
        if (show && burger.visibility != View.VISIBLE) {
            burger.visibility = View.VISIBLE
            burger.alpha = 0f
            burger.animate().alpha(1f).setDuration(500).start()
        } else if (!show && burger.visibility == View.VISIBLE) {
            burger.animate().alpha(0f).setDuration(300).withEndAction {
                if (memoryMode) burger.visibility = View.GONE
            }.start()
        }
    }

    private fun setupGameMenu() {
        burger.setOnClickListener { openGameMenu() }
        gameMenu.setOnClickListener { closeGameMenu() }
        findViewById<View>(R.id.menu_resume).setOnClickListener { closeGameMenu() }
        findViewById<View>(R.id.menu_save).setOnClickListener {
            val snap = snapshot()
            if (snap == null) {
                Toast.makeText(this, R.string.save_wait, Toast.LENGTH_SHORT).show()
            } else {
                closeGameMenu()
                openSaveLoad(SaveLoadActivity.MODE_SAVE, snap)
            }
        }
        findViewById<View>(R.id.menu_load).setOnClickListener {
            closeGameMenu()
            openSaveLoad(SaveLoadActivity.MODE_LOAD, null)
        }
        findViewById<View>(R.id.menu_log).setOnClickListener {
            closeGameMenu()
            openLog()
        }
        findViewById<View>(R.id.menu_settings).setOnClickListener {
            closeGameMenu()
            startActivity(Intent(this, SettingsActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        findViewById<View>(R.id.menu_main).setOnClickListener {
            closeGameMenu()
            confirmLeave()
        }
        findViewById<View>(R.id.log_close).setOnClickListener { closeLog() }
    }

    private fun openGameMenu() {
        if (gameMenu.visibility == View.VISIBLE) return
        gameMenu.visibility = View.VISIBLE
        gameMenu.alpha = 0f
        gameMenu.animate().alpha(1f).setDuration(200).start()
        val card = findViewById<View>(R.id.game_menu_card)
        card.scaleX = 0.92f
        card.scaleY = 0.92f
        card.animate().scaleX(1f).scaleY(1f).setDuration(260)
            .setInterpolator(DecelerateInterpolator(2f)).start()
    }

    private fun closeGameMenu() {
        if (gameMenu.visibility != View.VISIBLE) return
        gameMenu.animate().alpha(0f).setDuration(180).withEndAction {
            gameMenu.visibility = View.GONE
        }.start()
    }

    private fun openSaveLoad(mode: String, snap: SaveData?) {
        val i = Intent(this, SaveLoadActivity::class.java)
            .putExtra(SaveLoadActivity.EXTRA_MODE, mode)
        snap?.let { i.putExtra(SaveLoadActivity.EXTRA_STATE, it.toJson()) }
        @Suppress("DEPRECATION")
        startActivityForResult(i, if (mode == SaveLoadActivity.MODE_SAVE) REQ_SAVE else REQ_LOAD)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun openLog() {
        val list = findViewById<LinearLayout>(R.id.log_list)
        list.removeAllViews()
        val d = resources.displayMetrics.density
        if (log.isEmpty()) {
            list.addView(TextView(this).apply {
                setText(R.string.log_empty)
                setTextColor(0xB3FFFFFF.toInt())
                textSize = 15f
            })
        }
        log.forEach { (who, text) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.log_item)
                setPadding((14 * d).toInt(), (10 * d).toInt(), (14 * d).toInt(), (12 * d).toInt())
            }
            if (who != null) {
                item.addView(TextView(this).apply {
                    this.text = who
                    setTextColor(0xFFF2D58C.toInt())
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                })
            }
            item.addView(TextView(this).apply {
                this.text = text
                setTextColor(if (who == null) 0xFFD9D2C3.toInt() else 0xFFF6F1E6.toInt())
                textSize = 16f
                typeface = GameSettings.typeface(this@GameActivity, settings.fontStyle, italic = who == null)
                setLineSpacing(0f, 1.15f)
            })
            list.addView(item, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (8 * d).toInt() })
        }
        logOverlay.visibility = View.VISIBLE
        logOverlay.alpha = 0f
        logOverlay.animate().alpha(1f).setDuration(220).start()
        val scroll = findViewById<ScrollView>(R.id.log_scroll)
        scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun closeLog() {
        logOverlay.animate().alpha(0f).setDuration(180).withEndAction {
            logOverlay.visibility = View.GONE
        }.start()
    }

    // ================================================================== pilihan

    private fun showChoices(c: Cmd.Choice) {
        wait = Wait.CHOICE
        if (typing) finishTyping()
        setIndicator(false)
        choiceList.removeAllViews()
        val d = resources.displayMetrics.density
        c.options.forEachIndexed { i, opt ->
            val item = TextView(this).apply {
                text = "${i + 1}. ${fill(opt.text)}"
                setBackgroundResource(R.drawable.choice_item)
                setTextColor(0xFF22222A.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, (settings.fontSizeSp - 1f).coerceAtLeast(14f))
                typeface = GameSettings.typeface(this@GameActivity, settings.fontStyle, false)
                minHeight = (50 * d).toInt()
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding((18 * d).toInt(), (10 * d).toInt(), (18 * d).toInt(), (10 * d).toInt())
                elevation = 3 * d
                alpha = 0f
                translationY = 16 * d
                setOnClickListener { pickChoice(c, i, this) }
            }
            choiceList.addView(item, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { if (i > 0) topMargin = (10 * d).toInt() })
            item.animate().alpha(1f).translationY(0f)
                .setStartDelay(120L + i * 90L).setDuration(320)
                .setInterpolator(DecelerateInterpolator()).start()
        }
        choiceList.visibility = View.VISIBLE
        // Geser kotak dialog ke atas bila tertimpa daftar pilihan
        choiceList.post {
            val overlap = dialog.bottom + (10 * d) - choiceList.top
            dialog.animate().translationY(if (overlap > 0) -overlap else 0f).setDuration(250).start()
        }
    }

    private fun pickChoice(c: Cmd.Choice, index: Int, view: View) {
        if (wait != Wait.CHOICE) return
        wait = Wait.BUSY
        val opt = c.options[index]
        log.addLast("➤ Pilihanmu" to fill(opt.text))
        while (log.size > LOG_LIMIT) log.removeFirst()

        view.animate().scaleX(1.03f).scaleY(1.03f).setDuration(120).withEndAction {
            for (i in 0 until choiceList.childCount) {
                choiceList.getChildAt(i).animate().alpha(0f).setDuration(220).start()
            }
            dialog.animate().translationY(0f).setDuration(250).start()
            handler.postDelayed({
                choiceList.visibility = View.GONE
                choiceList.removeAllViews()
                pc = labels[opt.label] ?: pc
                next()
            }, 260)
        }.start()
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

    private fun receiveMessage(sender: String, text: String) {
        setPhoneIconVisible(true)
        messages.add(sender to text)
        phone.addMessage(sender, text)
        phoneIcon.ring(true)
        audio.playSound("notifikasi")
        vibrate()
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        if (!settings.vibration) return
        try {
            val v: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 140, 110, 140), -1))
        } catch (_: Exception) {
        }
    }

    private fun openPhone() {
        if (wait == Wait.BUSY && !phoneIcon.ringing) return
        phoneIcon.ring(false)
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
        phoneIcon.ring(false)
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
