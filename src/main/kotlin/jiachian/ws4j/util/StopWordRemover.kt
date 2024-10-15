package jiachian.ws4j.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class StopWordRemover private constructor() {
    private var stopList: Set<String>? = null

    init {
        try {
            loadStopWords()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun loadStopWords() {
        val set = mutableSetOf<String>()
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream(STOP_WORDS))
        val isr = InputStreamReader(stream)
        val br = BufferedReader(isr)
        var line = ""
        while ((br.readLine()?.also { line = it }) != null) {
            val stopWord = line.trim { it <= ' ' }
            if (stopWord.isNotEmpty()) {
                set.add(stopWord)
            }
        }
        stopList = set
        br.close()
        isr.close()
    }

    fun removeStopWords(words: Array<String>): Array<String> {
        val contents = words
            .filter { word -> stopList?.contains(word) != true }
            .toTypedArray()
        return contents
    }

    companion object {
        private const val STOP_WORDS = "stopWords"

        val instance = StopWordRemover()
    }
}