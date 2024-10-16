package jiachian.ws4j.util

import jiachian.lexical_db.item.POS
import jiachian.ws4j.RelatednessCalculator
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class WordSimilarityCalculator {
    private val cache = if (WS4JConfiguration.getInstance().useCache()) {
        ConcurrentHashMap<String, Double>()
    } else {
        null
    }

    private fun normalize(word: String): String {
        return word
            .lowercase(Locale.getDefault())
            .replace(' ', '_')
    }

    fun calcRelatednessOfWords(
        word1: String?,
        word2: String?,
        rc: RelatednessCalculator
    ): Double {
        if (word1 == word2) return rc.max
        if (word1.isNullOrEmpty() || word2.isNullOrEmpty()) return rc.min

        var normalizeWord1 = normalize(word1)
        var normalizeWord2 = normalize(word2)

        val key = "$normalizeWord1 & $normalizeWord2"
        if (WS4JConfiguration.getInstance().useCache()) {
            cache?.get(key)?.let {
                return it
            }
            val reverseKey = "$normalizeWord2 & $normalizeWord1"
            cache?.get(reverseKey)?.let {
                return it
            }
        }

        var pos1: POS? = null
        var sense1 = 0
        val offset1POS = normalizeWord1.indexOf(SEPARATOR)
        val offset1Sense = normalizeWord1.lastIndexOf(SEPARATOR)
        if (offset1POS != -1) {
            pos1 = POS.getPOS(normalizeWord1[offset1POS + 1]) ?: return rc.min
            if (offset1Sense != -1 && offset1POS != offset1Sense) {
                sense1 = Character.getNumericValue(normalizeWord1[offset1Sense + 1])
                if (sense1 == 0) {
                    throw IllegalArgumentException("Sense number must be greater than 0")
                }
            }
            normalizeWord1 = normalizeWord1.substring(0, offset1POS)
            if (sense1 > 0) {
                val word1Senses = rc.lexicalDB.getAllConcepts(normalizeWord1, pos1).size
                if (sense1 > word1Senses) {
                    throw IllegalArgumentException(
                        if (word1Senses == 1) "Invalid sense number. The word " +
                                '\"' + normalizeWord1 + '\"' + " has only one sense in WordNet" else "Sense number not found in WordNet. " +
                                "Enter a sense number less or " + "equal than " + word1Senses + " for " + "\"" + normalizeWord1 + "\"."
                    )
                }
            }
        }

        var pos2: POS? = null
        var sense2 = 0
        val offset2POS = normalizeWord2.indexOf(SEPARATOR)
        val offset2Sense = normalizeWord2.lastIndexOf(SEPARATOR)
        if (offset2POS != -1) {
            pos2 = POS.getPOS(normalizeWord2[offset2POS + 1]) ?: return rc.min
            if (offset2Sense != -1 && offset2POS != offset2Sense) {
                sense2 = Character.getNumericValue(normalizeWord2[offset2Sense + 1])
                if (sense2 == 0) {
                    throw IllegalArgumentException("Sense number must be greater than 0")
                }
            }
            normalizeWord2 = normalizeWord2.substring(0, offset2POS)
            if (sense2 > 0) {
                val word2Senses = rc.lexicalDB.getAllConcepts(normalizeWord2, pos2).size
                if (sense2 > word2Senses) {
                    throw IllegalArgumentException(
                        if (word2Senses == 1) "Invalid sense number. The word " +
                                '\"' + normalizeWord2 + '\"' + " has only one sense in WordNet" else "Sense number not found in WordNet. " +
                                "Enter a sense number less or " + "equal than " + word2Senses + " for " + "\"" + normalizeWord2 + "\"."
                    )
                }
            }
        }

        var maxScore = -1.0
        for (posPair in rc.posPairs) {
            if (pos1 != null && pos1 != posPair[0]) continue
            if (pos2 != null && pos2 != posPair[1]) continue
            if (sense1 > 0 && sense2 > 0) {
                maxScore = rc.calcRelatednessOfSynsets(
                    rc.lexicalDB.getConcept(normalizeWord1, posPair[0], sense1),
                    rc.lexicalDB.getConcept(normalizeWord2, posPair[1], sense2)
                ).score
            } else if (sense1 == 0 && sense2 > 0) {
                for (concept in rc.lexicalDB.getAllConcepts(normalizeWord1, posPair[0])) {
                    val relatedness = rc.calcRelatednessOfSynsets(
                        concept,
                        rc.lexicalDB.getConcept(normalizeWord2, posPair[1], sense2)
                    )
                    val score = relatedness.score
                    if (score > maxScore) maxScore = score
                }
            } else if (sense1 > 0 && sense2 == 0) {
                for (concept in rc.lexicalDB.getAllConcepts(normalizeWord2, posPair[1])) {
                    val relatedness = rc.calcRelatednessOfSynsets(
                        rc.lexicalDB.getConcept(normalizeWord1, posPair[0], sense1),
                        concept
                    )
                    val score = relatedness.score
                    if (score > maxScore) maxScore = score
                }
            } else if (WS4JConfiguration.getInstance().useMFS()) {
                val concept1 = rc.lexicalDB.getConcept(
                    normalizeWord1,
                    posPair[0],
                    1
                )
                val concept2 = rc.lexicalDB.getConcept(
                    normalizeWord2,
                    posPair[1],
                    1
                )
                val score = rc.calcRelatednessOfSynsets(concept1, concept2).score
                if (score > maxScore) maxScore = score
            } else {
                for (concept1 in rc.lexicalDB.getAllConcepts(normalizeWord1, posPair[0])) {
                    for (concept2 in rc.lexicalDB.getAllConcepts(normalizeWord2, posPair[1])) {
                        val relatedness = rc.calcRelatednessOfSynsets(concept1, concept2)
                        val score = relatedness.score
                        if (score > maxScore) maxScore = score
                    }
                }
            }
        }
        if (maxScore == -1.0) maxScore = 0.0
        maxScore = abs(maxScore)
        check(maxScore >= rc.min && maxScore <= rc.max)
        if (WS4JConfiguration.getInstance().useCache()) {
            cache?.put(key, maxScore)
        }
        return maxScore
    }

    companion object {
        const val SEPARATOR: Char = '#'
    }
}