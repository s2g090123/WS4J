package jiachian.ws4j.util

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.Link
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class Traverser(private val db: ILexicalDatabase) {
    fun contained(
        concept1: Concept?,
        concept2: Concept?
    ): Boolean {
        if (concept1 == null || concept2 == null) return false
        val wordsH = db.getWords(concept1)
        val wordsN = db.getWords(concept2)
        for (wordH in wordsH) {
            for (wordN in wordsN) if (wordH.contains(wordN) || wordN.contains(wordH)) return true
        }
        return false
    }

    fun getHorizontalSynsets(synset: Concept): Set<Concept> {
        if (WS4JConfiguration.getInstance().useCache()) {
            horizonCache?.get(synset)?.let {
                return it
            }
        }
        val points = ArrayList<Link>()
        points.add(Link.ANTONYM)
        points.add(Link.ATTRIBUTE)
        points.add(Link.SIMILAR_TO)
        val result = getGroupedSynsets(synset, points)
        if (WS4JConfiguration.getInstance().useCache()) {
            horizonCache?.put(synset, result)
        }
        return result
    }

    fun getUpwardSynsets(synset: Concept): Set<Concept> {
        if (WS4JConfiguration.getInstance().useCache()) {
            upwardCache?.get(synset)?.let {
                return it
            }
        }
        val points = ArrayList<Link>()
        points.add(Link.HYPERNYM)
        points.add(Link.MERONYM)
        val result = getGroupedSynsets(synset, points)
        if (WS4JConfiguration.getInstance().useCache()) {
            upwardCache?.put(synset, result)
        }
        return result
    }

    fun getDownwardSynsets(synset: Concept): Set<Concept> {
        if (WS4JConfiguration.getInstance().useCache()) {
            downwardCache?.get(synset)?.let {
                return it
            }
        }
        val points = ArrayList<Link>()
        points.add(Link.CAUSE)
        points.add(Link.ENTAILMENT)
        points.add(Link.HOLONYM)
        points.add(Link.HYPONYM)
        val result = getGroupedSynsets(synset, points)
        if (WS4JConfiguration.getInstance().useCache()
        ) {
            downwardCache?.put(synset, result)
        }
        return result
    }

    private fun getGroupedSynsets(
        synset: Concept,
        points: List<Link>
    ): Set<Concept> {
        val synsets = HashSet<Concept>()
        points.forEach { point ->
            synsets.addAll(db.getLinkedSynsets(synset, point))
        }
        return synsets
    }

    companion object {
        private var horizonCache: ConcurrentMap<Concept, Set<Concept>>? = null
        private var upwardCache: ConcurrentMap<Concept, Set<Concept>>? = null
        private var downwardCache: ConcurrentMap<Concept, Set<Concept>>? = null

        init {
            if (WS4JConfiguration.getInstance().useCache()) {
                horizonCache = ConcurrentHashMap()
                downwardCache = ConcurrentHashMap()
                upwardCache = ConcurrentHashMap()
            }
        }
    }
}