package jiachian.ws4j.util

import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.RelatednessCalculator
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.math.ln

class ICFinder private constructor() {
    private var freqV: ConcurrentMap<Int, Int>? = null
    private var freqN: ConcurrentMap<Int, Int>? = null

    init {
        try {
            loadIC()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    @Throws(IOException::class)
    private fun loadIC() {
        freqV = ConcurrentHashMap()
        freqN = ConcurrentHashMap()
        val stream = javaClass.classLoader.getResourceAsStream(INFO_CONTENT) ?: return
        val isr = InputStreamReader(stream)
        val br = BufferedReader(isr)
        var line = ""
        while (br.readLine()?.also { line = it } != null) {
            val elements = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            if (elements.size >= 2) {
                val e = elements[0]
                val pos = POS.getPOS(e[e.length - 1])
                val id = e.substring(0, e.length - 1).toInt()
                val freq = elements[1].toInt()
                if (pos == POS.NOUN) {
                    freqN?.put(id, freq)
                } else if (pos == POS.VERB) {
                    freqV?.put(id, freq)
                }
            }
        }
        br.close()
        isr.close()
    }

    fun getLCSbyIC(
        pathFinder: PathFinder,
        concept1: Concept,
        concept2: Concept,
        tracer: StringBuilder?
    ): List<PathFinder.Subsumer>? {
        val paths = pathFinder.getAllPaths(concept1, concept2, tracer)
        if (paths.isEmpty()) return null
        paths.forEach { path ->
            path.iC = IC(pathFinder, path.subsumer)
        }
        val sortedPaths = paths.sortedByDescending { it.iC }
        val results = sortedPaths.filter { path -> path.iC == sortedPaths[0].iC }
        return results
    }

    fun IC(
        pathFinder: PathFinder,
        concept: Concept
    ): Double {
        val pos = concept.pos
        return if (pos == POS.NOUN || pos == POS.VERB) {
            val prob = probability(pathFinder, concept)
            if (prob > 0.0) -ln(prob) else 0.0
        } else {
            0.0
        }
    }

    private fun probability(
        pathFinder: PathFinder,
        concept: Concept
    ): Double {
        var rootFreq = 0
        if (RelatednessCalculator.useRootNode) {
            if (concept.pos == POS.NOUN) {
                rootFreq = rootFreqN
            } else if (concept.pos == POS.VERB) {
                rootFreq = rootFreqV
            }
        } else {
            pathFinder.getRoot(concept)?.let {
                rootFreq = getFrequency(it)
            }
        }
        val offFreq = getFrequency(concept)
        return if (offFreq <= rootFreq) return offFreq / rootFreq.toDouble() else 0.0
    }

    fun getFrequency(concept: Concept): Int {
        if (concept.synsetID == "0") {
            if (concept.pos == POS.NOUN) {
                return rootFreqN
            } else if (concept.pos == POS.VERB) {
                return rootFreqV
            }
        }
        val synsetID = concept.synsetID.replace("\\D".toRegex(), "").toInt()
        var freq = 0
        if (concept.pos == POS.NOUN) {
            freq = freqN?.get(synsetID) ?: 0
        } else if (concept.pos == POS.VERB) {
            freq = freqV?.get(synsetID) ?: 0
        }
        return freq
    }

    companion object {
        private const val INFO_CONTENT = "infoContent"

        private const val rootFreqN = 128767 // sum of all root freq of n in infoContent
        private const val rootFreqV = 95935 // sum of all root freq of v in infoContent

        val instance: ICFinder = ICFinder()
    }
}
