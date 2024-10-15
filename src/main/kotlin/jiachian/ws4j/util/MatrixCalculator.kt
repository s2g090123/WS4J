package jiachian.ws4j.util

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.item.POS
import jiachian.ws4j.RelatednessCalculator

class MatrixCalculator(db: ILexicalDatabase) {
    init {
        Companion.db = db
    }

    companion object {
        private var db: ILexicalDatabase? = null

        fun getSimilarityMatrix(
            words1: Array<String>,
            words2: Array<String>,
            rc: RelatednessCalculator
        ): Array<DoubleArray> {
            val result = Array(words1.size) { DoubleArray(words2.size) }
            for (i in words1.indices) {
                for (j in words2.indices) {
                    val score = rc.calcRelatednessOfWords(words1[i], words2[j])
                    result[i][j] = score
                }
            }
            return result
        }

        fun getNormalizedSimilarityMatrix(
            words1: Array<String>,
            words2: Array<String>,
            rc: RelatednessCalculator
        ): Array<DoubleArray> {
            val scores = getSimilarityMatrix(words1, words2, rc)
            var bestScore = 1.0
            for (score in scores) {
                for (aScore in score) {
                    if (aScore > bestScore && aScore != Double.MAX_VALUE) bestScore = aScore
                }
            }
            for (i in scores.indices) {
                for (j in scores[i].indices) {
                    if (scores[i][j] == Double.MAX_VALUE) scores[i][j] = 1.0
                    else scores[i][j] /= bestScore
                }
            }
            return scores
        }

        fun getSynonymyMatrix(words1: Array<String>, words2: Array<String>): Array<DoubleArray> {
            val synonyms1 = ArrayList<Set<String>>(words1.size)
            words1.forEach { aWords1 ->
                val synonyms = mutableSetOf<String>()
                POS.values().forEach { pos ->
                    db?.getAllConcepts(aWords1, pos)?.forEach { concept ->
                        synonyms.add(concept.synsetID)
                    }
                }
                synonyms1.add(synonyms)
            }
            val synonyms2 = ArrayList<Set<String>>(words2.size)
            words2.forEach { aWords2 ->
                val synonyms = mutableSetOf<String>()
                POS.values().forEach { pos ->
                    db?.getAllConcepts(aWords2, pos)?.forEach { concept ->
                        synonyms.add(concept.synsetID)
                    }
                }
                synonyms2.add(synonyms)
            }
            val result = Array(words1.size) { DoubleArray(words2.size) }
            for (i in words1.indices) {
                for (j in words2.indices) {
                    val w1 = words1[i]
                    val w2 = words2[j]
                    if (w1 == w2) {
                        result[i][j] = 1.0
                        continue
                    }
                    val s1 = synonyms1[i]
                    val s2 = synonyms2[j]
                    result[i][j] = if ((s1.contains(w2) || s2.contains(w1))) 1.0 else 0.0
                }
            }
            return result
        }
    }
}