package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.GlossFinder
import jiachian.ws4j.util.OverlapFinder
import jiachian.ws4j.util.WS4JConfiguration


/**
 * This class calculates Lesk similarity score between two synsets through
 * the Banerjee and Pedersen (2002) method.
 *
 *
 * From WS package:
 *
 *
 * Lesk (1985) proposed that the relatedness of two words is proportional
 * to the extent of overlaps of their dictionary definitions. Banerjee and
 * Pedersen (2002) extended this notion to use WordNet as the dictionary
 * for the word definitions. This notion was further extended to use the rich
 * network of relationships between concepts present is WordNet. This adapted
 * lesk measure has been implemented in this module.
 *
 * @author Hideki Shima
 */
class Lesk(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
    private val glossFinder: GlossFinder = GlossFinder(db)

    private var overlapLog: StringBuilder? = null
    private var overlapLogMax: StringBuilder? = null

    override fun calcRelatedness(
        concept1: Concept?,
        concept2: Concept?
    ): Relatedness {
        val tracer = StringBuilder()
        if (concept1 == null || concept2 == null) return Relatedness(min)
        val glosses = glossFinder.getSuperGlosses(concept1, concept2)
        var score = 0
        for (gloss in glosses) {
            var functionsScore = calcFromSuperGloss(gloss.gloss1, gloss.gloss2)
            functionsScore *= gloss.weight
            if (WS4JConfiguration.getInstance().useTrace() && functionsScore > 0) {
                tracer.append("LESK(").append(concept1).append(", ").append(concept2).append(")\n")
                tracer.append("Functions: ").append(gloss.link1.trim { it <= ' ' }).append(" - ")
                    .append(gloss.link2.trim { it <= ' ' }).append(" : ").append(functionsScore).append("\n")
                tracer.append(overlapLogMax).append("\n")
            }
            score = (score + functionsScore).toInt()
        }
        return Relatedness(score.toDouble(), tracer.toString(), null)
    }

    private fun calcFromSuperGloss(glosses1: List<String>, glosses2: List<String>): Double {
        var max = 0.0
        overlapLogMax = StringBuilder()
        for (gloss1 in glosses1) {
            for (gloss2 in glosses2) {
                val score = calcFromSuperGloss(gloss1, gloss2)
                if (max < score) {
                    overlapLogMax = overlapLog
                    max = score
                }
            }
        }
        return max
    }

    private fun calcFromSuperGloss(gloss1: String, gloss2: String): Double {
        val overlaps = OverlapFinder.getOverlaps(gloss1, gloss2)
        var functionsScore = 0.0
        if (WS4JConfiguration.getInstance().useTrace()) {
            overlapLog = StringBuilder("Overlaps: ")
        }
        for (key in overlaps.overlapsHash?.keys ?: emptySet<String>()) {
            val tempArray = key.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val value = (tempArray.size) * (tempArray.size) * (overlaps.overlapsHash?.get(key) ?: 0)
            functionsScore += value.toDouble()
            if (WS4JConfiguration.getInstance().useTrace()) {
                overlapLog
                    ?.append(overlaps.overlapsHash?.get(key))
                    ?.append(" x \"")
                    ?.append(key)
                    ?.append("\" ")
            }
        }
        if (WS4JConfiguration.getInstance().useTrace()) {
            overlapLog = StringBuilder()
        }
        if (WS4JConfiguration.getInstance().useLeskNormalizer()) {
            val denominator = overlaps.length1 + overlaps.length2
            if (denominator > 0) {
                functionsScore /= denominator.toDouble()
            }
            if (WS4JConfiguration.getInstance().useTrace()) {
                overlapLog
                    ?.append("Normalized by dividing the score with ")
                    ?.append(overlaps.length1)
                    ?.append(" and ")
                    ?.append(overlaps.length1)
                    ?.append("\n")
            }
        }
        return functionsScore
    }

    override val posPairs: List<Array<POS>> = POSPairs

    companion object {
        private const val min = 0.0
        private const val max = Double.MAX_VALUE

        private val POSPairs: List<Array<POS>> =
            object : ArrayList<Array<POS>>() {
                init {
                    add(arrayOf(POS.ADJECTIVE, POS.ADJECTIVE))
                    add(arrayOf(POS.ADJECTIVE, POS.ADVERB))
                    add(arrayOf(POS.ADJECTIVE, POS.NOUN))
                    add(arrayOf(POS.ADJECTIVE, POS.VERB))

                    add(arrayOf(POS.ADVERB, POS.ADJECTIVE))
                    add(arrayOf(POS.ADVERB, POS.ADVERB))
                    add(arrayOf(POS.ADVERB, POS.NOUN))
                    add(arrayOf(POS.ADVERB, POS.VERB))

                    add(arrayOf(POS.NOUN, POS.ADJECTIVE))
                    add(arrayOf(POS.NOUN, POS.ADVERB))
                    add(arrayOf(POS.NOUN, POS.NOUN))
                    add(arrayOf(POS.NOUN, POS.VERB))

                    add(arrayOf(POS.VERB, POS.ADJECTIVE))
                    add(arrayOf(POS.VERB, POS.ADVERB))
                    add(arrayOf(POS.VERB, POS.NOUN))
                    add(arrayOf(POS.VERB, POS.VERB))
                }
            }
    }
}