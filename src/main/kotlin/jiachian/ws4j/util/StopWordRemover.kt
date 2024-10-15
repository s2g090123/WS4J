package jiachian.ws4j.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class StopWordRemover private constructor() {
    private var stopList: MutableSet<String>? = null

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
        stopList = HashSet()
        val stream = javaClass.classLoader.getResourceAsStream(STOP_WORDS)
            ?: throw Exception("An Error occurs when loading the resources stream")
        val isr = InputStreamReader(stream)
        val br = BufferedReader(isr)
        var line = ""
        while ((br.readLine()?.also { line = it }) != null) {
            val stopWord = line.trim { it <= ' ' }
            if (stopWord.isNotEmpty()) stopList?.add(stopWord)
        }
        br.close()
        isr.close()
    }

    fun removeStopWords(words: Array<String>): Array<String> {
        val contents = ArrayList<String>(words.size)
        words
            .filter { word -> stopList?.contains(word) != true }
            .forEach { e -> contents.add(e) }
        return contents.toTypedArray()
    }

    companion object {
        private const val STOP_WORDS = "stopWords"

        val instance: StopWordRemover = StopWordRemover()
    }
}