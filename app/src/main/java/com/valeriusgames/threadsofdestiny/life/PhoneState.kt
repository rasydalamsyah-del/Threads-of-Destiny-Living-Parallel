package com.valeriusgames.threadsofdestiny.life

import org.json.JSONArray
import org.json.JSONObject

/** Isi HP pemain: chat, janji temu, lamaran kerja, dokumen, riwayat uang, catatan. */
class PhoneState {

    data class Msg(val fromMe: Boolean, val text: String, val day: Int)

    class Thread(val key: String, var title: String) {
        val msgs = mutableListOf<Msg>()
        var unread = 0
        /** Hari pesan masuk yang belum dibalas (-1 = tidak ada). */
        var pendingSince = -1
        /** Hari terakhir pemain membalas (bonus Cinta hanya sekali sehari). */
        var repliedDay = -1
    }

    /** Janji ketemu dengan karakter pada hari & waktu tertentu. */
    data class Appt(val charId: String, val day: Int, val slot: String)

    /**
     * Lamaran kerja.
     * stage: dikirim → tes → wawancara → diterima / ditolak / gugur
     */
    class Application(
        val jobId: String,
        var stage: String,
        var dueDay: Int,
        var score: Int = 0,
        var viaInsider: Boolean = false,
    )

    data class Tx(val day: Int, val desc: String, val amount: Long)
    data class Note(val day: Int, val text: String)

    val threads = linkedMapOf<String, Thread>()
    val appts = mutableListOf<Appt>()
    val apps = mutableListOf<Application>()
    val docs = mutableSetOf<String>()
    val pendingDocs = mutableMapOf<String, Int>()
    val tx = mutableListOf<Tx>()
    val notes = mutableListOf<Note>()

    fun thread(key: String, title: String): Thread = threads.getOrPut(key) { Thread(key, title) }

    val unreadTotal: Int get() = threads.values.sumOf { it.unread }

    fun logTx(day: Int, desc: String, amount: Long) {
        if (amount == 0L) return
        tx += Tx(day, desc, amount)
        while (tx.size > 60) tx.removeAt(0)
    }

    fun toJson(): String = JSONObject().apply {
        put("threads", JSONArray().apply {
            threads.values.forEach { t ->
                put(JSONObject().apply {
                    put("key", t.key); put("title", t.title); put("unread", t.unread)
                    put("pending", t.pendingSince); put("replied", t.repliedDay)
                    put("msgs", JSONArray().apply {
                        t.msgs.takeLast(40).forEach { m ->
                            put(JSONObject().put("me", m.fromMe).put("text", m.text).put("day", m.day))
                        }
                    })
                })
            }
        })
        put("appts", JSONArray().apply {
            appts.forEach { put(JSONObject().put("char", it.charId).put("day", it.day).put("slot", it.slot)) }
        })
        put("apps", JSONArray().apply {
            apps.forEach {
                put(JSONObject().put("job", it.jobId).put("stage", it.stage).put("due", it.dueDay)
                    .put("score", it.score).put("insider", it.viaInsider))
            }
        })
        put("docs", JSONArray(docs.toList()))
        put("pendingDocs", JSONObject().apply { pendingDocs.forEach { (k, v) -> put(k, v) } })
        put("tx", JSONArray().apply {
            tx.forEach { put(JSONObject().put("day", it.day).put("desc", it.desc).put("amount", it.amount)) }
        })
        put("notes", JSONArray().apply {
            notes.forEach { put(JSONObject().put("day", it.day).put("text", it.text)) }
        })
    }.toString()

    companion object {
        fun fromJson(s: String?): PhoneState {
            val st = PhoneState()
            if (s.isNullOrEmpty()) return st
            try {
                val o = JSONObject(s)
                o.optJSONArray("threads")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val j = arr.getJSONObject(i)
                        val t = Thread(j.getString("key"), j.optString("title"))
                        t.unread = j.optInt("unread")
                        t.pendingSince = j.optInt("pending", -1)
                        t.repliedDay = j.optInt("replied", -1)
                        j.optJSONArray("msgs")?.let { ms ->
                            for (k in 0 until ms.length()) {
                                val m = ms.getJSONObject(k)
                                t.msgs += Msg(m.optBoolean("me"), m.optString("text"), m.optInt("day"))
                            }
                        }
                        st.threads[t.key] = t
                    }
                }
                o.optJSONArray("appts")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val j = arr.getJSONObject(i)
                        st.appts += Appt(j.getString("char"), j.optInt("day"), j.optString("slot"))
                    }
                }
                o.optJSONArray("apps")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val j = arr.getJSONObject(i)
                        st.apps += Application(j.getString("job"), j.optString("stage"), j.optInt("due"),
                            j.optInt("score"), j.optBoolean("insider"))
                    }
                }
                o.optJSONArray("docs")?.let { arr -> for (i in 0 until arr.length()) st.docs += arr.getString(i) }
                o.optJSONObject("pendingDocs")?.let { pd -> pd.keys().forEach { k -> st.pendingDocs[k] = pd.optInt(k) } }
                o.optJSONArray("tx")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val j = arr.getJSONObject(i)
                        st.tx += Tx(j.optInt("day"), j.optString("desc"), j.optLong("amount"))
                    }
                }
                o.optJSONArray("notes")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val j = arr.getJSONObject(i)
                        st.notes += Note(j.optInt("day"), j.optString("text"))
                    }
                }
            } catch (_: Exception) {
            }
            return st
        }
    }
}
