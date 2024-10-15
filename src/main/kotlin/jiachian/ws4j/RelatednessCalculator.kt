package jiachian.ws4j

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.POS
import jiachian.ws4j.util.DepthFinder
import jiachian.ws4j.util.MatrixCalculator
import jiachian.ws4j.util.PathFinder
import jiachian.ws4j.util.WordSimilarityCalculator

abstract class RelatednessCalculator(
    protected var db: ILexicalDatabase,
    val min: Double,
    val max: Double
) {
    protected var pathFinder = PathFinder(db)
    protected var depthFinder = DepthFinder(db)

    private val wordSimilarity: WordSimilarityCalculator = WordSimilarityCalculator()

    protected abstract fun calcRelatedness(
        concept1: Concept?,
        concept2: Concept?
    ): Relatedness

    abstract val posPairs: List<Array<POS>>

    fun calcRelatednessOfSynsets(
        concept1: Concept?,
        concept2: Concept?
    ): Relatedness {
        val t = System.currentTimeMillis()
        val r: Relatedness = calcRelatedness(concept1, concept2)
        r.appendTrace("Process done in " + (System.currentTimeMillis() - t) + " msec.\n")
        return r
    }

    fun calcRelatednessOfWords(word1: String, word2: String): Double {
        return wordSimilarity.calcRelatednessOfWords(word1, word2, this)
    }

    fun getSimilarityMatrix(words1: Array<String>, words2: Array<String>): Array<DoubleArray> {
        return MatrixCalculator.getSimilarityMatrix(words1, words2, this)
    }

    fun getNormalizedSimilarityMatrix(words1: Array<String>, words2: Array<String>): Array<DoubleArray> {
        return MatrixCalculator.getNormalizedSimilarityMatrix(words1, words2, this)
    }

    val lexicalDB: ILexicalDatabase
        get() = db

    companion object {
        const val illegalSynset = "Synset is null."
        const val identicalSynset = "Synsets are identical."

        const val useRootNode: Boolean = true
    }
}