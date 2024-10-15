package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.ICFinder
import jiachian.ws4j.util.WS4JConfiguration
import kotlin.math.ln


/**
 * This class calculates the Jiang-Conrath distance score between two synsets.
 * Following definition is cited from (Budanitsky & Hirst, 2001).
 * <blockquote>
 * Jiang–Conrath: Jiang and Conrath’s (1997) approach
 * also uses the notion of information content, but in the
 * form of the conditional probability of encountering an instance
 * of a child-synset given an instance of a parent synset.
 * Thus the information content of the two nodes, as
 * well as that of their most specific concept, plays a part.
 * Notice that this formula measures semantic distance, the
 * inverse of similarity.
 * <div style="padding:20px">`dist<sub>JS</sub>(c<sub>1</sub>, c<sub>2</sub>) =
 * 2 * log(p(lso(c<sub>1</sub>, c<sub>2</sub>))) - (log(p(c<sub>1</sub>))+log(p(c<sub>2</sub>))).`</div>
</blockquote> *
 *
 * @author Hideki Shima
 */
class JiangConrath(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
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
        val lcsList = ICFinder.instance.getLCSbyIC(pathFinder, concept1, concept2, subTracer)
        if (requireNotNull(lcsList).isEmpty()) return Relatedness(min, tracer.toString(), null)
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("JCN(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append(requireNotNull(subTracer))
            lcsList.forEach { lcs ->
                tracer.append("Lowest Common Subsumer(s): ")
                tracer.append(lcs.subsumer.toString()).append(" (IC = ").append(lcs.iC).append(")\n")
            }
        }
        val subsumer = lcsList[0]
        val lcsIC = subsumer.iC
        val rootConcept = requireNotNull(pathFinder.getRoot(subsumer.subsumer)).apply {
            pos = subsumer.subsumer.pos
        }
        val rootFreq = ICFinder.instance.getFrequency(rootConcept)
        if (rootFreq <= 0) {
            return Relatedness(min, tracer.toString(), null)
        }
        val ic1 = ICFinder.instance.IC(pathFinder, concept1)
        val ic2 = ICFinder.instance.IC(pathFinder, concept2)
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("IC(").append(concept1).append(") = ").append(ic1).append("\n")
            tracer.append("IC(").append(concept2).append(") = ").append(ic2).append("\n")
        }
        val distance = if (ic1 > 0 && ic2 > 0) {
            ic1 + ic2 - (2 * lcsIC)
        } else {
            return Relatedness(min, tracer.toString(), null)
        }
        val score = if (distance == 0.0) {
            if (rootFreq > 0.01) {
                1.0 / -ln((rootFreq.toDouble() - 0.01) / rootFreq.toDouble())
            } else {
                return Relatedness(min, tracer.toString(), null)
            }
        } else {
            1.0 / distance
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