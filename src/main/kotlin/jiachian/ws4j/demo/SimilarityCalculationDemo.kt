package jiachian.ws4j.demo

import jiachian.lexical_db.MITWordNet
import jiachian.ws4j.RelatednessCalculator
import jiachian.ws4j.similarity.*
import jiachian.ws4j.util.WS4JConfiguration


object SimilarityCalculationDemo {
    private val rcs: Array<RelatednessCalculator>

    init {
        WS4JConfiguration.getInstance().setMemoryDB(false)
        WS4JConfiguration.getInstance().setMFS(false)
        val db = MITWordNet()
        rcs = arrayOf(
            WuPalmer(db),
            JiangConrath(db),
            LeacockChodorow(db),
            Lin(db),
            Resnik(db),
            Path(db),
            Lesk(db),
            HirstStOnge(db)
        )
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val t = System.currentTimeMillis()
        rcs.forEach { rc ->
            println(rc.javaClass.getName() + "\t" + rc.calcRelatednessOfWords("act", "moderate"))
        }
        println("Done in ${System.currentTimeMillis() - t} msec.")
    }
}