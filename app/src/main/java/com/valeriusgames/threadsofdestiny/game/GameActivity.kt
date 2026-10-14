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
import com.valeriusgames.threadsofdestiny.life.GameData
import com.valeriusgames.threadsofdestiny.life.LifeHudView
import com.valeriusgames.threadsofdestiny.life.LifeState
import com.valeriusgames.threadsofdestiny.life.PhoneState
import com.valeriusgames.threadsofdestiny.life.SocialState
import com.valeriusgames.threadsofdestiny.life.rupiah
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
        const val FIRST_SCRIPT = "naskah/mulai.txt"

        /** Buka game langsung dari slot simpanan ini. */
        const val EXTRA_LOAD_SLOT = "load_slot"

        const val PREFS = "player"
        const val KEY_NAME = "name"
        const val KEY_GENDER = "gender"
        const val CHAR_BASE_HEIGHT = 0.72f
        const val GENDER_MALE = "laki-laki"
        const val GENDER_FEMALE = "perempuan"

        private const val REQ_SAVE = 1
        private const val REQ_LOAD = 2
        private const val LOG_LIMIT = 80
    }

    private enum class Wait { BUSY, TAP, PHONE, CHOICE, FREE }

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
    private lateinit var lifeHud: LifeHudView
    private lateinit var lifeDrawer: LinearLayout
    private lateinit var lifeTab: TextView
    private var hudOpen = false
    private lateinit var lifeNotice: TextView
    private lateinit var sheet: View
    private lateinit var sheetTitle: TextView
    private lateinit var sheetSub: TextView
    private lateinit var sheetList: LinearLayout
    private lateinit var sheetButtons: LinearLayout
    private lateinit var deathOverlay: View
    private lateinit var charLayer: FrameLayout
    private lateinit var charPanel: View

    // ---- hubungan ----
    private var social = SocialState()
    /** Karakter yang sedang tampil: id → (ekspresi, posisi). */
    private val shownChars = linkedMapOf<String, Pair<String, String>>()
    private var panelCharId: String? = null
    private var meetingTalked = false
    private var meetingGifted = false

    /** Kejadian yang menunggu ditampilkan: "ev:<id>", "ujian:<id_sekolah>", "kelas:<id_sekolah>". */
    private val pendingEvents = ArrayDeque<String>()

    // ---- isi HP ----
    private var phoneData = PhoneState()

    // ---- sistem kehidupan ----
    private var life = LifeState()
    private val gameData by lazy { GameData.get(this) }
    /** Aksi setelah pemain mengetuk kalimat (dipakai waktu bebas), bukan lanjut naskah. */
    private var afterTap: (() -> Unit)? = null
    private var freeLeft = 0
    /** Tombol kembali saat lembar terbuka (null = tidak bisa ditutup dengan kembali). */
    private var sheetBack: (() -> Unit)? = null
    private val noticeQueue = ArrayDeque<String>()
    private var noticeShowing = false

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
        lifeHud = findViewById(R.id.life_hud)
        lifeDrawer = findViewById(R.id.life_drawer)
        lifeTab = findViewById(R.id.life_tab)
        lifeTab.setOnClickListener { setHudOpen(!hudOpen) }
        lifeNotice = findViewById(R.id.life_notice)
        sheet = findViewById(R.id.sheet_overlay)
        sheetTitle = findViewById(R.id.sheet_title)
        sheetSub = findViewById(R.id.sheet_sub)
        sheetList = findViewById(R.id.sheet_list)
        sheetButtons = findViewById(R.id.sheet_buttons)
        deathOverlay = findViewById(R.id.death_overlay)
        charLayer = findViewById(R.id.char_layer)
        charPanel = findViewById(R.id.char_panel)
        findViewById<View>(R.id.char_panel_photo).clipToOutline = true
        lifeHud.setOnClickListener { if (wait != Wait.BUSY) openStatus(returnTo = currentSheetReturn()) }
        sheet.setOnClickListener { sheetBack?.invoke() }
        findViewById<View>(R.id.death_load).setOnClickListener { openSaveLoad(SaveLoadActivity.MODE_LOAD, null) }
        findViewById<View>(R.id.death_menu).setOnClickListener { leaveToMenu() }
        phone = PhoneScreen(findViewById(R.id.phone_overlay)) { onPhoneClosed() }
        phone.onAppOpen = { renderApp(it) }
        phone.onHomeShown = { refreshPhoneHome() }

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
            deathOverlay.visibility == View.VISIBLE -> leaveToMenu()
            logOverlay.visibility == View.VISIBLE -> closeLog()
            gameMenu.visibility != View.VISIBLE && sheet.visibility == View.VISIBLE ->
                sheetBack?.invoke() ?: openGameMenu()
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
        findViewById<View>(R.id.name_back).setOnClickListener {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(nameInput.windowToken, 0)
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

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
        findViewById<android.widget.ImageView>(R.id.gender_preview).apply {
            val bmp = if (playerGender.isEmpty()) null else playerPhoto()
            if (bmp == null) {
                visibility = View.GONE
            } else {
                setImageBitmap(bmp)
                clipToOutline = true
                if (visibility != View.VISIBLE) {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(350).start()
                }
            }
        }
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
        gameData.applyPlayerGender(playerGender)
        vars.clear()

        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(nameInput.windowToken, 0)
        nameInput.clearFocus()

        nameEntry.isClickable = false
        nameEntry.animate().alpha(0f).setDuration(450).withEndAction {
            nameEntry.visibility = View.GONE
            nameSparkles.stop()
            enterImmersiveMode()
            handler.postDelayed({ startScript() }, 120)
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
        syncSocialVars()
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
                is Cmd.Line -> {
                    showLine(c.speaker, c.text)
                    // Kalimat tepat sebelum @pilihan: tombol pilihan langsung muncul bersama pertanyaannya
                    val nextCmd = script.getOrNull(pc)
                    if (nextCmd is Cmd.Choice) {
                        currentIndex = pc
                        pc++
                        showChoices(nextCmd)
                    }
                    return
                }
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
                is Cmd.Life -> {
                    life.active = c.active
                    if (phoneData.docs.isEmpty()) phoneData.docs += gameData.documents.values.filter { it.initial }.map { it.id }
                    refreshLife()
                }
                is Cmd.Note -> {
                    phoneData.notes += PhoneState.Note(life.day, fill(c.text))
                    notice("📝 Catatan baru di HP")
                }
                is Cmd.Doc -> {
                    phoneData.docs += c.id
                    gameData.documents[c.id]?.let { notice("📁 Dokumen didapat: ${it.name}") }
                }
                is Cmd.Calendar -> {
                    val target = LifeState.WEEKDAYS.indexOfFirst { it.equals(c.weekday, true) }
                    if (life.startEpochDay != 0L && target >= 0) {
                        // Loncatan waktu ke hari tersebut (tagihan & kiriman tetap berjalan)
                        var guard = 0
                        while (life.weekdayIndex != target && guard++ < 7) {
                            life.day++
                            onNewDay()
                        }
                    }
                    life.setCalendar(c.weekday, c.slot)
                    refreshLife()
                }
                is Cmd.Free -> { startFree(c.count); return }
                is Cmd.Advance -> {
                    if (c.minutes > 0) passTime(c.minutes, fromStory = true)
                    else repeat(c.count) { if (!life.dead) advancePeriod() }
                    if (life.dead) return
                }
                is Cmd.Money -> {
                    life.money = (life.money + c.delta).coerceAtLeast(0)
                    logMoney(if (c.delta >= 0) "Pemasukan" else "Pengeluaran", c.delta)
                    notice((if (c.delta >= 0) "💰 +" else "💸 -") + rupiah(kotlin.math.abs(c.delta)))
                    refreshLife()
                }
                is Cmd.Stat -> {
                    LifeState.Stat.of(c.stat)?.let { life.change(it, c.delta) }
                    refreshLife()
                    if (checkDeath()) return
                }
                is Cmd.GiveItem -> {
                    val def = gameData.items[c.id]
                    if (def != null) {
                        repeat(c.count) { life.addItem(def.id, def.shelfDays) }
                        notice("${def.icon} ${def.name}" + (if (c.count > 1) " ×${c.count}" else "") + " masuk ke tas")
                    }
                }
                is Cmd.Location -> vars["lokasi"] = c.place
                is Cmd.LoadScript -> {
                    loadScript("naskah/${c.file}")
                    pc = 0
                }
                is Cmd.SetDate -> {
                    LifeState.parseDate(c.text)?.let { life.startEpochDay = it - life.day }
                    refreshLife()
                }
                is Cmd.Kos -> moveIn(c.id, fromStory = true)
                is Cmd.Allowance -> {
                    life.allowance = c.amount
                    life.allowanceNextDay = if (c.amount > 0) life.day + 30 else -1
                    if (c.amount > 0) notice("💌 Keluarga akan mengirim ${rupiah(c.amount)} setiap 30 hari")
                }
                is Cmd.JobStart -> startJob(c.id)
                is Cmd.JobStop -> stopJob(c.id)
                is Cmd.Apply -> {
                    val ok = applyJob(c.id, announceResult = false)
                    pc = labels[if (ok) c.okLabel else c.noLabel] ?: pc
                }
                is Cmd.ShowChar -> showCharacter(c.id, c.expression, c.position)
                is Cmd.HideChar -> hideCharacter(c.id)
                is Cmd.Meet -> {
                    gameData.characters[c.id]?.let { social.rel(it).met = true }
                    syncSocialVars()
                }
                is Cmd.Love -> announce(social.changeLove(gameData, c.id, c.delta))
                is Cmd.Trust -> {
                    social.changeTrust(gameData, c.id, c.delta)
                    gameData.characters[c.id]?.let {
                        notice((if (c.delta >= 0) "🤝 " else "⚠️ ") + "${it.name}: kepercayaan " + (if (c.delta >= 0) "+" else "") + c.delta)
                    }
                    syncSocialVars()
                }
                is Cmd.Behave -> announce(social.behave(gameData, c.id, c.kind))
                is Cmd.Lie -> {
                    social.lies[c.code] = c.ids.toMutableList()
                    vars["bohong_${c.code}"] = "1"
                }
                is Cmd.Reveal -> revealLie(c.code)
                is Cmd.Confess -> {
                    val def = gameData.characters[c.id]
                    val ok = def != null && social.confess(def, playerAge(), playerGender)
                    syncSocialVars()
                    pc = labels[if (ok) c.okLabel else c.noLabel] ?: pc
                }
                is Cmd.Event -> {
                    val ev = gameData.events.firstOrNull { it.id == c.id }
                    if (ev != null) {
                        runEvent(ev) { next() }
                        return
                    }
                }
                is Cmd.End -> { endStory(c.text); return }
                is Cmd.ResetState -> resetForStory()
                is Cmd.Info -> gameData.characters[c.id]?.let { ch ->
                    val keys = if (c.key == "semua") facts.map { it.key } else listOf(c.key)
                    keys.filter { ch.text[it] != null }.forEach { learnFact(ch, it) }
                    notice("📖 Info baru tentang ${ch.name}")
                }
                is Cmd.Show -> { showTour(c); return }
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
            val a = afterTap
            afterTap = null
            if (a != null) a() else next()
        }
    }

    // ================================================================== simpan & muat

    private fun snapshot(): SaveData? {
        if (!storyStarted || wait == Wait.BUSY || life.dead) return null
        val preview = when {
            wait == Wait.PHONE -> "📱 ${messages.lastOrNull()?.first.orEmpty()}"
            wait == Wait.CHOICE -> "Memilih: " + fill(fullText.toString())
            wait == Wait.FREE || vars.containsKey("_bebas_sisa") -> "Waktu bebas · ${life.clockText}"
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
            life = life.toJson(),
            social = social.toJson(),
            phone = phoneData.toJson(),
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
        gameData.applyPlayerGender(playerGender)
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
        life = LifeState.fromJson(d.life)
        social = SocialState.fromJson(d.social)
        phoneData = PhoneState.fromJson(d.phone)
        syncSocialVars()
        restoreShownCharacters()
        refreshLife()
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
        val speakerDef = who?.let { w -> gameData.findCharacter(w) ?: speakerIdFromRaw(speaker) }
        updateCharPanel(speakerDef)
        highlightSpeaker(speakerDef?.id)

        // Selalu batalkan animasi sebelumnya: perintah "sembunyikan dialog" yang baru dijadwalkan
        // (alpha masih 1) bisa menghapus kalimat ini kalau tidak dibatalkan.
        dialog.animate().cancel()
        if (dialog.alpha < 1f) {
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
        refreshLife()
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
        findViewById<View>(R.id.menu_chars).setOnClickListener {
            closeGameMenu()
            openCharacterList(currentSheetReturn())
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
        gameMenu.bringToFront()
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
        logOverlay.bringToFront()
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
        showButtons(c.options.map { fill(it.text) }) { i ->
            log.addLast("➤ Pilihanmu" to fill(c.options[i].text))
            while (log.size > LOG_LIMIT) log.removeFirst()
            pc = labels[c.options[i].label] ?: pc
            next()
        }
    }

    /** Tampilkan tombol-tombol pilihan bernomor di bawah kotak dialog. */
    private fun showButtons(options: List<String>, onPick: (Int) -> Unit) {
        wait = Wait.CHOICE
        if (typing) finishTyping()
        setIndicator(false)
        choiceList.bringToFront()
        choiceList.removeAllViews()
        val d = resources.displayMetrics.density
        options.forEachIndexed { i, label ->
            val item = TextView(this).apply {
                text = label
                setBackgroundResource(R.drawable.choice_item)
                setTextColor(0xFF3B2A12.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, (settings.fontSizeSp - 1f).coerceAtLeast(14f))
                typeface = android.graphics.Typeface.create(
                    GameSettings.typeface(this@GameActivity, settings.fontStyle, false), android.graphics.Typeface.BOLD)
                minHeight = (50 * d).toInt()
                gravity = android.view.Gravity.CENTER
                setPadding((18 * d).toInt(), (10 * d).toInt(), (18 * d).toInt(), (10 * d).toInt())
                elevation = 3 * d
                alpha = 0f
                translationY = 16 * d
                setOnClickListener { pickButton(i, this, onPick) }
            }
            choiceList.addView(item, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { if (i > 0) topMargin = (10 * d).toInt() })
            item.animate().alpha(1f).translationY(0f)
                .setStartDelay(120L + i * 90L).setDuration(320)
                .setInterpolator(DecelerateInterpolator()).start()
        }
        // Posisi: di tengah layar bila layar masih gelap (tanpa latar), selain itu di bawah.
        // Margin kiri-kanan sama agar tombol tepat di tengah; naik sedikit bila ikon HP tampil.
        val centered = bgName == null
        (choiceList.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = if (centered) android.view.Gravity.CENTER_VERTICAL else android.view.Gravity.BOTTOM
            marginStart = (28 * d).toInt()
            marginEnd = (28 * d).toInt()
            bottomMargin = ((if (!centered && phoneIcon.visibility == View.VISIBLE) 136 else 22) * d).toInt()
            choiceList.layoutParams = this
        }
        choiceList.visibility = View.VISIBLE
        // Geser kotak dialog ke atas bila tertimpa daftar pilihan
        if (!centered) choiceList.post {
            val overlap = dialog.bottom + (10 * d) - choiceList.top
            dialog.animate().translationY(if (overlap > 0) -overlap else 0f).setDuration(250).start()
        }
    }

    private fun pickButton(index: Int, view: View, onPick: (Int) -> Unit) {
        if (wait != Wait.CHOICE) return
        wait = Wait.BUSY
        view.animate().scaleX(1.03f).scaleY(1.03f).setDuration(120).withEndAction {
            for (i in 0 until choiceList.childCount) {
                choiceList.getChildAt(i).animate().alpha(0f).setDuration(220).start()
            }
            dialog.animate().translationY(0f).setDuration(250).start()
            handler.postDelayed({
                choiceList.visibility = View.GONE
                choiceList.removeAllViews()
                onPick(index)
            }, 260)
        }.start()
    }

    // ================================================================== kehidupan

    /** Perbarui panel status, jam, dan variabel kehidupan. */
    private fun refreshLife() {
        val show = life.active && storyStarted && !memoryMode
        if (show && lifeDrawer.visibility != View.VISIBLE) {
            lifeDrawer.visibility = View.VISIBLE
            lifeDrawer.alpha = 1f
            lifeDrawer.post { setHudOpen(hudOpen, animate = false) }
        } else if (!show) {
            lifeDrawer.visibility = View.GONE
        }
        lifeHud.bind(life)
        refreshHudTab()
        if (life.active) {
            vars["uang"] = life.money.toString()
            vars["kerja_aktif"] = life.jobs.count { gameData.jobs[it.id]?.kind != "sekolah" }.toString()
            vars["hari"] = life.weekday
            vars["waktu"] = life.slot.label
            vars["jam"] = life.timeText
            vars["jam_menit"] = life.minute.toString()
            vars["hari_ke"] = (life.day + 1).toString()
            life.date()?.let { d ->
                vars["bulan"] = d.monthValue.toString()
                vars["musim"] = if (d.monthValue >= 11 || d.monthValue <= 3) "hujan" else "kemarau"
            }
            if (clockText != life.clockText) setClock(life.clockText)
        }
    }

    /** Buka / tutup laci status di kiri layar. */
    private fun setHudOpen(open: Boolean, animate: Boolean = true) {
        hudOpen = open
        val w = lifeHud.width.takeIf { it > 0 } ?: lifeHud.measuredWidth
        val x = if (open) 0f else -w.toFloat()
        lifeDrawer.animate().cancel()
        lifeDrawer.alpha = 1f   // jangan sampai animasi muncul (alpha) ikut terbatalkan
        if (animate) lifeDrawer.animate().translationX(x).setDuration(260)
            .setInterpolator(DecelerateInterpolator()).start()
        else lifeDrawer.translationX = x
        refreshHudTab()
    }

    private fun refreshHudTab() {
        lifeTab.text = when {
            hudOpen -> "‹"
            lifeHud.critical() -> "⚠️"
            else -> "📊"
        }
        lifeTab.textSize = if (hudOpen) 22f else 17f
        lifeTab.setTextColor(0xFF3A3A40.toInt())
    }

    /** Pemberitahuan singkat di layar, antre satu per satu. */
    private fun notice(text: String) {
        noticeQueue.addLast(text)
        if (!noticeShowing) showNextNotice()
    }

    private fun showNextNotice() {
        val t = noticeQueue.removeFirstOrNull()
        if (t == null) {
            noticeShowing = false
            return
        }
        noticeShowing = true
        lifeNotice.bringToFront()
        lifeNotice.text = t
        lifeNotice.translationY = -10 * resources.displayMetrics.density
        lifeNotice.animate().alpha(1f).translationY(0f).setDuration(220).withEndAction {
            lifeNotice.animate().alpha(0f).setStartDelay(1900).setDuration(300).withEndAction {
                lifeNotice.animate().setStartDelay(0)
                showNextNotice()
            }.start()
        }.start()
    }

    /**
     * Majukan jam sebanyak [minutes] menit. Setiap lewat tengah malam: cek bolos/absen hari itu
     * lalu jalankan hal-hal harian (sewa, kiriman, kejadian...).
     */
    private fun passTime(minutes: Int, sleeping: Boolean = false, fromStory: Boolean = false) {
        if (!life.active || minutes <= 0) return
        var left = minutes
        val warnings = mutableListOf<String>()
        while (left > 0 && !life.dead) {
            val chunk = minOf(left, life.minutesToMidnight)
            val dayBefore = life.day
            warnings += life.pass(chunk, sleeping)
            left -= chunk
            if (life.day != dayBefore) {
                checkMissedJobs(dayBefore, wholeDay = true, excused = fromStory)
                onNewDay()
            }
        }
        checkMissedJobs(life.day, wholeDay = false, excused = fromStory)
        checkMissedAppointments(excused = fromStory)
        refreshLife()
        warnings.distinct().forEach { notice(it) }
        checkDeath()
    }

    /** "@maju 1": loncat ke bagian hari berikutnya. Dari Malam = tidur sampai 06:00. */
    private fun advancePeriod() {
        val s = life.slot
        if (s == LifeState.Slot.MALAM) {
            val target = 6 * 60
            val mins = if (life.minute < 4 * 60) target - life.minute else life.minutesToMidnight + target
            passTime(mins, sleeping = true, fromStory = true)
        } else {
            passTime(s.end - life.minute, fromStory = true)
        }
    }

    private fun checkDeath(): Boolean {
        if (!life.dead) return false
        showDeath()
        return true
    }

    private fun showDeath() {
        wait = Wait.BUSY
        afterTap = null
        handler.removeCallbacksAndMessages(null)
        closeSheet()
        hideDialog()
        choiceList.visibility = View.GONE
        val reason = when (life.deathReason) {
            "dehidrasi" -> "dehidrasi"
            "kelaparan" -> "kelaparan"
            "sakit" -> "sakit yang tak tertangani"
            else -> "kelelahan yang tak tertahankan"
        }
        findViewById<TextView>(R.id.death_text).text =
            "$playerName meninggal karena $reason,\npada hari ke-${life.day + 1}.\n\nHidup ini keras. Tapi selalu ada pilihan lain."
        deathOverlay.bringToFront()
        deathOverlay.visibility = View.VISIBLE
        deathOverlay.alpha = 0f
        deathOverlay.animate().alpha(1f).setDuration(1600).start()
        audio.stopMusic()
    }

    // ---------------- waktu bebas ----------------

    private fun startFree(count: Int) {
        if (!life.active) {
            life.active = true
            refreshLife()
        }
        freeLeft = vars["_bebas_sisa"]?.toIntOrNull() ?: count
        vars["_bebas_sisa"] = freeLeft.toString()
        wait = Wait.FREE
        hideDialog()
        showFreeSheet()
    }

    private fun showFreeSheet() {
        if (freeLeft <= 0) {
            endFree()
            return
        }
        // Kejadian yang menunggu (rintangan, ujian, dll.) tampil dulu
        val pending = pendingEvents.removeFirstOrNull()
        if (pending != null) {
            closeSheet()
            runPending(pending) { showFreeSheet() }
            return
        }
        wait = Wait.FREE
        val place = vars["lokasi"] ?: "desa"
        openSheet(
            getString(R.string.free_title),
            getString(R.string.free_sub, life.clockText, freeLeft),
            back = null,
        )
        // Janji temu di waktu ini
        phoneData.appts.filter { it.day == life.day && it.slot == life.slot.name }.forEach { a ->
            val c = gameData.characters[a.charId] ?: return@forEach
            sheetList.addView(sectionLabel("📅 Janji temu"))
            sheetList.addView(card(
                title = "🤝  Ketemu ${c.name}",
                sub = "Kalian sudah janjian di waktu ini.",
                note = "Tidak datang = ingkar janji",
                noteWarn = true,
            ) { meetCharacter(c) })
        }
        // Jadwal sekolah / kerja di waktu ini
        val duties = life.jobs.mapNotNull { j -> gameData.jobs[j.id] }
            .filter { gameData.canAttendNow(it, life.weekdayIndex, life.minute) && !life.hasAttended(it.id) }
        if (duties.isNotEmpty()) {
            sheetList.addView(sectionLabel("📌 Jadwalmu sekarang"))
            duties.forEach { def ->
                val needEnergy = -(def.effects["energi"] ?: 0L)
                val tooTired = needEnergy > 0 && life[LifeState.Stat.ENERGI] < needEnergy / 2
                val lateBy = life.minute - def.startMin
                sheetList.addView(card(
                    title = "${def.icon}  ${def.name}",
                    sub = "🕐 ${def.hoursText}" + (if (lateBy > 15) "  ·  ⏰ sudah telat ${LifeState.duration(lateBy)}" else "") +
                        "\n" + effectSummary(def.effects + payEffect(def)),
                    note = if (tooTired) getString(R.string.too_tired)
                        else if (def.mandatory) "Wajib hadir · tidak hadir = " + (if (def.kind == "sekolah") "bolos" else "absen")
                        else def.kindLabel,
                    noteWarn = def.mandatory,
                    enabled = !tooTired,
                ) { doJob(def) })
            }
            sheetList.addView(sectionLabel("🕊️ Atau lakukan hal lain"))
        }
        gameData.activitiesFor(place, life.slot)
            .filter { a -> a.condition == null || checkCondition(a.condition) }
            .forEach { a ->
                val needEnergy = -(a.effects["energi"] ?: 0L)
                val tooTired = needEnergy > 0 && life[LifeState.Stat.ENERGI] < needEnergy
                sheetList.addView(card(
                    title = "${a.icon}  ${a.name}",
                    sub = "⏱ ${LifeState.duration(a.minutes)}   " + effectSummary(a.effects),
                    note = if (tooTired) getString(R.string.too_tired) else null,
                    enabled = !tooTired,
                ) { doActivity(a) })
            }
        sheetButtonRow(
            getString(R.string.btn_status) to { openStatus(returnTo = { showFreeSheet() }) },
            getString(R.string.btn_shop) to { openShop() },
        )
        val people = gameData.characters.values.filter { c ->
            c.meetable && social.rel(c).met &&
                ("semua" in c.places || place.lowercase() in c.places) &&
                ("semua" in c.slots || life.slot.label.lowercase() in c.slots)
        }
        if (people.isNotEmpty()) {
            sheetList.addView(sectionLabel("👥 Temui seseorang"))
            people.forEach { c ->
                val r = social.rel(c)
                val loveText = if (social.isPartner(c)) "❤️ ${r.romance}" else "💛 ${r.love}"
                sheetList.addView(card(
                    title = c.name,
                    sub = "${c.role}\n${social.stageOf(c).label}   ·   $loveText   🤝 ${r.trust}",
                    note = if (r.lastGiftDay == life.day) "🎁 Sudah diberi hadiah hari ini" else null,
                ) { meetCharacter(c) })
            }
        }
        if (life.slot == LifeState.Slot.MALAM) {
            sheetButtonRow(
                getString(R.string.btn_sleep) to { sleepUntilMorning() },
                "📱 HP" to { openPhoneFromSheet() },
                "☰ Menu" to { openGameMenu() },
            )
        } else {
            sheetButtonRow("📱 HP" to { openPhoneFromSheet() }, "☰ Menu" to { openGameMenu() })
        }
        if (place == "kota") {
            sheetButtonRow("🔎 Cari kerja" to { openJobSearch() }, "🏠 Cari kos" to { openKosSearch() })
        } else {
            sheetButtonRow("🔎 Cari kerja" to { openJobSearch() })
        }
    }

    private fun doActivity(a: GameData.ActivityDef) {
        closeSheet()
        val extra = life.applyEffects(a.effects)
        extra.forEach { (k, v) ->
            vars[k] = ((vars[k]?.toLongOrNull() ?: 0L) + v).toString()
        }
        a.effects["uang"]?.let {
            if (it != 0L) {
                notice((if (it > 0) "💰 +" else "💸 -") + rupiah(kotlin.math.abs(it)))
                logMoney(a.name, it)
            }
        }
        freeLeft--
        vars["_bebas_sisa"] = freeLeft.toString()
        passTime(a.minutes)
        if (life.dead) return
        afterTap = { continueFree() }
        showLine(null, a.text.ifEmpty { a.name } + "\n(⏱ ${LifeState.duration(a.minutes)} · sekarang ${life.timeText})")
    }

    private fun continueFree() {
        if (freeLeft > 0) {
            hideDialog()
            showFreeSheet()
        } else {
            endFree()
        }
    }

    private fun endFree() {
        vars.remove("_bebas_sisa")
        freeLeft = 0
        closeSheet()
        wait = Wait.BUSY
        next()
    }

    /** Tidur: pilih jam bangun. */
    private fun sleepUntilMorning() {
        openSheet("😴 Tidur", "Sekarang ${life.timeText}. Mau bangun jam berapa?") { showFreeSheet() }
        listOf(4 * 60 + 30, 5 * 60, 6 * 60, 7 * 60, 8 * 60, 9 * 60).forEach { wake ->
            val mins = minutesUntil(wake)
            if (mins < 60) return@forEach
            sheetList.addView(card(
                title = "⏰  Bangun ${LifeState.clock(wake)}",
                sub = "Tidur ± ${LifeState.duration(mins)}" + when {
                    mins < 6 * 60 -> "  ·  kurang tidur, besok mengantuk"
                    mins > 10 * 60 -> "  ·  kebanyakan tidur"
                    else -> "  ·  cukup"
                },
            ) { sleepFor(mins) })
        }
        sheetButtonRow(getString(R.string.btn_back) to { showFreeSheet() })
    }

    /** Menit dari sekarang sampai jam [target] berikutnya. */
    private fun minutesUntil(target: Int): Int =
        if (target > life.minute) target - life.minute else life.minutesToMidnight + target

    private fun sleepFor(mins: Int) {
        closeSheet()
        if (!life.active) life.active = true
        passTime(mins, sleeping = true)
        if (life.dead) return
        if (mins < 6 * 60) addExtraVars(life.applyEffects(mapOf("kewarasan" to -4L, "energi" to -8L)))
        refreshLife()
        freeLeft = 0
        vars["_bebas_sisa"] = "0"
        afterTap = { endFree() }
        showLine(null, "{nama} tertidur selama ${LifeState.duration(mins)} dan bangun pukul ${life.timeText}." +
            (if (mins < 6 * 60) " Kepalanya masih berat... kurang tidur." else ""))
    }

    /** Syarat aktivitas, contoh "jalur != kerja" atau "usia >= 18". */
    /** Syarat, contoh "jalur != kerja" atau gabungan "musim == hujan && kos == kos_rel_utara". */
    private fun checkCondition(cond: String): Boolean = cond.split("&&").all { part ->
        val m = Regex("^\\s*(\\w+)\\s*(==|!=|>=|<=|>|<)\\s*(.+?)\\s*$").find(part) ?: return@all true
        val (v, op, value) = m.destructured
        check(Cmd.JumpIf(v, op, value, ""))
    }

    private fun effectSummary(effects: Map<String, Long>): String {
        val icons = mapOf(
            "kesehatan" to "❤️", "kenyang" to "🍚", "hidrasi" to "💧",
            "kewarasan" to "🧠", "energi" to "⚡", "uang" to "💰",
        )
        return effects.entries.joinToString("   ") { (k, v) ->
            val sign = if (v >= 0) "+" else "-"
            val amount = if (k == "uang") rupiah(kotlin.math.abs(v)) else kotlin.math.abs(v).toString()
            "${icons[k] ?: k} $sign$amount"
        }
    }

    // ---------------- status & tas ----------------

    private fun currentSheetReturn(): (() -> Unit)? =
        if (wait == Wait.FREE) ({ showFreeSheet() }) else null

    private fun openStatus(returnTo: (() -> Unit)?) {
        val back: () -> Unit = { if (returnTo != null) returnTo() else closeSheet() }
        openSheet(getString(R.string.status_title), "${life.clockText}   ·   💰 ${rupiah(life.money)}", back)
        val d = resources.displayMetrics.density

        sheetList.addView(playerProfileRow(dark = true))

        LifeState.Stat.values().forEach { stat ->
            val v = life[stat]
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, (4 * d).toInt(), 0, (8 * d).toInt())
            }
            row.addView(TextView(this).apply {
                text = "${stat.icon}  ${stat.label}   $v / 100"
                setTextColor(0xFFF6E7C8.toInt())
                textSize = 14f
            })
            row.addView(android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = v
                progressTintList = android.content.res.ColorStateList.valueOf(
                    when {
                        v >= 60 -> 0xFF4CAF7A.toInt()
                        v >= 30 -> 0xFFE0A93B.toInt()
                        else -> 0xFFE0554B.toInt()
                    }
                )
                progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0x40FFFFFF)
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (10 * d).toInt()).apply {
                topMargin = (4 * d).toInt()
            })
            sheetList.addView(row)
        }

        sheetList.addView(sectionLabel("🏠 Tempat tinggal & keuangan"))
        val kos = gameData.housing[life.kosId]
        sheetList.addView(infoText(buildString {
            append("📅 ").append(life.dateText).append("   ·   Usia ").append(playerAge()).append(" tahun\n")
            when {
                kos != null -> append("🏠 ").append(kos.name).append("\n     Sewa ").append(rupiah(life.rent))
                    .append(" / 30 hari · bayar lagi ").append(dueText(life.rentDueDay))
                vars["lokasi"] == "kota" -> append("🏚️ Tidak punya tempat tinggal — tidur di emperan")
                else -> append("🏠 Rumah keluarga di Tanjung Lentera")
            }
            if (life.arrears > 0) append("\n⚠️ Tunggakan sewa: ").append(life.arrears).append(" kali")
            if (life.allowance > 0) append("\n💌 Kiriman keluarga ").append(rupiah(life.allowance))
                .append(" · berikutnya ").append(dueText(life.allowanceNextDay))
        }))
        if (life.jobs.isNotEmpty()) {
            sheetList.addView(sectionLabel("💼 Sekolah & pekerjaan"))
            life.jobs.toList().forEach { j ->
                val def = gameData.jobs[j.id] ?: return@forEach
                sheetList.addView(card(
                    title = "${def.icon}  ${def.name}",
                    sub = "${def.kindLabel} · ${daysText(def.days)} · ${def.hoursText}" +
                        (if (def.pay > 0) "\n💰 " + rupiah(def.pay) + (if (def.kind == "tetap") " / 30 hari" else " / kerja") else ""),
                    note = (if (def.kind == "sekolah") "📊 Nilai " else "📊 Kinerja ") + "${j.performance}   ·   " +
                        (if (def.kind == "sekolah") "Bolos " else "Absen ") + "${j.absences}×",
                    noteWarn = j.absences >= 3 || j.performance < 30,
                    actions = listOf((if (def.kind == "sekolah") "Keluar" else "Berhenti") to {
                        stopJob(def.id)
                        openStatus(returnTo)
                    }),
                ))
            }
        }
        sheetList.addView(sectionLabel("🎒 Tas"))
        if (life.items.isEmpty()) {
            sheetList.addView(TextView(this).apply {
                setText(R.string.bag_empty)
                setTextColor(0x99E8E0CF.toInt())
                textSize = 14f
            })
        }
        life.items.sortedBy { it.expiresDay }.forEach { item ->
            val def = gameData.items[item.id] ?: return@forEach
            val left = item.expiresDay - life.day
            val status = when {
                life.isExpired(item) -> "⚠️ " + getString(R.string.expired)
                left == 0 -> getString(R.string.expires_today)
                else -> getString(R.string.expires_in, left)
            }
            val drinkOnly = (def.effects["kenyang"] ?: 0L) <= 0L && def.effects.containsKey("hidrasi")
            val medicine = !def.effects.containsKey("kenyang") && !def.effects.containsKey("hidrasi")
            val useLabel = getString(
                when {
                    medicine -> R.string.btn_use
                    drinkOnly -> R.string.btn_drink
                    else -> R.string.btn_eat
                }
            )
            sheetList.addView(card(
                title = "${def.icon}  ${def.name}",
                sub = effectSummary(def.effects),
                note = status,
                noteWarn = life.isExpired(item),
                actions = listOf(
                    useLabel to {
                        notice(life.consume(item, def))
                        refreshLife()
                        if (!checkDeath()) openStatus(returnTo)
                    },
                    getString(R.string.btn_discard) to {
                        life.items.remove(item)
                        openStatus(returnTo)
                    },
                ),
            ))
        }
        sheetButtonRow(
            (if (returnTo != null) getString(R.string.btn_back) else getString(R.string.btn_close)) to back,
        )
    }

    // ---------------- belanja ----------------

    private fun openShop() {
        val place = vars["lokasi"] ?: "desa"
        val placeName = if (place == "desa") "Warung desa" else if (place == "kota") "Minimarket" else place
        openSheet(getString(R.string.shop_title), "$placeName   ·   💰 ${rupiah(life.money)}") { showFreeSheet() }
        gameData.shopFor(place).sortedBy { it.price }.forEach { def ->
            val affordable = life.money >= def.price
            sheetList.addView(card(
                title = "${def.icon}  ${def.name}",
                sub = effectSummary(def.effects),
                note = rupiah(def.price) + if (affordable) "" else "  ·  " + getString(R.string.no_money),
                noteWarn = !affordable,
                actions = listOf(getString(R.string.btn_buy) to {
                    if (life.money >= def.price) {
                        life.money -= def.price
                        logMoney("Belanja: ${def.name}", -def.price)
                        life.addItem(def.id, def.shelfDays)
                        refreshLife()
                        notice(getString(R.string.bought, def.name))
                        openShop()
                    }
                }),
                actionsEnabled = affordable,
            ))
        }
        sheetButtonRow(
            getString(R.string.btn_status) to { openStatus(returnTo = { openShop() }) },
            getString(R.string.btn_back) to { showFreeSheet() },
        )
    }

    // ---------------- lembar serbaguna ----------------

    private fun openSheet(title: String, sub: String, back: (() -> Unit)?) {
        sheetBack = back
        sheetTitle.text = title
        sheetSub.text = sub
        sheetList.removeAllViews()
        sheetButtons.removeAllViews()
        sheetButtons.orientation = LinearLayout.VERTICAL
        if (sheet.visibility != View.VISIBLE) {
            sheet.bringToFront()
            sheet.visibility = View.VISIBLE
            sheet.alpha = 0f
            sheet.animate().alpha(1f).setDuration(200).start()
        }
    }

    private fun closeSheet() {
        sheetBack = null
        if (sheet.visibility != View.VISIBLE) return
        sheet.animate().alpha(0f).setDuration(160).withEndAction { sheet.visibility = View.GONE }.start()
    }

    private fun sectionLabel(text: String) = TextView(this).apply {
        this.text = text
        typeface = resources.getFont(R.font.cinzel)
        setTextColor(0xFFF2D58C.toInt())
        textSize = 15f
        setPadding(0, (14 * resources.displayMetrics.density).toInt(), 0, (8 * resources.displayMetrics.density).toInt())
    }

    private fun sheetButtonRow(vararg buttons: Pair<String, () -> Unit>) {
        val d = resources.displayMetrics.density
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttons.forEachIndexed { i, (label, action) ->
            row.addView(TextView(this).apply {
                text = label
                gravity = android.view.Gravity.CENTER
                setTextColor(0xFFF6E7C8.toInt())
                textSize = 14f
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 12 * d
                    setColor(0x40101A3A)
                    setStroke((1 * d).toInt(), 0xAAD2AE62.toInt())
                }
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, (44 * d).toInt(), 1f).apply {
                if (i > 0) marginStart = (8 * d).toInt()
            })
        }
        sheetButtons.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = (6 * d).toInt() })
    }

    /** Kartu pilihan di lembar: judul, keterangan efek, catatan, dan tombol aksi. */
    private fun card(
        title: String,
        sub: String,
        note: String? = null,
        noteWarn: Boolean = false,
        enabled: Boolean = true,
        actions: List<Pair<String, () -> Unit>> = emptyList(),
        actionsEnabled: Boolean = true,
        onClick: (() -> Unit)? = null,
    ): View {
        val d = resources.displayMetrics.density
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding((14 * d).toInt(), (11 * d).toInt(), (12 * d).toInt(), (11 * d).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 14 * d
                setColor(0x33FFFFFF)
                setStroke((1 * d).toInt(), 0x55D2AE62)
            }
            alpha = if (enabled) 1f else 0.5f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = (8 * d).toInt() }
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply {
            text = title
            setTextColor(0xFFFFF7E6.toInt())
            textSize = 15.5f
            typeface = Typeface.DEFAULT_BOLD
        })
        if (sub.isNotEmpty()) col.addView(TextView(this).apply {
            text = sub
            setTextColor(0xCCE8E0CF.toInt())
            textSize = 12.5f
            setPadding(0, (3 * d).toInt(), 0, 0)
        })
        if (note != null) col.addView(TextView(this).apply {
            text = note
            setTextColor(if (noteWarn) 0xFFFF9A8A.toInt() else 0xFFF2D58C.toInt())
            textSize = 12.5f
            setPadding(0, (3 * d).toInt(), 0, 0)
        })
        box.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        actions.forEach { (label, action) ->
            box.addView(TextView(this).apply {
                text = label
                gravity = android.view.Gravity.CENTER
                textSize = 13f
                setTextColor(if (actionsEnabled) 0xFF1A1206.toInt() else 0xFF8A8A8A.toInt())
                setPadding((12 * d).toInt(), 0, (12 * d).toInt(), 0)
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 10 * d
                    if (actionsEnabled) {
                        orientation = android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
                        colors = intArrayOf(0xFFF8E3A8.toInt(), 0xFFD2AE62.toInt())
                    } else {
                        setColor(0x55FFFFFF)
                    }
                }
                isEnabled = actionsEnabled
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (36 * d).toInt()).apply {
                marginStart = (6 * d).toInt()
            })
        }
        if (onClick != null && enabled) box.setOnClickListener { onClick() }
        return box
    }

    // ================================================================== sekolah, pekerjaan, kos

    private fun payEffect(def: GameData.JobDef): Map<String, Long> =
        if (def.pay > 0 && def.kind != "tetap" && def.kind != "sekolah") mapOf("uang" to def.pay) else emptyMap()

    private fun daysText(days: Set<Int>): String {
        val names = LifeState.WEEKDAYS
        if (days.size == 7) return "Setiap hari"
        val sorted = days.sorted()
        val contiguous = sorted.zipWithNext().all { (a, b) -> b == a + 1 }
        return if (contiguous && sorted.size > 2) "${names[sorted.first()]}–${names[sorted.last()]}"
        else sorted.joinToString(", ") { names[it] }
    }

    private fun dueText(dayIndex: Int): String {
        if (dayIndex < 0) return "-"
        val left = dayIndex - life.day
        val d = if (life.startEpochDay != 0L) java.time.LocalDate.ofEpochDay(life.startEpochDay + dayIndex) else null
        val date = d?.let { "${it.dayOfMonth} ${LifeState.MONTHS[it.monthValue - 1].take(3)}" } ?: "hari ke-${dayIndex + 1}"
        return "$date (${if (left <= 0) "hari ini" else "$left hari lagi"})"
    }

    private fun infoText(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(0xFFF6E7C8.toInt())
        textSize = 13.5f
        setLineSpacing(0f, 1.2f)
        setPadding(0, 0, 0, (6 * resources.displayMetrics.density).toInt())
    }

    private fun addExtraVars(extra: Map<String, Long>) {
        extra.forEach { (k, v) -> vars[k] = ((vars[k]?.toLongOrNull() ?: 0L) + v).toString() }
    }

    private fun startJob(id: String) {
        val def = gameData.jobs[id] ?: return
        if (life.jobs.any { it.id == id }) return
        life.jobs += LifeState.Job(id, life.day)
        vars["kerja_$id"] = "1"
        notice("${def.icon} " + (if (def.kind == "sekolah") "Mulai bersekolah di " else "Mulai bekerja: ") + def.name)
    }

    private fun stopJob(id: String) {
        val def = gameData.jobs[id]
        if (life.jobs.removeAll { it.id == id } && def != null) {
            vars["kerja_$id"] = "0"
            notice("${def.icon} " + (if (def.kind == "sekolah") "Berhenti sekolah dari " else "Berhenti dari ") + def.name)
        }
    }

    /** Peluang diterima: tingkat kesulitan, kemampuan, skor wawancara, dan orang dalam. */
    private fun applyJob(id: String, announceResult: Boolean): Boolean {
        val def = gameData.jobs[id] ?: return false
        if (playerAge() < def.minAge) {
            if (announceResult) notice("Usiamu belum cukup (minimal ${def.minAge} tahun)")
            return false
        }
        if (def.condition != null && !checkCondition(def.condition)) return false
        var chance = 100 - def.difficulty
        val skill = (vars["akademik"]?.toIntOrNull() ?: 0) + (vars["komunikasi"]?.toIntOrNull() ?: 0)
        chance += (skill * 2).coerceAtMost(30)
        chance += vars["skor_wawancara"]?.toIntOrNull() ?: 0
        if (life[LifeState.Stat.KEWARASAN] < 30) chance -= 15
        val ref = def.referrer?.let { gameData.characters[it] }
        var viaInsider = false
        if (ref != null) {
            val r = social.rel(ref)
            if (r.met && r.love >= 60 && r.trust >= 60) {
                chance += 45
                viaInsider = true
            }
        }
        val ok = kotlin.random.Random.nextInt(100) < chance.coerceIn(5, 97)
        vars["lewat_orang_dalam"] = if (viaInsider) "1" else "0"
        if (ok) {
            startJob(id)
            if (viaInsider && ref != null) notice("🤝 Diterima lewat rekomendasi ${ref.name}")
        } else if (announceResult) {
            notice("❌ Lamaran ke ${def.name} ditolak")
        }
        return ok
    }

    private fun doJob(def: GameData.JobDef) {
        closeSheet()
        val job = life.jobs.firstOrNull { it.id == def.id } ?: return
        val late = life.minute - def.startMin
        val waitMin = (def.startMin - life.minute).coerceAtLeast(0)
        val workMin = def.endMin - maxOf(life.minute, def.startMin)
        if (late > 15) {
            job.performance = (job.performance - (if (late > 60) 6 else 3)).coerceAtLeast(0)
            notice("⏰ Kamu terlambat ${LifeState.duration(late)}!")
        }
        addExtraVars(life.applyEffects(def.effects + payEffect(def)))
        payEffect(def)["uang"]?.let {
            notice("💰 +" + rupiah(it))
            logMoney("Upah: ${def.name}", it)
        }
        job.shifts++
        job.performance = (job.performance + (if (life[LifeState.Stat.ENERGI] >= 20) 3 else -4)).coerceIn(0, 100)
        life.markAttended(def.id)
        if (job.performance < 15 && def.kind != "sekolah") {
            life.jobs.remove(job)
            vars["dipecat_${def.id}"] = "1"
            notice("📄 Kinerjamu terlalu buruk. Kamu diberhentikan dari ${def.name}.")
        }
        freeLeft--
        vars["_bebas_sisa"] = freeLeft.toString()
        passTime(waitMin + workMin)
        if (life.dead) return
        afterTap = { continueFree() }
        showLine(null, def.text.ifEmpty { def.name } + "\n(${def.hoursText} · sekarang ${life.timeText})")
    }

    /** Janji temu yang waktunya sudah lewat = ingkar janji. */
    private fun checkMissedAppointments(excused: Boolean = false) {
        phoneData.appts.filter { a ->
            val end = runCatching { LifeState.Slot.valueOf(a.slot).end }.getOrDefault(24 * 60)
            a.day < life.day || (a.day == life.day && life.minute >= end)
        }.forEach { a ->
            phoneData.appts.remove(a)
            gameData.characters[a.charId]?.let { c ->
                val place = (vars["lokasi"] ?: "desa").lowercase()
                if (excused || ("semua" !in c.places && place !in c.places)) {
                    // pemain sudah pindah tempat (mis. berangkat ke kota): janji batal baik-baik
                    receiveMessage(c.name, if (excused) "Eh, tadi kamu lagi sibuk ya? Nggak apa-apa, lain kali aja 🙂"
                        else "Oh iya, kamu udah pindah ya. Janjinya nanti aja kalau kamu pulang 🙂")
                    return@let
                }
                announce(social.behave(gameData, c.id, "ingkar_janji"))
                notice("💔 Kamu tidak datang ke janji dengan ${c.name}")
                receiveMessage(c.name, "Aku nungguin kamu tadi... Kok nggak datang?")
            }
        }
    }

    /** Sekolah / kerja tetap hari ke-[d] yang tidak dihadiri (batas: 1 jam sebelum selesai). */
    private fun checkMissedJobs(d: Int, wholeDay: Boolean, excused: Boolean = false) {
        life.jobs.toList().forEach { job ->
            val def = gameData.jobs[job.id] ?: return@forEach
            if (!def.mandatory || !gameData.scheduledOn(def, life.weekdayOf(d))) return@forEach
            if (d < job.startDay || life.hasAttended(def.id, d) || life.isMarkedAbsent(def.id, d)) return@forEach
            if (!wholeDay && life.minute <= def.endMin - 60) return@forEach
            if (excused) {
                // waktu dimajukan oleh cerita: dianggap hadir
                life.markAttended(def.id, d)
                return@forEach
            }
            life.markAbsent(def.id, d)
            job.absences++
            job.performance = (job.performance - 8).coerceAtLeast(0)
            val school = def.kind == "sekolah"
            notice(if (school) "⚠️ Kamu bolos sekolah (${job.absences}×)" else "⚠️ Kamu tidak masuk kerja (${job.absences}×)")
            when {
                job.absences >= 6 -> {
                    life.jobs.remove(job)
                    vars[if (school) "dikeluarkan_${def.id}" else "dipecat_${def.id}"] = "1"
                    notice(if (school) "🏫 Kamu dikeluarkan dari ${def.name}." else "📄 Kamu dipecat dari ${def.name}.")
                }
                job.absences == 3 -> notice("📄 Surat peringatan dari ${def.name}!")
            }
        }
    }

    /** Hal-hal yang terjadi setiap ganti hari: ulang tahun, sewa kos, kiriman, gaji bulanan. */
    private fun onNewDay() {
        processPhoneDaily()
        rollDailyEvent()
        checkSchool()
        if (life.day > 0 && life.day % 365 == 0) {
            vars["usia"] = (playerAge() + 1).toString()
            notice("🎂 Selamat ulang tahun! Usiamu sekarang ${playerAge()} tahun.")
        }
        if (life.kosId.isNotEmpty() && life.rentDueDay in 0..life.day) {
            if (life.money >= life.rent) {
                life.money -= life.rent
                logMoney("Sewa kos", -life.rent)
                life.arrears = 0
                notice("🏠 Bayar sewa kos " + rupiah(life.rent))
            } else {
                life.arrears++
                notice("⚠️ Uang tidak cukup bayar kos! Tunggakan ${life.arrears}×")
            }
            life.rentDueDay += 30
            if (life.arrears >= 2) evict()
        }
        if (life.allowance > 0 && life.allowanceNextDay in 0..life.day) {
            life.money += life.allowance
            logMoney("Kiriman Ayah & Ibu", life.allowance)
            life.allowanceNextDay += 30
            notice("💌 Kiriman dari Ayah & Ibu: +" + rupiah(life.allowance))
        }
        life.jobs.forEach { j ->
            val def = gameData.jobs[j.id] ?: return@forEach
            val worked = life.day - j.startDay
            if (def.kind == "tetap" && def.pay > 0 && worked > 0 && worked % 30 == 0) {
                val pay = (def.pay * (0.6 + j.performance / 250.0)).toLong()
                life.money += pay
                logMoney("Gaji: ${def.name}", pay)
                notice("💼 Gaji dari ${def.name}: +" + rupiah(pay))
            }
        }
    }

    private fun moveIn(id: String, fromStory: Boolean) {
        val def = gameData.housing[id]
        if (def == null) {
            life.kosId = ""
            life.rent = 0
            life.rentDueDay = -1
            life.arrears = 0
            life.sleepEnergy = 0
            life.sleepSanity = 0
            vars["kos"] = ""
            return
        }
        life.kosId = def.id
        life.rent = def.price
        life.sleepEnergy = def.sleepEnergy
        life.sleepSanity = def.sleepSanity
        life.rentDueDay = life.day + 30
        vars["kos"] = def.id
        vars["diusir"] = "0"
        if (life.money >= def.price) {
            life.money -= def.price
            logMoney("Sewa kos: ${def.name}", -def.price)
            life.arrears = 0
            notice("🏠 Pindah ke ${def.name} · bayar " + rupiah(def.price))
        } else {
            life.arrears = 1
            notice("🏠 Pindah ke ${def.name} · uangmu kurang, sewa jadi tunggakan!")
        }
        refreshLife()
    }

    private fun evict() {
        val name = gameData.housing[life.kosId]?.name ?: "kos"
        life.kosId = ""
        life.rent = 0
        life.rentDueDay = -1
        life.arrears = 0
        life.sleepEnergy = -40
        life.sleepSanity = -10
        vars["kos"] = ""
        vars["diusir"] = "1"
        notice("🏚️ Kamu diusir dari $name! Sampai dapat tempat baru, kamu tidur di emperan.")
    }

    private fun openJobSearch() {
        val place = vars["lokasi"] ?: "desa"
        openSheet("🔎 Cari kerja", "Melamar memakai 1 kesempatan waktu bebas") { showFreeSheet() }
        val list = gameData.jobs.values.filter { j ->
            j.kind != "sekolah" && (j.way == "langsung" || j.way == "lamar") &&
                ("semua" in j.places || place in j.places) && life.jobs.none { it.id == j.id }
        }
        if (list.isEmpty()) sheetList.addView(infoText("Belum ada lowongan di sini."))
        list.forEach { j ->
            val ref = j.referrer?.let { gameData.characters[it] }
            val notes = mutableListOf<String>()
            if (j.minAge > 0) notes += "Usia min. ${j.minAge}"
            notes += if (j.way == "langsung") "Peluang besar" else "Seleksi ketat"
            if (ref != null && social.rel(ref).met) notes += "Orang dalam: ${ref.name}"
            sheetList.addView(card(
                title = "${j.icon}  ${j.name}",
                sub = "${j.kindLabel} · ${daysText(j.days)} · ${j.hoursText}\n" +
                    "💰 " + rupiah(j.pay) + (if (j.kind == "tetap") " / 30 hari" else " / kerja") +
                    (if (j.desc.isNotEmpty()) "\n" + j.desc else ""),
                note = notes.joinToString(" · "),
                actions = listOf("Lamar" to { applyFromSearch(j) }),
            ))
        }
        sheetButtonRow(getString(R.string.btn_back) to { showFreeSheet() })
    }

    private fun applyFromSearch(j: GameData.JobDef) {
        closeSheet()
        val ok = applyJob(j.id, announceResult = true)
        addExtraVars(life.applyEffects(mapOf("energi" to -5L, "kewarasan" to (if (ok) 5L else -5L))))
        freeLeft--
        vars["_bebas_sisa"] = freeLeft.toString()
        passTime(60)
        if (life.dead) return
        afterTap = { continueFree() }
        showLine(null, if (ok) "Kamu diterima! ${j.name} — mulai sekarang kamu bisa bekerja sesuai jadwal."
            else "Lamaranmu ke ${j.name} ditolak. Mungkin lain kali, dengan persiapan yang lebih baik.")
    }

    private fun openKosSearch() {
        openSheet("🏠 Cari kos", "💰 ${rupiah(life.money)} · sewa dibayar di muka untuk 30 hari") { showFreeSheet() }
        gameData.housing.values.filter { "semua" in it.places || "kota" in it.places }.sortedBy { it.price }.forEach { k ->
            val current = k.id == life.kosId
            sheetList.addView(card(
                title = "🏠  ${k.name}",
                sub = rupiah(k.price) + " / 30 hari\n" + k.text,
                note = if (current) "Tempat tinggalmu sekarang" else
                    "Tidur: energi ${if (k.sleepEnergy >= 0) "+" else ""}${k.sleepEnergy}, kewarasan ${if (k.sleepSanity >= 0) "+" else ""}${k.sleepSanity}",
                actions = if (current) emptyList() else listOf("Pindah" to {
                    moveIn(k.id, fromStory = false)
                    openKosSearch()
                }),
            ))
        }
        sheetButtonRow(getString(R.string.btn_back) to { showFreeSheet() })
    }

    // ================================================================== kejadian, ujian, kenaikan kelas

    /** Setiap ganti hari: paling banyak satu kejadian acak. */
    private fun rollDailyEvent() {
        if (!life.active) return
        val place = vars["lokasi"] ?: "desa"
        val candidates = gameData.events.filter { e ->
            e.chance > 0 &&
                ("semua" in e.places || place in e.places) &&
                (!e.onceOnly || vars["kejadian_${e.id}"] != "1") &&
                (e.condition == null || checkCondition(e.condition))
        }.shuffled()
        candidates.firstOrNull { kotlin.random.Random.nextInt(100) < it.chance }?.let { pendingEvents.addLast("ev:${it.id}") }
    }

    /** Ujian berkala & kenaikan kelas untuk sekolah yang sedang dijalani. */
    private fun checkSchool() {
        life.jobs.toList().forEach { j ->
            val def = gameData.jobs[j.id] ?: return@forEach
            if (def.kind != "sekolah") return@forEach
            val days = life.day - j.startDay
            if (days <= 0) return@forEach
            if (def.examEvery > 0 && days % def.examEvery == 0) pendingEvents.addLast("ujian:${def.id}")
            if (days % 365 == 0) pendingEvents.addLast("kelas:${def.id}")
        }
    }

    private fun runPending(key: String, done: () -> Unit) {
        val kind = key.substringBefore(':')
        val id = key.substringAfter(':')
        when (kind) {
            "ev" -> gameData.events.firstOrNull { it.id == id }?.let { runEvent(it, done) } ?: done()
            "ujian" -> gameData.jobs[id]?.let { runExam(it, done) } ?: done()
            "kelas" -> gameData.jobs[id]?.let { runSchoolYear(it, done) } ?: done()
            else -> done()
        }
    }

    // ---------------- tutorial: tampilkan bagian layar ----------------

    private var tourOverlay: FrameLayout? = null
    private var tourTarget: String? = null

    private fun tourLayer(): FrameLayout {
        tourOverlay?.let { return it }
        val d = resources.displayMetrics.density
        val layer = FrameLayout(this).apply {
            isClickable = true
            visibility = View.GONE
            elevation = 40 * d
        }
        val card = LinearLayout(this).apply {
            tag = "tour_card"
            orientation = LinearLayout.VERTICAL
            setPadding((16 * d).toInt(), (12 * d).toInt(), (16 * d).toInt(), (10 * d).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 16 * d
                setColor(0xF2141B33.toInt())
                setStroke((1.5f * d).toInt(), 0xFFD2AE62.toInt())
            }
        }
        card.addView(TextView(this).apply {
            tag = "tour_text"
            setTextColor(0xFFF6E7C8.toInt())
            textSize = 14.5f
            setLineSpacing(0f, 1.15f)
        })
        card.addView(TextView(this).apply {
            text = "Ketuk untuk lanjut ▸"
            setTextColor(0xFFD2AE62.toInt())
            textSize = 11.5f
            gravity = android.view.Gravity.END
            setPadding(0, (6 * d).toInt(), 0, 0)
        })
        layer.addView(card, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        layer.setOnClickListener { tourTap() }
        findViewById<ViewGroup>(android.R.id.content).addView(layer,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        tourOverlay = layer
        return layer
    }

    private fun showTour(c: Cmd.Show) {
        wait = Wait.BUSY
        if (dialog.alpha > 0f) hideDialog()
        if (tourTarget != null && tourTarget != c.target) closeTourTarget()
        when (c.target) {
            "hp" -> {
                refreshPhoneHome()
                if (!phone.isOpen) phone.open()
                if (c.sub != null) phone.openAppById(c.sub) else phone.showHome()
            }
            "status" -> setHudOpen(true)
            "tas" -> openStatus(returnTo = null)
            "belanja" -> openShop()
            "kerja" -> openJobSearch()
            "kos" -> openKosSearch()
            "karakter" -> openCharacterList(null)
            "karakter_detail" -> gameData.characters[c.sub ?: ""]?.let { openCharacterDetail(it, null) }
            "bebas" -> {
                pendingEvents.clear()
                freeLeft = 2
                showFreeSheet()
                wait = Wait.BUSY
            }
            "jam" -> pulse(clock)
            "menu" -> pulse(burger)
            "ikon_hp" -> pulse(phoneIcon)
            "panel" -> pulse(charPanel)
        }
        tourTarget = c.target
        val layer = tourLayer()
        val d = resources.displayMetrics.density
        val card = layer.findViewWithTag<View>("tour_card")
        layer.findViewWithTag<TextView>("tour_text").text = fill(c.text).replace("<br>", "\n")
        (card.layoutParams as FrameLayout.LayoutParams).apply {
            gravity = if (c.top) android.view.Gravity.TOP else android.view.Gravity.BOTTOM
            setMargins((14 * d).toInt(), ((if (c.top) 38 else 0) * d).toInt(), (14 * d).toInt(), ((if (c.top) 0 else 26) * d).toInt())
            card.layoutParams = this
        }
        layer.bringToFront()
        layer.visibility = View.VISIBLE
        card.alpha = 0f
        card.translationY = (if (c.top) -12 else 12) * d
        card.animate().alpha(1f).translationY(0f).setStartDelay(250).setDuration(260).start()
    }

    private fun tourTap() {
        val layer = tourOverlay ?: return
        if (layer.findViewWithTag<View>("tour_card").alpha < 1f) return
        layer.visibility = View.GONE
        val nextShow = script.getOrNull(pc) as? Cmd.Show
        if (nextShow?.target != tourTarget) closeTourTarget()
        handler.postDelayed({ next() }, if (nextShow?.target == tourTarget) 0L else 280L)
    }

    private fun closeTourTarget() {
        when (tourTarget) {
            "hp" -> phone.close()
            "status" -> setHudOpen(false)
            "bebas" -> { freeLeft = 0; closeSheet() }
            "tas", "belanja", "kerja", "kos", "karakter", "karakter_detail" -> closeSheet()
        }
        tourTarget = null
        wait = Wait.BUSY
    }

    /** Denyut kecil untuk menunjuk sebuah elemen layar. */
    private fun pulse(v: View) {
        v.animate().cancel()
        v.animate().scaleX(1.35f).scaleY(1.35f).setDuration(260).withEndAction {
            v.animate().scaleX(1f).scaleY(1f).setDuration(260).withEndAction {
                v.animate().scaleX(1.25f).scaleY(1.25f).setDuration(220).withEndAction {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(220).start()
                }.start()
            }.start()
        }.start()
    }

    // ================================================================== profil karakter

    /** Info profil & syarat terbukanya: Cinta minimal, jumlah obrolan, Kepercayaan, bisa diceritakan orang lain. */
    private data class Fact(val key: String, val label: String, val love: Int, val talks: Int, val trust: Int, val gossip: Boolean)

    private val facts = listOf(
        Fact("hobi", "🎯 Hobi", 20, 1, 0, true),
        Fact("kegemaran", "⭐ Kegemaran", 30, 2, 0, true),
        Fact("pelajaran", "📘 Pelajaran favorit", 30, 2, 0, true),
        Fact("keahlian", "🛠️ Keahlian", 30, 2, 0, true),
        Fact("cita_cita", "🌠 Cita-cita", 45, 3, 0, true),
        Fact("kisah_1", "📖 Kisah hidupnya (1)", 40, 3, 0, true),
        Fact("kisah_2", "📖 Kisah hidupnya (2)", 60, 6, 55, false),
        Fact("kisah_3", "📖 Kisah hidupnya (3)", 80, 10, 75, false),
    )

    private fun initialFacts(c: GameData.CharacterDef): Set<String> =
        c.text["tahu_awal"]?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

    private fun knowsFact(c: GameData.CharacterDef, key: String): Boolean {
        val r = social.rel(c)
        return "f:$key" in r.known || ((r.met || c.text["tampil_awal"] == "ya") && key in initialFacts(c))
    }

    /** Karakter tampil di daftar bila sudah dikenal, pernah diceritakan orang lain, atau tampil_awal. */
    private fun profileVisible(c: GameData.CharacterDef): Boolean {
        val r = social.rel(c)
        return r.met || c.text["tampil_awal"] == "ya" || r.known.any { it.startsWith("f:") }
    }

    private fun learnFact(c: GameData.CharacterDef, key: String) {
        social.rel(c).known += "f:$key"
    }

    /** Setelah mengobrol: buka satu info baru tentang karakter itu bila syaratnya terpenuhi. */
    private fun revealOwnFact(c: GameData.CharacterDef, talks: Int): String? {
        val r = social.rel(c)
        val love = if (social.isPartner(c)) maxOf(r.love, r.romance) else r.love
        val f = facts.firstOrNull { f ->
            c.text[f.key] != null && !knowsFact(c, f.key) &&
                love >= f.love && talks >= f.talks && r.trust >= f.trust
        } ?: return null
        learnFact(c, f.key)
        notice("📖 Info baru tentang ${c.name}: ${f.label.substringAfter(' ')}")
        return "📖 ${c.name} bercerita — ${f.label.substringAfter(' ')}:\n" + fill(c.text[f.key] ?: "")
    }

    /** Kadang ia bercerita tentang orang dekatnya (keluarga/teman) yang belum kamu ketahui. */
    private fun gossipFact(c: GameData.CharacterDef): String? {
        if (social.rel(c).love < 35 || kotlin.random.Random.nextInt(100) >= 30) return null
        val options = gameData.relationsOf(c.id).mapNotNull { (otherId, _) ->
            val o = gameData.characters[otherId] ?: return@mapNotNull null
            if (o.text["daftar"] == "tidak") return@mapNotNull null
            val f = facts.firstOrNull { it.gossip && o.text[it.key] != null && !knowsFact(o, it.key) } ?: return@mapNotNull null
            o to f
        }
        val (o, f) = options.randomOrNull() ?: return null
        learnFact(o, f.key)
        notice("🗣️ Kamu jadi tahu tentang ${o.name}")
        return "🗣️ ${c.name} bercerita tentang ${o.name} — ${f.label.substringAfter(' ')}:\n" + fill(o.text[f.key] ?: "")
    }

    private fun openCharacterList(returnTo: (() -> Unit)?) {
        val back: () -> Unit = { if (returnTo != null) returnTo() else closeSheet() }
        val all = gameData.characters.values.filter { it.text["daftar"] != "tidak" }
        val shown = all.count { profileVisible(it) }
        openSheet("👥 Karakter", "$shown dari ${all.size} karakter kamu ketahui", back)
        all.forEach { c ->
            if (profileVisible(c)) {
                val r = social.rel(c)
                val love = if (social.isPartner(c)) "❤️ ${r.romance}" else "💛 ${r.love}"
                val known = facts.count { c.text[it.key] != null && knowsFact(c, it.key) }
                val total = facts.count { c.text[it.key] != null }
                sheetList.addView(card(
                    title = c.name,
                    sub = c.role + "\n" + (if (r.met) "${social.stageOf(c).label}   ·   $love   🤝 ${r.trust}" else "Belum bertemu langsung"),
                    note = "📖 Info terbuka $known/$total",
                ) { openCharacterDetail(c, returnTo) })
            } else {
                sheetList.addView(card(
                    title = "🔒  ???",
                    sub = "Belum kamu kenal. Siapa tahu kalian akan bertemu, atau kamu mendengar tentangnya dari orang lain.",
                    enabled = false,
                ))
            }
        }
        sheetButtonRow(getString(R.string.btn_back) to back)
    }

    private fun openCharacterDetail(c: GameData.CharacterDef, returnTo: (() -> Unit)?) {
        val r = social.rel(c)
        val d = resources.displayMetrics.density
        openSheet(c.name, c.role) { openCharacterList(returnTo) }
        charBitmap(c.id, "biasa")?.let { bmp ->
            sheetList.addView(android.widget.ImageView(this).apply {
                setImageBitmap(bmp)
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                adjustViewBounds = true
                if (!r.met) colorFilter = android.graphics.PorterDuffColorFilter(0xAA101425.toInt(), android.graphics.PorterDuff.Mode.SRC_ATOP)
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (260 * d).toInt()))
        }
        sheetList.addView(infoText(buildString {
            append("🎂 Usia ").append(social.ageOf(c, playerAge())).append(" tahun")
            if (r.met) append("   ·   ").append(social.stageOf(c).label)
            else append("   ·   belum bertemu langsung")
        }))
        if (r.met) {
            fun bar(label: String, v: Int, color: Int) {
                sheetList.addView(TextView(this).apply {
                    text = "$label   $v / 100"
                    setTextColor(0xFFF6E7C8.toInt())
                    textSize = 13.5f
                    setPadding(0, (8 * d).toInt(), 0, (2 * d).toInt())
                })
                sheetList.addView(android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = 100
                    progress = v
                    progressTintList = android.content.res.ColorStateList.valueOf(color)
                    progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0x40FFFFFF)
                }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (10 * d).toInt()))
            }
            bar("💛 Cinta", r.love, 0xFFE0A93B.toInt())
            if (social.isPartner(c)) bar("❤️ Cinta pasangan", r.romance, 0xFFE0556B.toInt())
            bar("🤝 Kepercayaan", r.trust, 0xFF4C9FD6.toInt())
            c.text["sifat"]?.let {
                sheetList.addView(sectionLabel("✨ Sifat"))
                sheetList.addView(infoText(it.replaceFirstChar { ch -> ch.uppercase() }))
            }
            fun knownNames(prefix: String) = r.known.filter { it.startsWith(prefix) }.mapNotNull { k ->
                val id = k.substring(3)
                gameData.topics[id]?.name ?: gameData.items[id]?.name
            }
            val likes = knownNames("+t:") + knownNames("+i:")
            val hates = knownNames("-t:") + knownNames("-i:")
            sheetList.addView(sectionLabel("👍 Suka"))
            sheetList.addView(infoText(likes.joinToString(", ").ifEmpty { "🔒 Belum diketahui · coba ajak ngobrol berbagai topik atau beri hadiah" }))
            sheetList.addView(sectionLabel("👎 Tidak suka"))
            sheetList.addView(infoText(hates.joinToString(", ").ifEmpty { "🔒 Belum diketahui" }))
        }
        facts.filter { c.text[it.key] != null }.forEach { f ->
            sheetList.addView(sectionLabel(f.label))
            sheetList.addView(infoText(
                if (knowsFact(c, f.key)) fill(c.text[f.key] ?: "")
                else if (f.key.startsWith("kisah")) "🔒 Akan terbuka saat kalian makin dekat dan saling percaya."
                else "🔒 Belum diketahui · makin akrab, makin banyak yang ia ceritakan. Teman dekatnya juga mungkin tahu."
            ))
        }
        sheetButtonRow(getString(R.string.btn_back) to { openCharacterList(returnTo) })
    }

    /** Bersihkan semua hasil tutorial sebelum cerita sungguhan dimulai. */
    private fun resetForStory() {
        life = LifeState()
        social = SocialState()
        phoneData = PhoneState()
        pendingEvents.clear()
        noticeQueue.clear()
        messages.clear()
        log.clear()
        vars.clear()
        vars["nama"] = playerName
        vars["kelamin"] = playerGender
        hideCharacter(null)
        speakingCharId = null
        updateCharPanel(null)
        setPhoneIconVisible(false)
        setClock(null)
        hudOpen = false
        syncSocialVars()
        refreshLife()
        refreshPhoneHome()
    }

    /** Kebohongan terbongkar: semua yang dibohongi kecewa. */
    private fun revealLie(code: String) {
        if (vars["terbongkar_$code"] == "1") return
        val ids = social.lies[code].orEmpty()
        ids.forEach { announce(social.behave(gameData, it, "bohong_ketahuan")) }
        vars["terbongkar_$code"] = "1"
        syncSocialVars()
    }

    /** Terapkan efek kejadian: bar, uang (+ persen), cinta_x / percaya_x, terbongkar_x, dan variabel lain. */
    private fun applyEventEffects(effects: Map<String, Long>, desc: String) {
        val lifeEffects = mutableMapOf<String, Long>()
        effects.forEach { (k, v) ->
            when {
                k.startsWith("cinta_") -> announce(social.changeLove(gameData, k.removePrefix("cinta_"), v.toInt()))
                k.startsWith("terbongkar_") -> revealLie(k.removePrefix("terbongkar_"))
                k.startsWith("percaya_") -> {
                    social.changeTrust(gameData, k.removePrefix("percaya_"), v.toInt())
                    syncSocialVars()
                }
                else -> lifeEffects[k] = v
            }
        }
        val before = life.money
        addExtraVars(life.applyEffects(lifeEffects))
        val diff = life.money - before
        if (diff != 0L) {
            logMoney(desc, diff)
            notice((if (diff > 0) "💰 +" else "💸 -") + rupiah(kotlin.math.abs(diff)))
        }
        refreshLife()
    }

    private fun runEvent(e: GameData.EventDef, done: () -> Unit) {
        if (e.onceOnly) vars["kejadian_${e.id}"] = "1"
        vars["kejadian_terakhir"] = e.id
        e.note?.let { phoneData.notes += PhoneState.Note(life.day, fill(it)) }
        hideDialog()
        fun finish() {
            if (checkDeath()) return
            done()
        }
        afterTap = {
            if (e.effects.isNotEmpty()) applyEventEffects(e.effects, e.title)
            if (life.dead) {
                checkDeath()
            } else if (e.options.isEmpty()) {
                finish()
            } else {
                showButtons(e.options.map { fill(it.label) }) { i ->
                    val o = e.options[i]
                    applyEventEffects(o.effects, e.title)
                    if (life.dead) {
                        checkDeath()
                    } else if (o.result.isNotEmpty()) {
                        afterTap = { finish() }
                        showLine(null, fill(o.result))
                    } else {
                        finish()
                    }
                }
            }
        }
        showLine(null, "${e.icon} ${e.title} — ${fill(e.text)}")
    }

    /** Ujian: 4 soal. Nilai sekolah naik/turun sesuai jumlah benar. */
    private fun runExam(def: GameData.JobDef, done: () -> Unit) {
        val job = life.jobs.firstOrNull { it.id == def.id } ?: return done()
        val rnd = kotlin.random.Random
        val akademik = vars["akademik"]?.toIntOrNull() ?: 0
        val questions = List(4) { i ->
            when (i % 4) {
                0 -> { val x = rnd.nextInt(15, 90); val y = rnd.nextInt(15, 90); "Berapa $x + $y?" to (x + y) }
                1 -> { val x = rnd.nextInt(4, 15); val y = rnd.nextInt(4, 15); "Berapa $x × $y?" to (x * y) }
                2 -> { val x = rnd.nextInt(40, 120); val y = rnd.nextInt(5, 39); "Berapa $x − $y?" to (x - y) }
                else -> { val st = rnd.nextInt(2, 9); val step = rnd.nextInt(2, 8)
                    "Lanjutkan deret: $st, ${st + step}, ${st + 2 * step}, …" to (st + 3 * step) }
            }
        }
        var correct = 0
        fun ask(i: Int) {
            if (i >= questions.size) {
                // Belajar (akademik) membantu nilai
                val score = (correct * 25 + akademik).coerceAtMost(100)
                val change = when {
                    score >= 80 -> 10
                    score >= 60 -> 4
                    score >= 40 -> -4
                    else -> -10
                }
                job.performance = (job.performance + change).coerceIn(0, 100)
                vars["nilai_ujian_terakhir"] = score.toString()
                addExtraVars(life.applyEffects(mapOf("energi" to -10L, "kewarasan" to (if (change > 0) 6L else -8L))))
                refreshLife()
                afterTap = { done() }
                showLine(null, "📊 Nilai ujianmu: $score (${correct} dari 4 benar). Nilai ${def.name} sekarang ${job.performance}." +
                    (if (score < 40) " Kamu harus lebih giat belajar!" else ""))
                return
            }
            val (q, ans) = questions[i]
            showLine(null, "📝 Ujian ${def.name} — Soal ${i + 1}/4: $q", record = false, instant = true)
            val opts = listOf(ans, ans + rnd.nextInt(1, 6), ans - rnd.nextInt(1, 6), ans + rnd.nextInt(7, 15)).shuffled()
            showButtons(opts.map { it.toString() }) { k ->
                if (opts[k] == ans) correct++
                ask(i + 1)
            }
        }
        afterTap = { ask(0) }
        showLine(null, "📝 Hari ini ujian di ${def.name}! Jawab soal-soal berikut. (Rajin belajar menambah nilai.)")
    }

    /** Akhir tahun ajaran: naik kelas, tinggal kelas, atau lulus. */
    private fun runSchoolYear(def: GameData.JobDef, done: () -> Unit) {
        val job = life.jobs.firstOrNull { it.id == def.id } ?: return done()
        val kelas = vars["kelas"]?.toIntOrNull() ?: 10
        val pass = job.performance >= 40 && job.absences < 6
        val text = when {
            !pass -> {
                vars["tinggal_kelas"] = ((vars["tinggal_kelas"]?.toIntOrNull() ?: 0) + 1).toString()
                addExtraVars(life.applyEffects(mapOf("kewarasan" to -20L)))
                "📄 Rapor dibagikan... Nilaimu terlalu rendah. Kamu tinggal kelas di kelas $kelas."
            }
            kelas >= 12 -> {
                life.jobs.remove(job)
                vars["lulus_sma"] = "1"
                phoneData.docs += "ijazah_sma"
                addExtraVars(life.applyEffects(mapOf("kewarasan" to 20L)))
                "🎓 Selamat! Kamu LULUS dari ${def.name}! Ijazah SMA sudah di tanganmu."
            }
            else -> {
                vars["kelas"] = (kelas + 1).toString()
                job.performance = 50
                job.absences = 0
                addExtraVars(life.applyEffects(mapOf("kewarasan" to 10L)))
                "📗 Rapor dibagikan. Kamu naik ke kelas ${kelas + 1}!"
            }
        }
        refreshLife()
        afterTap = { done() }
        showLine(null, text)
    }

    // ================================================================== aplikasi HP

    private val phoneApps by lazy {
        listOf(
            PhoneScreen.App("chat", "💬", "Chat", 0xFF34C759.toInt()),
            PhoneScreen.App("kontak", "👥", "Kontak", 0xFF5E8BFF.toInt()),
            PhoneScreen.App("kalender", "📅", "Kalender", 0xFFFF9F0A.toInt()),
            PhoneScreen.App("lowongan", "💼", "Lowongan", 0xFF8E5CF7.toInt()),
            PhoneScreen.App("dompet", "👛", "Dompet", 0xFF2EB5A0.toInt()),
            PhoneScreen.App("catatan", "📝", "Catatan", 0xFFE0A33B.toInt()),
            PhoneScreen.App("sosmed", "📷", "Sosmed", 0xFFE1306C.toInt()),
        )
    }

    private fun logMoney(desc: String, amount: Long) = phoneData.logTx(life.day, desc, amount)

    private fun openPhoneFromSheet() {
        phoneIcon.ring(false)
        phone.open()
    }

    private fun refreshPhoneHome() {
        val actionable = phoneData.apps.count { it.stage == "tes" || it.stage == "wawancara" }
        val apptsToday = phoneData.appts.count { it.day == life.day }
        val canSearch = if (vars["sosmed_hari"] != life.day.toString()) 1 else 0
        phone.setApps(phoneApps, mapOf("chat" to phoneData.unreadTotal, "lowongan" to actionable,
            "kalender" to apptsToday, "sosmed" to canSearch))
    }

    private fun renderApp(id: String) {
        when (id) {
            "chat" -> renderChatList()
            "kontak" -> renderContacts()
            "kalender" -> renderCalendar()
            "lowongan" -> renderJobs()
            "dompet" -> renderWallet()
            "catatan" -> renderNotes()
            "sosmed" -> renderSosmed()
        }
    }

    // ---------- komponen tampilan HP (tema terang) ----------

    private fun pText(text: String, size: Float = 13.5f, color: Int = 0xFF2A2A30.toInt(), bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setLineSpacing(0f, 1.12f)
        }

    private fun pSection(text: String) = pText(text, 12f, 0xFF8A8A93.toInt(), true).apply {
        letterSpacing = 0.05f
        val d = resources.displayMetrics.density
        setPadding((4 * d).toInt(), (12 * d).toInt(), 0, (6 * d).toInt())
    }

    private fun pCard(title: String, sub: String? = null, right: String? = null, badge: Int = 0, onClick: (() -> Unit)? = null): View {
        val d = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding((12 * d).toInt(), (10 * d).toInt(), (12 * d).toInt(), (10 * d).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 14 * d
                setColor(0xFFFFFFFF.toInt())
            }
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply { bottomMargin = (6 * d).toInt() }
            onClick?.let { cb -> setOnClickListener { cb() } }
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(pText(title, 14.5f, 0xFF16161A.toInt(), true))
        if (!sub.isNullOrEmpty()) col.addView(pText(sub, 12.5f, 0xFF6B6B73.toInt()))
        row.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        if (right != null) row.addView(pText(right, 12f, 0xFF8A8A93.toInt()))
        if (badge > 0) row.addView(TextView(this).apply {
            text = badge.toString()
            textSize = 11f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFFFF3B4E.toInt())
            }
        }, LinearLayout.LayoutParams((20 * d).toInt(), (20 * d).toInt()).apply { marginStart = (6 * d).toInt() })
        return row
    }

    /** Tombol di bagian bawah aplikasi. Satu baris bisa berisi beberapa tombol. */
    private fun pButtons(vararg buttons: Triple<String, Boolean, () -> Unit>) {
        val d = resources.displayMetrics.density
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttons.forEachIndexed { i, (label, enabled, action) ->
            row.addView(TextView(this).apply {
                text = label
                gravity = android.view.Gravity.CENTER
                textSize = 12.5f
                setPadding((6 * d).toInt(), (8 * d).toInt(), (6 * d).toInt(), (8 * d).toInt())
                setTextColor(if (enabled) 0xFFFFFFFF.toInt() else 0xFF9A9AA2.toInt())
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = 18 * d
                    setColor(if (enabled) 0xFF2F6DB5.toInt() else 0xFFE4E4E8.toInt())
                }
                if (enabled) setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                if (i > 0) marginStart = (6 * d).toInt()
            })
        }
        phone.footer.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { topMargin = (6 * d).toInt() })
    }

    private fun bubble(text: String, mine: Boolean): View {
        val d = resources.displayMetrics.density
        val wrap = LinearLayout(this).apply {
            gravity = if (mine) android.view.Gravity.END else android.view.Gravity.START
            setPadding(0, (3 * d).toInt(), 0, (3 * d).toInt())
        }
        wrap.addView(pText(text, 13.5f, if (mine) 0xFFFFFFFF.toInt() else 0xFF16161A.toInt()).apply {
            maxWidth = (200 * d).toInt()
            setPadding((12 * d).toInt(), (8 * d).toInt(), (12 * d).toInt(), (8 * d).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 16 * d
                setColor(if (mine) 0xFF2F9E5B.toInt() else 0xFFFFFFFF.toInt())
            }
        })
        return wrap
    }

    private fun dayLabel(dayIndex: Int): String {
        val diff = dayIndex - life.day
        val d = if (life.startEpochDay != 0L) java.time.LocalDate.ofEpochDay(life.startEpochDay + dayIndex) else null
        val date = d?.let { "${LifeState.WEEKDAYS[it.dayOfWeek.value - 1]}, ${it.dayOfMonth} ${LifeState.MONTHS[it.monthValue - 1].take(3)}" }
            ?: "Hari ke-${dayIndex + 1}"
        return when (diff) {
            0 -> "Hari ini"
            1 -> "Besok"
            -1 -> "Kemarin"
            else -> date
        }
    }

    // ---------- Chat ----------

    private fun renderChatList() {
        phone.clearApp()
        phone.setTitle("💬  Chat")
        phone.backHandler = null
        val threads = phoneData.threads.values.filter { it.msgs.isNotEmpty() }.reversed()
        if (threads.isEmpty()) phone.content.addView(pText("Belum ada pesan.", 13f, 0xFF8A8A93.toInt()))
        threads.sortedByDescending { it.msgs.lastOrNull()?.day ?: 0 }.forEach { t ->
            val last = t.msgs.last()
            phone.content.addView(pCard(
                t.title,
                (if (last.fromMe) "Kamu: " else "") + last.text,
                dayLabel(last.day),
                t.unread,
            ) { renderThread(t.key) })
        }
        phone.scrollToTop()
    }

    private fun renderThread(key: String) {
        val t = phoneData.threads[key] ?: return
        t.unread = 0
        phone.clearApp()
        phone.setTitle(t.title)
        phone.backHandler = { renderChatList() }
        var lastDay = -999
        t.msgs.forEach { m ->
            if (m.day != lastDay) {
                lastDay = m.day
                phone.content.addView(pText(dayLabel(m.day), 11f, 0xFF8A8A93.toInt()).apply {
                    gravity = android.view.Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })
            }
            phone.content.addView(bubble(m.text, m.fromMe))
        }
        val c = gameData.characters[key]
        when {
            c != null -> {
                val canReply = t.pendingSince >= 0
                pButtons(
                    Triple("😊 Balas hangat", canReply) { replyChat(c, t, warm = true) },
                    Triple("Balas singkat", canReply) { replyChat(c, t, warm = false) },
                )
                val place = (vars["lokasi"] ?: "desa").lowercase()
                val sameArea = "semua" in c.places || place in c.places
                val canMeet = sameArea && c.meetable && social.rel(c).met && phoneData.appts.none { it.charId == c.id }
                pButtons(Triple(if (sameArea) "📅 Ajak ketemu" else "📍 Beda tempat — tidak bisa ketemu", canMeet) { chooseMeetTime(c, t) })
            }
            key == "Nomor Tidak Dikenal" -> {
                val replied = vars["misterius_dibalas"] == "1"
                pButtons(
                    Triple("Siapa kamu sebenarnya?", !replied) { replyMystery(t, "Siapa kamu sebenarnya?") },
                    Triple("Aku ingat janji kita.", !replied) { replyMystery(t, "Kamu... aku masih ingat janji kita di dermaga.") },
                )
            }
        }
        phone.scrollToBottom()
    }

    private fun replyChat(c: GameData.CharacterDef, t: PhoneState.Thread, warm: Boolean) {
        val mine = if (warm) listOf("Hehe, iya! Kamu sendiri lagi apa?", "Makasih udah ngabarin 😊", "Aku juga kangen ngobrol sama kamu.").random()
            else listOf("Ok.", "Iya.", "Sip.").random()
        t.msgs += PhoneState.Msg(true, mine, life.day)
        t.pendingSince = -1
        if (t.repliedDay != life.day) {
            t.repliedDay = life.day
            announce(social.changeLove(gameData, c.id, if (warm) 3 else 1))
        }
        val replies = c.text["chat_balas"]?.split('|')?.map { it.trim() }?.filter { it.isNotEmpty() }
        replies?.randomOrNull()?.let { t.msgs += PhoneState.Msg(false, fill(it), life.day) }
        renderThread(t.key)
    }

    private fun replyMystery(t: PhoneState.Thread, text: String) {
        t.msgs += PhoneState.Msg(true, text, life.day)
        vars["misterius_dibalas"] = "1"
        notice("📨 Pesan terkirim... tapi belum ada balasan.")
        renderThread(t.key)
    }

    private fun chooseMeetTime(c: GameData.CharacterDef, t: PhoneState.Thread) {
        phone.footer.removeAllViews()
        val options = mutableListOf<Pair<Int, LifeState.Slot>>()
        for (dayOffset in 0..1) {
            LifeState.Slot.values().forEach { sl ->
                if (dayOffset == 0 && sl.ordinal <= life.slot.ordinal) return@forEach
                if ("semua" in c.slots || sl.label.lowercase() in c.slots) options += (life.day + dayOffset) to sl
            }
        }
        if (options.isEmpty()) {
            notice("${c.name} tidak punya waktu luang dalam dua hari ini.")
            renderThread(t.key)
            return
        }
        options.take(4).chunked(2).forEach { pair ->
            val btns = pair.map { (dy, sl) ->
                Triple("${dayLabel(dy)} ${sl.label.lowercase()}", true) { inviteMeet(c, t, dy, sl) }
            }
            pButtons(*btns.toTypedArray())
        }
        pButtons(Triple("Batal", true) { renderThread(t.key) })
    }

    private fun inviteMeet(c: GameData.CharacterDef, t: PhoneState.Thread, day: Int, slot: LifeState.Slot) {
        val label = "${dayLabel(day)} ${slot.label.lowercase()}"
        t.msgs += PhoneState.Msg(true, "Ketemu yuk, $label?", life.day)
        val blocker = social.blockedBy(gameData, c)
        val r = social.rel(c)
        val reply = when {
            blocker != null -> fill(c.text["larangan"] ?: "Maaf, nggak bisa.")
            r.love < 30 -> fill(c.text["ajak_tidak"] ?: "Maaf, lagi sibuk.")
            else -> {
                phoneData.appts += PhoneState.Appt(c.id, day, slot.name)
                notice("📅 Janji ketemu ${c.name}: $label")
                fill(c.text["ajak_ya"] ?: "Oke!")
            }
        }
        t.msgs += PhoneState.Msg(false, reply, life.day)
        renderThread(t.key)
    }

    // ---------- Kontak ----------

    private fun renderContacts() {
        phone.clearApp()
        phone.setTitle("👥  Kontak")
        phone.backHandler = null
        phone.content.addView(playerProfileRow(dark = false))
        val known = gameData.characters.values.filter { social.rel(it).met }
        if (known.isEmpty()) phone.content.addView(pText("Belum ada kontak.", 13f, 0xFF8A8A93.toInt()))
        known.forEach { c ->
            val r = social.rel(c)
            val love = if (social.isPartner(c)) "❤️ ${r.romance}" else "💛 ${r.love}"
            phone.content.addView(pCard(c.name, "${social.stageOf(c).label} · $love · 🤝 ${r.trust}", null) { renderContact(c) })
        }
        phone.scrollToTop()
    }

    private fun renderContact(c: GameData.CharacterDef) {
        phone.clearApp()
        phone.setTitle(c.name)
        phone.backHandler = { renderContacts() }
        val d = resources.displayMetrics.density
        val r = social.rel(c)
        val photo = charPhoto(c.id)
        if (photo != null) {
            phone.content.addView(android.widget.ImageView(this).apply {
                setImageBitmap(photo)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                background = android.graphics.drawable.GradientDrawable().apply { shape = android.graphics.drawable.GradientDrawable.OVAL }
                clipToOutline = true
            }, LinearLayout.LayoutParams((96 * d).toInt(), (96 * d).toInt()).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                bottomMargin = (8 * d).toInt()
            })
        }
        phone.content.addView(pText(c.role, 13f, 0xFF6B6B73.toInt()).apply { gravity = android.view.Gravity.CENTER })
        phone.content.addView(pSection("HUBUNGAN"))
        val love = if (social.isPartner(c)) "❤️ Cinta ${r.romance}%" else "💛 Cinta ${r.love}%"
        phone.content.addView(pCard(social.stageOf(c).label, "$love   ·   🤝 Kepercayaan ${r.trust}%"))
        val playerAge = playerAge()
        phone.content.addView(pText("Usia ${social.ageOf(c, playerAge)} tahun", 12.5f, 0xFF6B6B73.toInt()))

        fun knownList(prefix: String): String {
            val names = r.known.filter { it.startsWith(prefix) }.map { k ->
                val id = k.substring(3)
                if (k[1] == 't') gameData.topics[id]?.name ?: id else gameData.items[id]?.name ?: id
            }
            return if (names.isEmpty()) "Belum tahu" else names.joinToString(", ")
        }
        phone.content.addView(pSection("YANG KAMU KETAHUI"))
        phone.content.addView(pCard("💚 Topik disukai", knownList("+t:")))
        phone.content.addView(pCard("💔 Topik tidak disukai", knownList("-t:")))
        phone.content.addView(pCard("🎁 Hadiah disukai", knownList("+i:")))
        phone.content.addView(pCard("🚫 Hadiah tidak disukai", knownList("-i:")))
        pButtons(Triple("💬 Kirim pesan", true) {
            phoneData.thread(c.id, c.name)
            renderThread(c.id)
        })
        phone.scrollToTop()
    }

    // ---------- Kalender ----------

    private fun renderCalendar() {
        phone.clearApp()
        phone.setTitle("📅  Kalender")
        phone.backHandler = null
        phone.content.addView(pCard("Hari ini", "${life.dateText} · ${life.timeText} ${life.slot.icon}"))

        val events = mutableListOf<Pair<Int, String>>()
        phoneData.appts.forEach { a ->
            val name = gameData.characters[a.charId]?.name ?: a.charId
            val sl = runCatching { LifeState.Slot.valueOf(a.slot).label.lowercase() }.getOrDefault(a.slot)
            events += a.day to "🤝 Janji ketemu $name ($sl)"
        }
        if (life.rentDueDay >= 0) events += life.rentDueDay to "🏠 Bayar sewa kos ${rupiah(life.rent)}"
        if (life.allowance > 0 && life.allowanceNextDay >= 0) events += life.allowanceNextDay to "💌 Kiriman keluarga ${rupiah(life.allowance)}"
        life.jobs.forEach { j ->
            val def = gameData.jobs[j.id] ?: return@forEach
            if (def.kind == "tetap") {
                val next = life.day + (30 - ((life.day - j.startDay) % 30)).let { if (it == 0) 30 else it }
                events += next to "💼 Gajian ${def.name}"
            }
        }
        phoneData.apps.filter { it.stage == "tes" || it.stage == "wawancara" || it.stage == "dikirim" }.forEach { a ->
            val name = gameData.jobs[a.jobId]?.name ?: a.jobId
            val what = when (a.stage) { "tes" -> "Batas tes"; "wawancara" -> "Batas wawancara"; else -> "Pengumuman berkas" }
            events += a.dueDay to "📨 $what: $name"
        }
        phoneData.pendingDocs.forEach { (id, day) -> events += day to "📁 ${gameData.documents[id]?.name ?: id} jadi" }

        phone.content.addView(pSection("AKAN DATANG"))
        val upcoming = events.filter { it.first >= life.day }.sortedBy { it.first }
        if (upcoming.isEmpty()) phone.content.addView(pText("Tidak ada acara.", 13f, 0xFF8A8A93.toInt()))
        upcoming.forEach { (day, text) -> phone.content.addView(pCard(text, null, dayLabel(day))) }

        if (life.jobs.isNotEmpty()) {
            phone.content.addView(pSection("JADWAL RUTIN"))
            life.jobs.forEach { j ->
                val def = gameData.jobs[j.id] ?: return@forEach
                phone.content.addView(pCard("${def.icon} ${def.name}", "${daysText(def.days)} · ${def.hoursText}"))
            }
        }
        phone.scrollToTop()
    }

    // ---------- Dompet ----------

    private fun renderWallet() {
        phone.clearApp()
        phone.setTitle("👛  Dompet")
        phone.backHandler = null
        phone.content.addView(pText("Saldo", 12f, 0xFF8A8A93.toInt()))
        phone.content.addView(pText(rupiah(life.money), 28f, 0xFF16161A.toInt(), true))
        if (life.rentDueDay >= 0) phone.content.addView(pCard("🏠 Sewa kos berikutnya", rupiah(life.rent), dayLabel(life.rentDueDay)))
        if (life.arrears > 0) phone.content.addView(pCard("⚠️ Tunggakan sewa", "${life.arrears}× — 2× berarti diusir!"))
        phone.content.addView(pSection("RIWAYAT"))
        if (phoneData.tx.isEmpty()) phone.content.addView(pText("Belum ada transaksi.", 13f, 0xFF8A8A93.toInt()))
        phoneData.tx.reversed().forEach { t ->
            phone.content.addView(pCard(t.desc, dayLabel(t.day), (if (t.amount >= 0) "+" else "−") + rupiah(kotlin.math.abs(t.amount))))
        }
        phone.scrollToTop()
    }

    // ---------- Catatan ----------

    private fun renderNotes() {
        phone.clearApp()
        phone.setTitle("📝  Catatan")
        phone.backHandler = null
        if (phoneData.notes.isEmpty()) phone.content.addView(pText("Belum ada catatan.", 13f, 0xFF8A8A93.toInt()))
        phoneData.notes.reversed().forEach { n -> phone.content.addView(pCard(n.text, dayLabel(n.day))) }
        phone.scrollToTop()
    }

    // ---------- Sosmed: kabar teman & mencari sahabat ----------

    private fun renderSosmed() {
        phone.clearApp()
        phone.setTitle("📷  Sosmed")
        phone.backHandler = null
        val friend = vars["nama_sahabat"] ?: "sahabat"
        val searchedToday = vars["sosmed_hari"] == life.day.toString()
        val count = vars["sosmed_cari"]?.toIntOrNull() ?: 0
        phone.content.addView(pSection("CARI ORANG"))
        phone.content.addView(pCard(
            "🔍  Cari \"$friend\"",
            if (searchedToday) "Sudah mencari hari ini. Coba lagi besok."
            else "Telusuri akun, foto, dan komentar lama (1× sehari)",
            if (count > 0) "${count}×" else null,
        ) { if (!searchedToday) searchFriend() })
        val found = gameData.posts.filter { it.search && vars["sosmed_${it.id}"] == "1" }
        found.forEach { p -> phone.content.addView(pCard("📌 Temuan", fill(p.text))) }

        phone.content.addView(pSection("BERANDA"))
        val feed = gameData.posts.filter { p ->
            !p.search && p.minDay <= life.day &&
                (p.author == null || gameData.characters[p.author]?.let { social.rel(it).met } == true) &&
                (p.condition == null || checkCondition(p.condition))
        }.sortedByDescending { it.minDay }
        if (feed.isEmpty()) phone.content.addView(pText("Belum ada unggahan dari teman-temanmu.", 13f, 0xFF8A8A93.toInt()))
        feed.forEach { p ->
            val author = p.author?.let { gameData.characters[it] }
            val liked = vars["suka_${p.id}"] == "1"
            val ago = (life.day - p.minDay).let { if (it <= 0) "hari ini" else "$it hari lalu" }
            phone.content.addView(pCard(
                author?.name ?: "Akun",
                fill(p.text) + "\n" + ago,
                if (liked) "❤️" else "🤍",
            ) {
                if (!liked && author != null) {
                    vars["suka_${p.id}"] = "1"
                    announce(social.changeLove(gameData, author.id, 1))
                    renderSosmed()
                }
            })
        }
        phone.scrollToTop()
    }

    /** Satu kali pencarian per hari. Temuan baru muncul pada pencarian ke-N (data/sosmed.txt). */
    private fun searchFriend() {
        val count = (vars["sosmed_cari"]?.toIntOrNull() ?: 0) + 1
        vars["sosmed_cari"] = count.toString()
        vars["sosmed_hari"] = life.day.toString()
        val hit = gameData.posts.filter { p ->
            p.search && vars["sosmed_${p.id}"] != "1" && p.need <= count &&
                (p.condition == null || checkCondition(p.condition))
        }.minByOrNull { it.need }
        if (hit != null) {
            vars["sosmed_${hit.id}"] = "1"
            if (hit.clue != 0) {
                val now = vars["petunjuk_sahabat"]?.toIntOrNull() ?: 0
                vars["petunjuk_sahabat"] = (now + hit.clue).toString()
            }
            hit.note?.let { phoneData.notes += PhoneState.Note(life.day, fill(it)) }
            notice("📌 Temuan baru di Sosmed!")
        } else {
            notice(listOf(
                "🔍 Tidak ada hasil baru hari ini...",
                "🔍 Puluhan akun bernama sama. Tak satu pun dia.",
                "🔍 Akun-akun itu bukan dia. Besok coba lagi.",
            ).random())
        }
        refreshPhoneHome()
        renderSosmed()
    }

    // ---------- Lowongan: lamaran, dokumen, tes, wawancara ----------

    private fun stageLabel(stage: String) = when (stage) {
        "dikirim" -> "⏳ Berkas sedang diperiksa"
        "tes" -> "📝 Kerjakan tes"
        "wawancara" -> "🗣️ Ikuti wawancara"
        "diterima" -> "✅ Diterima"
        "ditolak" -> "❌ Ditolak"
        else -> "⌛ Gugur (lewat batas waktu)"
    }

    private fun renderJobs() {
        phone.clearApp()
        phone.setTitle("💼  Lowongan")
        phone.backHandler = null
        val place = vars["lokasi"] ?: "desa"

        if (phoneData.apps.isNotEmpty()) {
            phone.content.addView(pSection("LAMARANKU"))
            phoneData.apps.takeLast(6).reversed().forEach { a ->
                val def = gameData.jobs[a.jobId] ?: return@forEach
                val due = if (a.stage in setOf("dikirim", "tes", "wawancara")) "Batas: ${dayLabel(a.dueDay)}" else null
                phone.content.addView(pCard("${def.icon} ${def.name}", stageLabel(a.stage) + (due?.let { "\n$it" } ?: ""), null,
                    if (a.stage == "tes" || a.stage == "wawancara") 1 else 0) {
                    when (a.stage) {
                        "tes" -> startTest(a)
                        "wawancara" -> startInterview(a)
                        else -> renderJobDetail(def)
                    }
                })
            }
        }
        phone.content.addView(pSection("LOWONGAN DI SEKITARMU"))
        val list = gameData.jobs.values.filter { j ->
            j.kind != "sekolah" && (j.way == "lamar" || j.way == "langsung") &&
                ("semua" in j.places || place in j.places)
        }
        if (list.isEmpty()) phone.content.addView(pText("Belum ada lowongan.", 13f, 0xFF8A8A93.toInt()))
        list.forEach { j ->
            val held = life.jobs.any { it.id == j.id }
            phone.content.addView(pCard("${j.icon} ${j.name}",
                "${j.kindLabel} · ${rupiah(j.pay)}" + (if (j.kind == "tetap") "/30 hari" else "/kerja") +
                    (if (held) "\n✅ Pekerjaanmu sekarang" else ""),
                if (j.way == "lamar") "Lamar" else "Datang langsung") { renderJobDetail(j) })
        }
        pButtons(Triple("📁 Dokumen saya", true) { renderDocs() })
        phone.scrollToTop()
    }

    private fun renderJobDetail(j: GameData.JobDef) {
        phone.clearApp()
        phone.setTitle(j.name)
        phone.backHandler = { renderJobs() }
        phone.content.addView(pCard("${j.icon} ${j.kindLabel}",
            "${daysText(j.days)} · ${j.hoursText}\n💰 ${rupiah(j.pay)}" + (if (j.kind == "tetap") " per 30 hari" else " per kerja")))
        if (j.desc.isNotEmpty()) phone.content.addView(pText(j.desc, 13f).apply { setPadding(4, 4, 4, 8) })
        phone.content.addView(pSection("SYARAT"))
        val ageOk = playerAge() >= j.minAge
        phone.content.addView(pCard((if (ageOk) "✅" else "❌") + " Usia minimal ${j.minAge} tahun", "Usiamu ${playerAge()} tahun"))
        j.documents.forEach { docId ->
            val has = docId in phoneData.docs
            phone.content.addView(pCard((if (has) "✅ " else "❌ ") + (gameData.documents[docId]?.name ?: docId)))
        }
        if (j.hasTest) phone.content.addView(pCard("📝 Ada tes tertulis", "Dipengaruhi kemampuan akademik"))
        j.referrer?.let { gameData.characters[it] }?.takeIf { social.rel(it).met }?.let { ref ->
            val r = social.rel(ref)
            val ok = r.love >= 60 && r.trust >= 60
            phone.content.addView(pCard("🤝 Orang dalam: ${ref.name}",
                if (ok) "Bisa merekomendasikanmu — langsung ke wawancara" else "Perlu Cinta & Kepercayaan ≥ 60 (sekarang ${r.love} / ${r.trust})"))
        }
        val applied = phoneData.apps.any { it.jobId == j.id && it.stage in setOf("dikirim", "tes", "wawancara") }
        val held = life.jobs.any { it.id == j.id }
        when {
            held -> phone.content.addView(pText("Kamu sudah bekerja di sini.", 13f, 0xFF2F9E5B.toInt(), true))
            applied -> phone.content.addView(pText("Lamaranmu sedang diproses.", 13f, 0xFF8E5CF7.toInt(), true))
            j.way == "langsung" -> phone.content.addView(pText("Pekerjaan ini tidak perlu berkas. Datangi langsung lewat 🔎 Cari kerja saat waktu bebas.", 13f, 0xFF6B6B73.toInt()))
            else -> {
                val missing = j.documents.filter { it !in phoneData.docs }
                pButtons(Triple(if (missing.isEmpty() && ageOk) "📨 Kirim lamaran" else "Syarat belum lengkap", missing.isEmpty() && ageOk) {
                    sendApplication(j)
                })
            }
        }
        phone.scrollToTop()
    }

    private fun hrdMessage(j: GameData.JobDef, text: String) = receiveMessage("HRD · ${j.name}", text)

    private fun sendApplication(j: GameData.JobDef) {
        val ref = j.referrer?.let { gameData.characters[it] }
        val insider = ref != null && social.rel(ref).let { it.met && it.love >= 60 && it.trust >= 60 }
        val app = PhoneState.Application(
            j.id,
            stage = if (insider) "wawancara" else "dikirim",
            dueDay = life.day + if (insider) 3 else 2,
            viaInsider = insider,
        )
        phoneData.apps += app
        if (insider && ref != null) {
            hrdMessage(j, "Atas rekomendasi ${ref.name}, kamu langsung kami undang wawancara. Silakan ikuti sebelum ${dayLabel(app.dueDay)} lewat aplikasi Lowongan.")
        } else {
            notice("📨 Lamaran ke ${j.name} terkirim. Tunggu kabar 2 hari lagi.")
        }
        renderJobs()
    }

    /** Proses harian: pemeriksaan berkas, batas waktu, dokumen jadi, chat dari karakter, chat yang diabaikan. */
    private fun processPhoneDaily() {
        phoneData.apps.filter { it.stage in setOf("dikirim", "tes", "wawancara") }.forEach { a ->
            val j = gameData.jobs[a.jobId] ?: return@forEach
            when {
                a.stage == "dikirim" && life.day >= a.dueDay -> {
                    val skill = (vars["akademik"]?.toIntOrNull() ?: 0) + (vars["komunikasi"]?.toIntOrNull() ?: 0)
                    val chance = (85 - j.difficulty / 2 + skill * 2).coerceIn(10, 95)
                    if (kotlin.random.Random.nextInt(100) < chance) {
                        a.stage = if (j.hasTest) "tes" else "wawancara"
                        a.dueDay = life.day + 3
                        hrdMessage(j, "Selamat, kamu lolos seleksi berkas! " +
                            (if (j.hasTest) "Kerjakan tes" else "Ikuti wawancara") + " di aplikasi Lowongan sebelum ${dayLabel(a.dueDay)}.")
                    } else {
                        a.stage = "ditolak"
                        hrdMessage(j, "Mohon maaf, berkas lamaranmu belum sesuai dengan kebutuhan kami.")
                    }
                }
                (a.stage == "tes" || a.stage == "wawancara") && life.day > a.dueDay -> {
                    a.stage = "gugur"
                    hrdMessage(j, "Batas waktu sudah lewat. Lamaranmu kami anggap gugur.")
                }
            }
        }
        phoneData.pendingDocs.filter { it.value <= life.day }.keys.toList().forEach { id ->
            phoneData.pendingDocs.remove(id)
            phoneData.docs += id
            notice("📁 ${gameData.documents[id]?.name ?: id} sudah jadi")
        }
        // Chat yang tidak dibalas 2 hari = mengabaikan
        phoneData.threads.values.forEach { t ->
            val c = gameData.characters[t.key] ?: return@forEach
            if (t.pendingSince >= 0 && life.day - t.pendingSince >= 2) {
                t.pendingSince = -1
                social.behave(gameData, c.id, "abaikan_chat")
                syncSocialVars()
            }
        }
        // Karakter kadang mengirim pesan
        var sent = 0
        gameData.characters.values.shuffled().forEach { c ->
            if (sent >= 2) return@forEach
            val r = social.rel(c)
            if (!r.met || r.love < 25) return@forEach
            val lines = c.text["chat"]?.split('|')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: return@forEach
            val chance = 0.12f + r.love / 400f
            if (kotlin.random.Random.nextFloat() < chance) {
                receiveMessage(c.name, fill(lines.random()))
                sent++
            }
        }
    }

    private fun renderDocs() {
        phone.clearApp()
        phone.setTitle("📁  Dokumen")
        phone.backHandler = { renderJobs() }
        gameData.documents.values.forEach { doc ->
            val has = doc.id in phoneData.docs
            val pending = phoneData.pendingDocs[doc.id]
            val status = when {
                has -> "✅ Sudah punya"
                pending != null -> "⏳ Jadi ${dayLabel(pending)}"
                doc.minAge > 90 -> "Didapat lewat cerita"
                playerAge() < doc.minAge -> "Usia minimal ${doc.minAge}"
                else -> "Urus: ${rupiah(doc.cost)}" + (if (doc.days > 0) " · ${doc.days} hari" else "")
            }
            val canProcess = !has && pending == null && doc.minAge <= 90 && playerAge() >= doc.minAge && life.money >= doc.cost
            phone.content.addView(pCard(doc.name, doc.desc + "\n" + status, if (canProcess) "Urus ›" else null) {
                if (canProcess) processDoc(doc)
            })
        }
        phone.scrollToTop()
    }

    private fun processDoc(doc: GameData.DocDef) {
        life.money -= doc.cost
        logMoney("Urus ${doc.name}", -doc.cost)
        if (doc.days <= 0) {
            phoneData.docs += doc.id
            notice("📁 ${doc.name} selesai")
        } else {
            phoneData.pendingDocs[doc.id] = life.day + doc.days
            notice("📁 ${doc.name} sedang diurus, jadi ${dayLabel(life.day + doc.days)}")
        }
        refreshLife()
        renderDocs()
    }

    /** Tes tertulis: 3 soal hitungan & logika. */
    private fun startTest(a: PhoneState.Application) {
        val j = gameData.jobs[a.jobId] ?: return
        val rnd = kotlin.random.Random
        val questions = List(3) { i ->
            when (i) {
                0 -> { val x = rnd.nextInt(12, 60); val y = rnd.nextInt(12, 60); "Berapa $x + $y?" to (x + y) }
                1 -> { val x = rnd.nextInt(3, 13); val y = rnd.nextInt(3, 13); "Berapa $x × $y?" to (x * y) }
                else -> { val st = rnd.nextInt(2, 9); val step = rnd.nextInt(2, 7)
                    "Lanjutkan deret: ${st}, ${st + step}, ${st + 2 * step}, …" to (st + 3 * step) }
            }
        }
        var correct = 0
        fun ask(i: Int) {
            phone.clearApp()
            phone.setTitle("📝 Tes · ${j.name}")
            phone.backHandler = null
            if (i >= questions.size) {
                val pass = correct >= 2 || (correct >= 1 && (vars["akademik"]?.toIntOrNull() ?: 0) >= 10)
                a.score += correct * 10
                if (pass) {
                    a.stage = "wawancara"
                    a.dueDay = life.day + 3
                    hrdMessage(j, "Hasil tesmu bagus ($correct/3 benar). Ikuti wawancara sebelum ${dayLabel(a.dueDay)}.")
                } else {
                    a.stage = "ditolak"
                    hrdMessage(j, "Mohon maaf, nilai tesmu ($correct/3) belum memenuhi syarat.")
                }
                life.applyEffects(mapOf("energi" to -8L, "kewarasan" to (if (pass) 3L else -5L)))
                refreshLife()
                phone.content.addView(pText("Tes selesai: $correct dari 3 benar.", 16f, 0xFF16161A.toInt(), true))
                pButtons(Triple("Kembali ke Lowongan", true) { renderJobs() })
                return
            }
            val (q, ans) = questions[i]
            phone.content.addView(pText("Soal ${i + 1} dari 3", 12f, 0xFF8A8A93.toInt()))
            phone.content.addView(pText(q, 18f, 0xFF16161A.toInt(), true).apply { setPadding(0, 12, 0, 12) })
            val opts = (listOf(ans, ans + rnd.nextInt(1, 5), ans - rnd.nextInt(1, 5), ans + rnd.nextInt(6, 12))).shuffled()
            opts.chunked(2).forEach { pair ->
                pButtons(*pair.map { o -> Triple(o.toString(), true) { if (o == ans) correct++; ask(i + 1) } }.toTypedArray())
            }
        }
        ask(0)
    }

    /** Wawancara: 3 pertanyaan acak dari data/wawancara.txt. */
    private fun startInterview(a: PhoneState.Application) {
        val j = gameData.jobs[a.jobId] ?: return
        val qs = gameData.interview.shuffled().take(3)
        if (qs.isEmpty()) return
        var score = 0
        var caught = false
        val transcript = mutableListOf<Pair<Boolean, String>>()
        fun step(i: Int) {
            phone.clearApp()
            phone.setTitle("🗣️ Wawancara · ${j.name}")
            phone.backHandler = null
            transcript.forEach { (mine, t) -> phone.content.addView(bubble(t, mine)) }
            if (i >= qs.size) {
                val skill = (vars["akademik"]?.toIntOrNull() ?: 0) + (vars["komunikasi"]?.toIntOrNull() ?: 0)
                var chance = 40 - j.difficulty / 3 + score + a.score / 2 + skill * 2
                if (a.viaInsider) chance += 30
                if (life[LifeState.Stat.KEWARASAN] < 30) chance -= 15
                val ok = kotlin.random.Random.nextInt(100) < chance.coerceIn(5, 95)
                if (ok) {
                    a.stage = "diterima"
                    startJob(j.id)
                    hrdMessage(j, "Selamat! Kamu diterima sebagai ${j.name}. Mulai bekerja sesuai jadwal.")
                } else {
                    a.stage = "ditolak"
                    hrdMessage(j, "Terima kasih sudah datang wawancara. Mohon maaf, kami memilih kandidat lain.")
                }
                life.applyEffects(mapOf("energi" to -10L, "kewarasan" to (if (ok) 6L else -6L)))
                refreshLife()
                phone.content.addView(pText(if (ok) "✅ Kamu diterima!" else "❌ Belum berhasil kali ini.", 16f, 0xFF16161A.toInt(), true)
                    .apply { setPadding(0, 16, 0, 0) })
                if (caught) phone.content.addView(pText("Kebohonganmu sempat ketahuan pewawancara.", 12.5f, 0xFFB5402E.toInt()))
                pButtons(Triple("Kembali ke Lowongan", true) { renderJobs() })
                phone.scrollToBottom()
                return
            }
            val q = qs[i]
            transcript += false to q.question
            phone.content.addView(bubble(q.question, false))
            q.answers.forEach { ans ->
                pButtons(Triple(ans.text, true) {
                    transcript += true to ans.text
                    if (ans.lie) {
                        vars["bohong_wawancara_${j.id}"] = "1"
                        if (kotlin.random.Random.nextInt(100) < 40) {
                            caught = true
                            score -= 30
                            transcript += false to "Hmm... boleh saya cek kebenarannya? Sepertinya ada yang tidak cocok."
                        } else {
                            score += ans.score
                        }
                    } else {
                        score += ans.score
                    }
                    step(i + 1)
                })
            }
            phone.scrollToBottom()
        }
        step(0)
    }

    // ================================================================== hubungan

    private fun playerAge(): Int = vars["usia"]?.toIntOrNull() ?: 15

    private fun speakerIdFromRaw(raw: String?): GameData.CharacterDef? =
        raw?.let { gameData.findCharacter(it.trim().removePrefix("{").removeSuffix("}")) }

    /** Variabel hubungan untuk @jika: cinta_bayu, percaya_bayu, tingkat_bayu, kenal_bayu. */
    private fun syncSocialVars() {
        gameData.characters.values.forEach { c ->
            val r = social.rel(c)
            vars["cinta_${c.id}"] = (if (social.isPartner(c)) r.romance else r.love).toString()
            vars["percaya_${c.id}"] = r.trust.toString()
            vars["tingkat_${c.id}"] = social.stageOf(c).name.lowercase()
            vars["kenal_${c.id}"] = if (r.met) "1" else "0"
            vars["nama_${c.id}"] = c.name
        }
        panelCharId?.let { id -> gameData.characters[id]?.let { fillCharPanel(it) } }
    }

    /** Tampilkan perubahan Cinta (dan kabar yang menyebar) sebagai pemberitahuan. */
    private fun announce(changes: List<Pair<GameData.CharacterDef, Int>>) {
        changes.forEachIndexed { i, (c, d) ->
            if (d == 0) return@forEachIndexed
            val sign = if (d > 0) "+" else ""
            notice(
                if (i == 0) (if (d > 0) "💛 " else "💔 ") + "${c.name} $sign$d"
                else "🗣️ Kabar menyebar ke ${c.name} ($sign$d)"
            )
        }
        syncSocialVars()
    }

    // ---------------- gambar karakter ----------------

    private fun loadAsset(paths: List<String>, sample: Int = 1): Bitmap? {
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        for (p in paths) {
            try {
                return assets.open(p).use { BitmapFactory.decodeStream(it, null, opts) }
            } catch (_: Exception) {
            }
        }
        return null
    }

    /** Akhiran gambar untuk karakter yang jenis kelaminnya menyesuaikan pemain: _l / _p. */
    private fun genderSuffix(id: String): String? {
        val c = gameData.characters[id] ?: return null
        if (c.text["nama_l"] == null && c.text["nama_p"] == null) return null
        return if (c.gender == "perempuan") "p" else "l"
    }

    /** Foto tokoh utama sesuai jenis kelamin: karakter/pemain_l_foto.jpg / pemain_p_foto.jpg. */
    private fun playerPhoto(): Bitmap? {
        val g = if (playerGender == GENDER_FEMALE) "p" else "l"
        return loadAsset(listOf("pemain_${g}_foto.jpg", "pemain_${g}_foto.png", "pemain_$g.png")
            .map { "karakter/$it" })
    }

    /** Baris profil tokoh utama (foto bulat + nama + usia). Tanpa foto, hanya teks. */
    private fun playerProfileRow(dark: Boolean): View {
        val d = resources.displayMetrics.density
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, (2 * d).toInt(), 0, (12 * d).toInt())
        }
        playerPhoto()?.let { bmp ->
            row.addView(android.widget.ImageView(this).apply {
                setImageBitmap(bmp)
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(0xFF2A2A33.toInt())
                }
                clipToOutline = true
            }, LinearLayout.LayoutParams((64 * d).toInt(), (64 * d).toInt()).apply {
                marginEnd = (14 * d).toInt()
            })
        }
        row.addView(TextView(this).apply {
            text = (if (dark) "" else "Saya · ") + playerName + "\n" +
                (if (playerGender == GENDER_FEMALE) "Perempuan" else "Laki-laki") + " · " + playerAge() + " tahun"
            setTextColor(if (dark) 0xFFF6E7C8.toInt() else 0xFF2A2A33.toInt())
            textSize = 15f
            setLineSpacing(0f, 1.15f)
        })
        return row
    }

    private fun charPhoto(id: String): Bitmap? {
        val g = genderSuffix(id)
        val names = (if (g != null) listOf("${id}_${g}_foto") else emptyList()) + "${id}_foto"
        return loadAsset(names.flatMap { n -> listOf("karakter/$n.jpg", "karakter/$n.png", "karakter/$n.webp") })
    }

    private fun charBitmap(id: String, expr: String): Bitmap? {
        val g = genderSuffix(id)
        val names = (if (g != null) listOf("${id}_${g}_$expr", "${id}_$g") else emptyList()) + listOf("${id}_$expr", id)
        return loadAsset(names.flatMap { n -> listOf("karakter/$n.png", "karakter/$n.webp") })
    }

    /** Karakter yang sedang bicara (untuk disorot); null = narasi / pemain. */
    private var speakingCharId: String? = null

    private fun showCharacter(id: String, expr: String, pos: String, animate: Boolean = true) {
        val bmp = charBitmap(id, expr) ?: return
        val d = resources.displayMetrics.density
        val existing = charLayer.findViewWithTag<android.widget.ImageView>("char_$id")
        val iv = existing ?: android.widget.ImageView(this).apply {
            tag = "char_$id"
            adjustViewBounds = true
            scaleType = android.widget.ImageView.ScaleType.FIT_END
        }
        iv.setImageBitmap(bmp)
        val isNew = existing == null
        if (isNew) {
            val screenH = (charLayer.height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels)
            charLayer.addView(iv, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, (screenH * CHAR_BASE_HEIGHT).toInt(),
                android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL,
            ))
            if (animate) iv.translationY = 24 * d
        }
        // posisi baru selalu ditaruh paling akhir supaya urutan kiri→kanan stabil
        shownChars.remove(id)
        shownChars[id] = expr to pos
        vars["_tampil"] = shownChars.entries.joinToString(";") { "${it.key}:${it.value.first}:${it.value.second}" }
        layoutCharacters(animate, newId = if (isNew && animate) id else null)
    }

    /**
     * Menata semua karakter yang tampil supaya tidak bertumpuk:
     * 1 orang di tengah, 2 orang kiri–kanan, 3+ dibagi rata. Urutan mengikuti posisi
     * yang diminta naskah (kiri / tengah / kanan). Yang bicara di depan & terang,
     * yang lain sedikit lebih kecil dan redup.
     */
    private fun layoutCharacters(animate: Boolean, newId: String? = null) {
        val w = (charLayer.width.takeIf { it > 0 } ?: resources.displayMetrics.widthPixels).toFloat()
        val h = (charLayer.height.takeIf { it > 0 } ?: resources.displayMetrics.heightPixels).toFloat()
        val rank = mapOf("kiri" to 0, "tengah" to 1, "kanan" to 2)
        val order = shownChars.entries.withIndex()
            .sortedWith(compareBy({ rank[it.value.value.second] ?: 1 }, { it.index }))
            .map { it.value.key }
        val n = order.size
        if (n == 0) return
        val heightFrac = when (n) { 1 -> 0.72f; 2 -> 0.66f; else -> 0.60f }
        val spread = when (n) { 1 -> 0f; 2 -> 0.46f; 3 -> 0.62f; else -> 0.72f } * w
        val speaker = speakingCharId?.takeIf { it in shownChars }
        order.forEachIndexed { i, id ->
            val iv = charLayer.findViewWithTag<android.widget.ImageView>("char_$id") ?: return@forEachIndexed
            val x = if (n == 1) 0f else -spread / 2 + spread * i / (n - 1)
            val dim = n > 1 && speaker != null && speaker != id
            val scale = heightFrac / CHAR_BASE_HEIGHT * (if (dim) 0.95f else 1f)
            // perkecil dari tengah-bawah (kaki tetap di bawah layar)
            val viewH = h * CHAR_BASE_HEIGHT
            val dr = iv.drawable
            val viewW = if (dr != null && dr.intrinsicHeight > 0)
                minOf(w, viewH * dr.intrinsicWidth / dr.intrinsicHeight) else w
            iv.pivotX = viewW / 2
            iv.pivotY = viewH
            iv.colorFilter = if (dim) android.graphics.PorterDuffColorFilter(
                0x66000000, android.graphics.PorterDuff.Mode.SRC_ATOP) else null
            iv.translationZ = if (id == speaker) 2f else 0f
            iv.animate().cancel()
            if (id == newId) {
                iv.alpha = 0f
                iv.translationX = x
                iv.scaleX = scale; iv.scaleY = scale
                iv.animate().alpha(1f).translationY(0f).setDuration(350).start()
            } else if (animate) {
                iv.animate().alpha(1f).translationX(x).translationY(0f).scaleX(scale).scaleY(scale)
                    .setDuration(260).start()
            } else {
                iv.alpha = 1f; iv.translationX = x; iv.translationY = 0f
                iv.scaleX = scale; iv.scaleY = scale
            }
        }
    }

    /** Dipanggil saat dialog berganti pembicara. */
    private fun highlightSpeaker(id: String?) {
        if (speakingCharId == id) return
        speakingCharId = id
        if (shownChars.size > 1) layoutCharacters(animate = true)
    }

    private fun hideCharacter(id: String?) {
        val ids = if (id == null) shownChars.keys.toList() else listOf(id)
        ids.forEach { cid ->
            shownChars.remove(cid)
            val iv = charLayer.findViewWithTag<View>("char_$cid") ?: return@forEach
            // lepas tag dulu: kalau karakter yang sama langsung dimunculkan lagi,
            // dibuat gambar baru, bukan gambar yang sedang menghilang
            iv.tag = null
            iv.animate().cancel()
            iv.animate().alpha(0f).setDuration(220).withEndAction { charLayer.removeView(iv) }.start()
        }
        vars["_tampil"] = shownChars.entries.joinToString(";") { "${it.key}:${it.value.first}:${it.value.second}" }
        if (shownChars.isNotEmpty()) layoutCharacters(animate = true)
    }

    private fun restoreShownCharacters() {
        vars["_tampil"]?.split(';')?.forEach { e ->
            val parts = e.split(':')
            if (parts.size == 3) showCharacter(parts[0], parts[1], parts[2], animate = false)
        }
    }

    // ---------------- panel karakter ----------------

    private fun updateCharPanel(def: GameData.CharacterDef?) {
        val show = def != null && !memoryMode && social.rel(def).met
        if (show && def != null) {
            if (panelCharId != def.id) fillCharPanel(def)
            panelCharId = def.id
            if (charPanel.visibility != View.VISIBLE || charPanel.alpha < 1f) {
                charPanel.visibility = View.VISIBLE
                charPanel.animate().alpha(1f).setDuration(220).start()
            }
        } else if (panelCharId != null) {
            panelCharId = null
            charPanel.animate().alpha(0f).setDuration(200).withEndAction {
                if (panelCharId == null) charPanel.visibility = View.GONE
            }.start()
        }
    }

    private fun fillCharPanel(def: GameData.CharacterDef) {
        val r = social.rel(def)
        val photo = charPhoto(def.id) ?: charBitmap(def.id, "biasa")
        findViewById<android.widget.ImageView>(R.id.char_panel_photo).setImageBitmap(photo)
        findViewById<TextView>(R.id.char_panel_name).apply {
            text = def.name
            setTextColor(def.color)
        }
        findViewById<TextView>(R.id.char_panel_stage).text = social.stageOf(def).label
        findViewById<TextView>(R.id.char_panel_meters).text =
            (if (social.isPartner(def)) "❤️ Cinta ${r.romance}%" else "💛 ${r.love}%") + "   🤝 ${r.trust}%"
    }

    // ---------------- bertemu: ngobrol & hadiah ----------------

    private fun meetCharacter(c: GameData.CharacterDef) {
        closeSheet()
        val blocker = social.blockedBy(gameData, c)
        if (blocker != null) {
            afterTap = { showFreeSheet() }
            showLine(null, c.text["larangan"] ?: "${c.name} tidak bisa ditemui sekarang.")
            notice("🚫 ${blocker.name} melarang ${c.name} bertemu denganmu")
            return
        }
        social.rel(c).met = true
        phoneData.appts.firstOrNull { it.charId == c.id && it.day == life.day && it.slot == life.slot.name }?.let { a ->
            phoneData.appts.remove(a)
            announce(social.behave(gameData, c.id, "menepati_janji"))
            notice("🤝 Kamu menepati janji dengan ${c.name}")
        }
        meetingTalked = false
        meetingGifted = social.rel(c).lastGiftDay == life.day
        showCharacter(c.id, "biasa", "tengah")
        afterTap = { meetingMenu(c) }
        showLine(c.name, c.text["sapa"] ?: "Halo.")
    }

    private fun meetingMenu(c: GameData.CharacterDef) {
        val opts = mutableListOf<Pair<String, () -> Unit>>()
        if (!meetingTalked) opts += "💬 Ngobrol" to { chooseTopic(c) }
        if (!meetingGifted) opts += "🎁 Beri hadiah" to { chooseGift(c) }
        opts += "👋 Pamit" to { endMeeting(c) }
        showButtons(opts.map { it.first }) { i -> opts[i].second() }
    }

    private fun chooseTopic(c: GameData.CharacterDef) {
        val topics = gameData.topics.values.shuffled().take(3)
        if (topics.isEmpty()) {
            meetingMenu(c)
            return
        }
        showButtons(topics.map { "${it.icon} ${it.name}" }) { i ->
            val t = topics[i]
            meetingTalked = true
            val r = social.rel(c)
            val repeatToday = r.lastTalkDay == life.day
            r.lastTalkDay = life.day
            val (delta, key) = when (t.id) {
                in c.likeTopics -> (if (repeatToday) 3 else 6) to "respon_suka"
                in c.hateTopics -> -5 to "respon_benci"
                else -> (if (repeatToday) 1 else 2) to "respon_biasa"
            }
            when (t.id) {
                in c.likeTopics -> r.known += "+t:${t.id}"
                in c.hateTopics -> r.known += "-t:${t.id}"
            }
            afterTap = {
                announce(social.changeLove(gameData, c.id, delta))
                showCharacter(c.id, if (delta > 2) "senyum" else if (delta < 0) "kesal" else "biasa", "tengah")
                // Makin akrab, makin banyak yang ia ceritakan (atau ia bercerita tentang orang lain)
                val talks = (vars["obrolan_${c.id}"]?.toIntOrNull() ?: 0) + 1
                vars["obrolan_${c.id}"] = talks.toString()
                val story = if (delta >= 0) revealOwnFact(c, talks) ?: gossipFact(c) else null
                afterTap = if (story != null) ({
                    afterTap = { meetingMenu(c) }
                    showLine(null, story)
                }) else ({ meetingMenu(c) })
                showLine(c.name, c.text[key] ?: "...")
            }
            showLine("{nama}", t.line)
        }
    }

    private fun chooseGift(c: GameData.CharacterDef) {
        if (life.items.isEmpty()) {
            notice("🎒 Tasmu kosong. Belanja dulu di warung.")
            meetingMenu(c)
            return
        }
        hideDialog()
        openSheet("🎁 Beri hadiah", "Untuk ${c.name}   ·   1 hadiah per hari") { closeSheet(); showLine(c.name, c.text["sapa"] ?: "...", record = false, instant = true); meetingMenu(c) }
        life.items.sortedBy { it.expiresDay }.forEach { item ->
            val def = gameData.items[item.id] ?: return@forEach
            sheetList.addView(card(
                title = "${def.icon}  ${def.name}",
                sub = if (life.isExpired(item)) "⚠️ Basi" else "",
                actions = listOf("Beri" to { giveGift(c, item, def) }),
            ))
        }
        sheetButtonRow(getString(R.string.btn_back) to {
            closeSheet()
            showLine(c.name, c.text["sapa"] ?: "...", record = false, instant = true)
            meetingMenu(c)
        })
    }

    private fun giveGift(c: GameData.CharacterDef, item: LifeState.Item, def: GameData.ItemDef) {
        closeSheet()
        life.items.remove(item)
        meetingGifted = true
        social.rel(c).lastGiftDay = life.day
        refreshLife()
        val expired = life.isExpired(item)
        val (delta, key) = when {
            expired -> -8 to "hadiah_benci"
            def.id in c.likeItems -> 10 to "hadiah_suka"
            def.id in c.hateItems -> -6 to "hadiah_benci"
            else -> 3 to "hadiah_biasa"
        }
        when {
            def.id in c.likeItems -> social.rel(c).known += "+i:${def.id}"
            def.id in c.hateItems -> social.rel(c).known += "-i:${def.id}"
        }
        announce(social.changeLove(gameData, c.id, delta))
        if (expired) {
            social.changeTrust(gameData, c.id, -6)
            syncSocialVars()
        }
        showCharacter(c.id, if (delta > 3) "senyum" else if (delta < 0) "kesal" else "biasa", "tengah")
        afterTap = { meetingMenu(c) }
        showLine(c.name, if (expired) "Ini... sudah basi, kan? Kamu serius ngasih ini ke aku?" else (c.text[key] ?: "Terima kasih."))
    }

    private fun endMeeting(c: GameData.CharacterDef) {
        hideCharacter(c.id)
        updateCharPanel(null)
        life.applyEffects(mapOf("energi" to -5L, "kewarasan" to (if (social.rel(c).love >= 50) 5L else 2L)))
        freeLeft--
        vars["_bebas_sisa"] = freeLeft.toString()
        passTime(60)
        if (life.dead) return
        continueFree()
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
        val ch = gameData.findCharacter(sender)
        val key = when {
            ch != null -> ch.id
            sender.startsWith("HRD") -> "hrd_" + sender
            else -> sender
        }
        val th = phoneData.thread(key, ch?.name ?: sender)
        th.msgs += PhoneState.Msg(false, text, life.day)
        th.unread++
        if (ch != null) th.pendingSince = life.day
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
        if (wait == Wait.FREE && !life.dead) showFreeSheet()
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
