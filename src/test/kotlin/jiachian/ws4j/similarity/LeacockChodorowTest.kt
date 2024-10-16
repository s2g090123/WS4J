package jiachian.ws4j.similarity

import jiachian.lexical_db.item.POS
import jiachian.ws4j.RelatednessCalculatorTest
import jiachian.ws4j.util.WordSimilarityCalculator
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

/**
 * Test class for [LeacockChodorow].
 *
 * @author Hideki Shima
 */
class LeacockChodorowTest : RelatednessCalculatorTest() {
    @Test
    override fun testHappyPathOnSynsets() {
        assertEquals(2.9957, rc.calcRelatednessOfSynsets(cycloneConcepts[1], hurricaneConcepts[0]).score, 0.0001)
        assertEquals(1.6094, rc.calcRelatednessOfSynsets(cycloneConcepts[0], hurricaneConcepts[0]).score, 0.0001)
        assertEquals(2.6391, rc.calcRelatednessOfSynsets(migrateConcepts[0], emigrateConcepts[0]).score, 0.0001)
        assertEquals(1.9459, rc.calcRelatednessOfSynsets(migrateConcepts[1], emigrateConcepts[0]).score, 0.0001)
    }

    @Test
    override fun testOnSameSynsets() {
        assertEquals(rc.max, rc.calcRelatednessOfSynsets(cycloneConcepts[0], cycloneConcepts[0]).score, 0.0001)
    }

    @Test
    override fun testOnUnknownSynsets() {
        assertEquals(rc.min, rc.calcRelatednessOfSynsets(null, cycloneConcepts[0]).score, 0.0001)
        assertEquals(rc.min, rc.calcRelatednessOfWords(null, CYCLONE), 0.0001)
        assertEquals(rc.min, rc.calcRelatednessOfWords("", CYCLONE), 0.0001)
    }

    @Test
    override fun testHappyPathOnWords() {
        assertEquals(2.9957, rc.calcRelatednessOfWords(CYCLONE, HURRICANE), 0.0001)
        assertEquals(2.6391, rc.calcRelatednessOfWords(MIGRATE, EMIGRATE), 0.0001)
    }

    @Test
    override fun testHappyPathOnWordsWithPOS() {
        assertEquals(
            2.5902,
            rc.calcRelatednessOfWords(
                CHAT + WordSimilarityCalculator.SEPARATOR + POS.NOUN,
                TALK + WordSimilarityCalculator.SEPARATOR + POS.NOUN
            ),
            0.0001
        )
        assertEquals(
            0.0000,
            rc.calcRelatednessOfWords(
                CHAT + WordSimilarityCalculator.SEPARATOR + POS.NOUN,
                TALK + WordSimilarityCalculator.SEPARATOR + POS.VERB
            ),
            0.0001
        )
        assertEquals(
            0.0000,
            rc.calcRelatednessOfWords(
                CHAT + WordSimilarityCalculator.SEPARATOR + POS.VERB,
                TALK + WordSimilarityCalculator.SEPARATOR + POS.NOUN
            ),
            0.0001
        )
        assertEquals(
            2.2335,
            rc.calcRelatednessOfWords(
                CHAT + WordSimilarityCalculator.SEPARATOR + POS.VERB,
                TALK + WordSimilarityCalculator.SEPARATOR + POS.VERB
            ),
            0.0001
        )
        assertEquals(
            0.0000,
            rc.calcRelatednessOfWords(
                CHAT + WordSimilarityCalculator.SEPARATOR + "other",
                TALK + WordSimilarityCalculator.SEPARATOR + "other"
            ),
            0.0001
        )
    }

    @Test
    override fun testHappyPathOnWordsWithPOSAndSense() {
        assertEquals(
            2.9957,
            rc.calcRelatednessOfWords(
                CYCLONE + WordSimilarityCalculator.SEPARATOR + POS.NOUN + WordSimilarityCalculator.SEPARATOR + 2,
                HURRICANE + WordSimilarityCalculator.SEPARATOR + POS.NOUN + WordSimilarityCalculator.SEPARATOR + 1
            ),
            0.0001
        )
        assertEquals(
            1.6094,
            rc.calcRelatednessOfWords(
                CYCLONE + WordSimilarityCalculator.SEPARATOR + POS.NOUN + WordSimilarityCalculator.SEPARATOR + 1,
                HURRICANE + WordSimilarityCalculator.SEPARATOR + POS.NOUN + WordSimilarityCalculator.SEPARATOR + 1
            ),
            0.0001
        )
        assertEquals(
            2.6391,
            rc.calcRelatednessOfWords(
                MIGRATE + WordSimilarityCalculator.SEPARATOR + POS.VERB + WordSimilarityCalculator.SEPARATOR + 1,
                EMIGRATE + WordSimilarityCalculator.SEPARATOR + POS.VERB + WordSimilarityCalculator.SEPARATOR + 1
            ),
            0.0001
        )
        assertEquals(
            1.9459,
            rc.calcRelatednessOfWords(
                MIGRATE + WordSimilarityCalculator.SEPARATOR + POS.VERB + WordSimilarityCalculator.SEPARATOR + 2,
                EMIGRATE + WordSimilarityCalculator.SEPARATOR + POS.VERB + WordSimilarityCalculator.SEPARATOR + 1
            ),
            0.0001
        )
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun oneTimeSetUp() {
            rc = LeacockChodorow(db)
        }
    }
}
