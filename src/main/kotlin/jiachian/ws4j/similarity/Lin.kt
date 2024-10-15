package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.ICFinder
import jiachian.ws4j.util.WS4JConfiguration
import java.util.*


/**
 * This class calculates the Lin's similarity score between two synsets.
 * Following definition is cited from (Budanitsky & Hirst, 2001).
 * <blockquote>
 * Lin: Lin’s (1998) similarity measure follows from his
 * theory of similarity between arbitrary objects. It uses the
 * same elements as distJC, but in a different fashion:
 * <div style="padding:20px">`sim<sub>L</sub>(c<sub>1</sub>, c<sub>2</sub>) =
 * 2 * log p(lso(c<sub>1</sub>, c<sub>2</sub>)) / (log p(c<sub>1</sub>) + log p(c<sub>2</sub>)).`</div>
</blockquote> *
 *
 * @author Hideki Shima
 */
class Lin(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
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
        val subTracer = StringBuilder()
        val lcsList = ICFinder.instance.getLCSbyIC(pathFinder, concept1, concept2, subTracer)
        if (requireNotNull(lcsList).isEmpty()) {
            return Relatedness(min, tracer.toString(), null)
        }
        val ic1 = ICFinder.instance.IC(pathFinder, concept1)
        val ic2 = ICFinder.instance.IC(pathFinder, concept2)
        val score = if ((ic1 > 0 && ic2 > 0)) (2.0 * lcsList[0].iC / (ic1 + ic2)) else 0.0
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("LIN(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append(Objects.requireNonNull(subTracer))
            lcsList.forEach { lcs ->
                tracer.append("Lowest Common Subsumer(s): ")
                tracer.append(lcs.subsumer).append(" (IC = ").append(lcs.iC).append(")\n")
            }
            tracer.append("Concept(").append(concept1).append(") = ").append(" (IC = ").append(ic1).append(")\n")
            tracer.append("Concept(").append(concept2).append(") = ").append(" (IC = ").append(ic2).append(")\n")
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