package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.ICFinder
import jiachian.ws4j.util.WS4JConfiguration


/**
 * This class calculates the Resnik's similarity score between two synsets.
 * Following definition is cited from (Budanitsky & Hirst, 2001).
 * <blockquote>
 * Resnik: Resnik’s (1995) approach was, to our knowledge,
 * the first to bring together ontology and corpus.
 * Guided by the intuition that the similarity between a
 * pair of concepts may be judged by “the extent to which
 * they share information”,Resnik defined the similarity between
 * two concepts lexicalized in WordNet to be the information
 * content of their lowest super-ordinate (most
 * specific common concept) lso(c1; c2):
 * <div style="padding:20px">`sim<sub>R</sub>(c<sub>1</sub>, c<sub>2</sub>) = -log p(lso(c<sub>1</sub>, c<sub>2</sub>)).`</div>
 * where p(c) is the probability of encountering an instance
 * of a synset c in some specific corpus.
</blockquote> *
 *
 * @author Hideki Shima
 */
class Resnik(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
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
        val lcsList = ICFinder.instance.getLCSbyIC(pathFinder, concept1, concept2, subTracer)
        if (requireNotNull(lcsList).isEmpty()) {
            return Relatedness(min, tracer.toString(), null)
        }
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("RES(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append(requireNotNull(subTracer))
            lcsList.forEach { lcs ->
                tracer.append("Lowest Common Subsumer(s): ")
                tracer.append(lcs.subsumer).append(" (IC = ").append(lcs.iC).append(")\n")
            }
        }
        val subsumer = lcsList[0]
        return Relatedness(subsumer.iC, tracer.toString(), null)
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