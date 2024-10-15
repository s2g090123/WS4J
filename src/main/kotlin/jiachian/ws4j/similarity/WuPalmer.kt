package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.WS4JConfiguration
import java.util.*


/**
 * This class calculates semantic relatedness of word senses using
 * the edge counting method of the of Wu & Palmer (1994).
 *
 * @author Hideki Shima
 */
class WuPalmer(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
    override fun calcRelatedness(
        concept1: Concept?,
        concept2: Concept?
    ): Relatedness {
        val tracer = StringBuilder()
        if (concept1 == null || concept2 == null) {
            return Relatedness(
                min,
                null,
                illegalSynset
            )
        }
        if (concept1 == concept2) {
            return Relatedness(
                max,
                identicalSynset,
                null
            )
        }
        val subTracer = if (WS4JConfiguration.getInstance().useTrace()) StringBuilder() else null
        val lcsList = depthFinder.getRelatedness(concept1, concept2, subTracer)
        if (lcsList.isNullOrEmpty()) return Relatedness(min)
        val depth = lcsList[0].depth
        val depth1 = depthFinder.getShortestDepth(concept1)
        val depth2 = depthFinder.getShortestDepth(concept2)
        var score = 0.0
        if (depth1 > 0 && depth2 > 0) score = (2 * depth).toDouble() / (depth1 + depth2).toDouble()
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("WUP(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append(Objects.requireNonNull(subTracer))
            lcsList.forEach { lcs ->
                tracer.append("Lowest Common Subsumer(s): ")
                tracer.append(lcs.leaf).append(" (Depth = ").append(lcs.depth).append(")\n")
            }
            tracer.append("Depth(").append(concept1).append(") = ").append(depth1).append("\n")
            tracer.append("Depth(").append(concept2).append(") = ").append(depth2).append("\n")
        }
        return Relatedness(score, tracer.toString(), null)
    }

    override val posPairs: List<Array<POS>> = POSPairs

    companion object {
        private const val min = 0.0
        private const val max = 1.0

        private val POSPairs: List<Array<POS>> =
            object : ArrayList<Array<POS>>() {
                init {
                    add(arrayOf(POS.NOUN, POS.NOUN))
                    add(arrayOf(POS.VERB, POS.VERB))
                }
            }
    }
}