package jiachian.ws4j.similarity

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.Relatedness
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.util.Traverser
import jiachian.ws4j.util.WS4JConfiguration

/**
 * This class calculates the Hirst and St-Onge relatedness score between two synsets.
 * Following definition is cited from (Budanitsky & Hirst, 2001).
 * <blockquote>
 * Hirst and St-Onge: The idea behind Hirst and St-Onge’s
 * (1998) measure of semantic relatedness is that two lexicalized
 * concepts are semantically close if their WordNet
 * synsets are connected by a path that is not too long and
 * that “does not change direction too often”. The strength
 * of the relationship is given by:
 * <div style="padding:20px">`rel<sub>HS</sub>(c<sub>1</sub>, c<sub>2</sub>) = C - path_length - k * d.`</div>
 * where d is the number of changes of direction in the
 * path, and C and k are constants; if no such path exists,
 * rel_HS(c1, c2) is zero and the synsets are deemed unrelated.
</blockquote> *
 *
 *
 * From WS:
 * Unless a problem occurs, the return value is the relatedness
 * score, which is greater-than or equal-to 0 and less-than or equal-to 16.
 * If an error occurs, then the error level is set to non-zero and an error
 * string is created (see the description of getError()).
 *
 * @author Hideki Shima
 */
class HirstStOnge(db: ILexicalDatabase) : RelatednessCalculator(db, min, max) {
    private val traverser = Traverser(db)

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
        val horizontal1 = traverser.getHorizontalSynsets(concept1)
        val horizontal2 = traverser.getHorizontalSynsets(concept2)
        val inHorizon = horizontal2.contains(concept1) || horizontal1.contains(concept2)
        if (inHorizon) return Relatedness(max)
        val upward1 = traverser.getUpwardSynsets(concept1)
        val upward2 = traverser.getUpwardSynsets(concept2)
        val downward1 = traverser.getDownwardSynsets(concept1)
        val downward2 = traverser.getDownwardSynsets(concept2)
        if (WS4JConfiguration.getInstance().useTrace()) {
            tracer.append("HSO(").append(concept1).append(", ").append(concept2).append(")\n")
            tracer.append("Horizontal Links(").append(concept1.toString()).append(") = ").append(horizontal1)
                .append("\n")
            tracer.append("Horizontal Links(").append(concept2.toString()).append(") = ").append(horizontal1)
                .append("\n")
            tracer.append("Upward Links(").append(concept1.toString()).append(") = ").append(upward1).append("\n")
            tracer.append("Upward Links(").append(concept2.toString()).append(") = ").append(upward2).append("\n")
            tracer.append("Downward Links(").append(concept1.toString()).append(") = ").append(downward1).append("\n")
            tracer.append("Downward Links(").append(concept2.toString()).append(") = ").append(downward2).append("\n")
        }
        val contained = traverser.contained(concept1, concept2)
        val inUpOrDown = upward2.contains(concept1) || downward2.contains(concept1)
        if (contained && inUpOrDown) {
            tracer.append("Strong Rel (Compound Word Match).\n")
            return Relatedness(max, tracer.toString(), null)
        }
        val score = MedStrong(traverser).medStrong(0, 0, 0, concept1, concept1.synsetID, concept2)
        return Relatedness(score.toDouble(), tracer.toString(), null)
    }

    override val posPairs: List<Array<POS>> = POSPairs

    private class MedStrong(private val traverser: Traverser) {
        fun medStrong(
            state: Int,
            distance: Int,
            chdir: Int,
            from: Concept,
            path: String,
            endSynset: Concept
        ): Int {
            if (from == endSynset && distance > 1) return 8 - distance - chdir
            if (distance >= 5) return 0
            val horizontal = traverser.getHorizontalSynsets(from)
            val upward = if (state == 0 || state == 1) traverser.getUpwardSynsets(from) else emptySet()
            val downward = if (state != 6) traverser.getDownwardSynsets(from) else emptySet()
            return when (state) {
                0 -> {
                    val retU = findU(upward, 1, distance, 0, path, endSynset)
                    val retD = findD(downward, 2, distance, 0, path, endSynset)
                    val retH = findH(horizontal, 3, distance, 0, path, endSynset)
                    if (retU > retD && retU > retH) return retU
                    if (retD > retH) return retD
                    retH
                }

                1 -> {
                    val retU = findU(upward, 1, distance, 0, path, endSynset)
                    val retD = findD(downward, 4, distance, 1, path, endSynset)
                    val retH = findH(horizontal, 5, distance, 1, path, endSynset)
                    if (retU > retD && retU > retH) return retU
                    if (retD > retH) return retD
                    retH
                }

                2 -> {
                    val retD = findD(downward, 2, distance, 0, path, endSynset)
                    val retH = findH(horizontal, 6, distance, 0, path, endSynset)
                    if (retD > retH) retD else retH
                }

                3 -> {
                    val retD = findD(downward, 7, distance, 0, path, endSynset)
                    val retH = findH(horizontal, 3, distance, 0, path, endSynset)
                    if (retD > retH) retD else retH
                }

                4 -> {
                    findD(downward, 4, distance, 1, path, endSynset)
                }

                5 -> {
                    val retD = findD(downward, 4, distance, 2, path, endSynset)
                    val retH = findH(horizontal, 5, distance, 1, path, endSynset)
                    if (retD > retH) retD else retH
                }

                6 -> {
                    findH(horizontal, 6, distance, 1, path, endSynset)
                }

                7 -> {
                    findD(downward, 7, distance, 1, path, endSynset)
                }

                else -> 0
            }
        }

        private fun findD(
            downward: Set<Concept>,
            state: Int,
            distance: Int,
            chdir: Int,
            path: String,
            endSynset: Concept
        ): Int {
            return find(downward, state, distance, chdir, path, endSynset, "D")
        }

        private fun findU(
            upward: Set<Concept>,
            state: Int,
            distance: Int,
            chdir: Int,
            path: String,
            endSynset: Concept
        ): Int {
            return find(upward, state, distance, chdir, path, endSynset, "U")
        }

        private fun findH(
            horizontal: Set<Concept>,
            state: Int,
            distance: Int,
            chdir: Int,
            path: String,
            endSynset: Concept
        ): Int {
            return find(horizontal, state, distance, chdir, path, endSynset, "H")
        }

        private fun find(
            synsetGroup: Set<Concept>,
            state: Int,
            distance: Int,
            chdir: Int,
            path: String,
            endSynset: Concept,
            abbreviation: String
        ): Int {
            var ret = 0
            for (synset in synsetGroup) {
                val retT = medStrong(
                    state,
                    distance + 1,
                    chdir,
                    synset,
                    "$path [$abbreviation] $synset",
                    endSynset
                )
                if (retT > ret) ret = retT
            }
            return ret
        }
    }

    companion object {
        private const val min = 0.0
        private const val max = 16.0

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