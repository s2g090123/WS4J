package jiachian.ws4j

import jiachian.lexical_db.MITWordNet
import jiachian.ws4j.similarity.*
import jiachian.ws4j.util.MatrixCalculator


/**
 * This is a facade class that provides simple APIs for end users.
 *
 * @author Hideki Shima
 */
object WS4J {
    private var lin: RelatednessCalculator
    private var wup: RelatednessCalculator
    private var hso: RelatednessCalculator
    private var lch: RelatednessCalculator
    private var jcn: RelatednessCalculator
    private var lesk: RelatednessCalculator
    private var path: RelatednessCalculator
    private var res: RelatednessCalculator

    init {
        val db = MITWordNet()
        lin = Lin(db)
        wup = WuPalmer(db)
        hso = HirstStOnge(db)
        lch = LeacockChodorow(db)
        jcn = JiangConrath(db)
        lesk = Lesk(db)
        path = Path(db)
        res = Resnik(db)
    }

    fun runHSO(word1: String, word2: String): Double {
        return hso.calcRelatednessOfWords(word1, word2)
    }

    fun runLCH(word1: String, word2: String): Double {
        return lch.calcRelatednessOfWords(word1, word2)
    }

    fun runRES(word1: String, word2: String): Double {
        return res.calcRelatednessOfWords(word1, word2)
    }

    fun runJCN(word1: String, word2: String): Double {
        return jcn.calcRelatednessOfWords(word1, word2)
    }

    fun runLIN(word1: String, word2: String): Double {
        return lin.calcRelatednessOfWords(word1, word2)
    }

    fun runLESK(word1: String, word2: String): Double {
        return lesk.calcRelatednessOfWords(word1, word2)
    }

    fun runPATH(word1: String, word2: String): Double {
        return path.calcRelatednessOfWords(word1, word2)
    }

    fun runWUP(word1: String, word2: String): Double {
        return wup.calcRelatednessOfWords(word1, word2)
    }

    fun getSynonymyMatrix(words1: Array<String>, words2: Array<String>): Array<DoubleArray> {
        return MatrixCalculator.getSynonymyMatrix(words1, words2)
    }
}