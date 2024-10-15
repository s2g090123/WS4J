package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.WS4JConfiguration
import kotlin.math.ln


/**
 * This class calculates the Leacock-Chodorow similarity score between two synsets.
 * Following definition is cited from (Budanitsky & Hirst, 2001).
 * <blockquote>
 * Leacock-Chodorow: Leacock and Chodorow (1998)
 * also rely on the length len(c1; c2) of the shortest path between
 * two synsets for their measure of similarity. However,
 * they limit their attention to IS-A links and scale the
 * path length by the overall depth D of the taxonomy:
 * <div style="padding:20px">`sim<sub>LC</sub>(c<sub>1</sub>, c<sub>2</sub>) =
 * -log(len(c<sub>1</sub>, c<sub>2</sub>) / 2D).`</div>
</blockquote> *
 *
 *
 * (from lch.pm) This module computes the semantic relatedness of word senses according
 * to a method described by Leacock and Chodorow (1998). This method counts up
 * the number of edges between the senses in the 'is-a' hierarchy of WordNet.
 * The value is then scaled by the maximum depth of the WordNet 'is-a'
 * hierarchy. A relatedness value is obtained by taking the negative log
 * of this scaled value.
 *
 * @author Hideki Shima
 */
class LeacockChodorow(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
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
        } else if (concept1 == concept2) {
            return Relatedness(
                max,
                identicalSynset,
                null
            )
        }
        val subTracer = if (WS4JConfiguration.getInstance().useTrace()) StringBuilder() else null
        val lcsList = pathFinder.getLCSByPath(concept1, concept2, subTracer)
        if (lcsList.isEmpty()) return Relatedness(min)
        val maxDepth = when (concept1.pos) {
            POS.NOUN -> 20
            POS.VERB -> 14
            else -> 1
        }
        val length = lcsList[0].pathLength
        val score = -ln(length / (2.0 * maxDepth))
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("LCH(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append(requireNotNull(subTracer))
            lcsList.forEach { lcs ->
                tracer.append("Lowest Common Subsumer(s): ")
                tracer.append(lcs.subsumer.toString()).append(" (Length = ").append(lcs.pathLength).append(")\n")
            }
        }
        return Relatedness(score, tracer.toString(), null)
    }

    override val posPairs: List<Array<POS>> = POSPairs

    companion object {
        private const val min = 0.0
        private const val max = Double.MAX_VALUE

        private val POSPairs: List<Array<POS>> =
            object : ArrayList<Array<POS>>() {
                init {
                    add(arrayOf(POS.NOUN, POS.NOUN))
                    add(arrayOf(POS.VERB, POS.VERB))
                }
            }
    }
}