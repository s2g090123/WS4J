package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.WS4JConfiguration
import java.util.*


/**
 * Computing semantic relatedness of word senses by counting
 * nodes in the noun and verb WordNet 'is-a' hierarchies.
 *
 * @author Hideki Shima
 */
class Path(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
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
        val shortestPaths = pathFinder.getShortestPaths(concept1, concept2, subTracer)
        if (shortestPaths.isEmpty()) return Relatedness(min)
        val path = shortestPaths[0]
        val dist = path.pathLength
        val score = if (dist > 0) 1.0 / dist.toDouble()
        else -1.0
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("PATH(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append(Objects.requireNonNull(subTracer))
            tracer.append("Shortest path: ").append(path).append("\n")
            tracer.append("Path length = ").append(dist).append("\n")
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