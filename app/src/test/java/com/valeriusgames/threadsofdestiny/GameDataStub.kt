package com.valeriusgames.threadsofdestiny

import com.valeriusgames.threadsofdestiny.life.GameData

/** GameData kosong untuk menguji fungsi-fungsinya tanpa membaca aset Android. */
object GameDataStub {
    fun empty(): GameData = GameData.forTest()
}
