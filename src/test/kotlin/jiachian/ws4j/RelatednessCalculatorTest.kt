package jiachian.ws4j

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.MITWordNet
import jiachian.lexical_db.item.POS
import jiachian.ws4j.util.WS4JConfiguration
import org.junit.Test

abstract class RelatednessCalculatorTest {
    protected val cycloneConcepts = db.getAllConcepts(CYCLONE, POS.NOUN)
    protected val hurricaneConcepts = db.getAllConcepts(HURRICANE, POS.NOUN)

    protected val migrateConcepts = db.getAllConcepts(MIGRATE, POS.VERB)
    protected val emigrateConcepts = db.getAllConcepts(EMIGRATE, POS.VERB)

    protected val hugeConcepts = db.getAllConcepts(HUGE, POS.ADJECTIVE)
    protected val tremendousConcepts = db.getAllConcepts(TREMENDOUS, POS.ADJECTIVE)

    protected val eventuallyConcepts = db.getAllConcepts(EVENTUALLY, POS.ADVERB)
    protected val finallyConcepts = db.getAllConcepts(FINALLY, POS.ADVERB)

    protected val manuscriptConcepts = db.getAllConcepts(MANUSCRIPT, POS.NOUN)
    protected val writeDownConcepts = db.getAllConcepts(WRITE_DOWN, POS.VERB)

    protected val rc: RelatednessCalculator
        get() = requireNotNull(Companion.rc)

    @Test
    abstract fun testHappyPathOnSynsets()

    @Test
    abstract fun testOnSameSynsets()

    @Test
    abstract fun testOnUnknownSynsets()

    @Test
    abstract fun testHappyPathOnWords()

    @Test
    abstract fun testHappyPathOnWordsWithPOS()

    @Test
    abstract fun testHappyPathOnWordsWithPOSAndSense()

    companion object {
        var db: ILexicalDatabase
        var rc: RelatednessCalculator? = null

        init {
            WS4JConfiguration.getInstance().setMemoryDB(false)
            WS4JConfiguration.getInstance().setLeskNormalize(false)
            WS4JConfiguration.getInstance().setMFS(false)
            db = MITWordNet()
        }

        const val CYCLONE = "cyclone"
        const val HURRICANE = "hurricane"
        const val MIGRATE = "migrate"
        const val EMIGRATE = "emigrate"
        const val HUGE = "huge"
        const val TREMENDOUS = "tremendous"
        const val EVENTUALLY = "eventually"
        const val FINALLY = "finally"
        const val MANUSCRIPT = "manuscript"
        const val WRITE_DOWN = "write down"
        const val CHAT = "chat"
        const val TALK = "talk"
    }
}
