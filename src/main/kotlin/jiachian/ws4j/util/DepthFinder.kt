package jiachian.ws4j.util

import com.google.gson.Gson
import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept


class DepthFinder(db: ILexicalDatabase) {
    private val pathFinder = PathFinder(db)

    fun getRelatedness(
        concept1: Concept,
        concept2: Concept,
        tracer: StringBuilder?
    ): List<Depth>? {
        val paths = pathFinder.getAllPaths(concept1, concept2, tracer)
        if (paths.isEmpty()) return null
        var depthList = ArrayList<Depth>(paths.size)
        for (s in paths) {
            val depths = getSynsetDepths(s.subsumer)
            if (depths.isNullOrEmpty()) return null
            val depth = depths[0]
            depthList.add(depth)
        }
        val toBeDeleted = mutableListOf<Depth>()
        for (d in depthList) {
            if (depthList[0].depth != d.depth) toBeDeleted.add(d)
        }
        depthList.removeAll(toBeDeleted)
        val map = LinkedHashMap<Int, Depth>(depthList.size)
        for (d in depthList) {
            val key = d.toString().hashCode()
            map[key] = d
        }
        depthList = ArrayList(map.values)
        return depthList
    }

    private fun getSynsetDepths(concept: Concept): List<Depth>? {
        val history = HashSet<Concept>()
        val hyperTrees = pathFinder.getHypernymTrees(concept, history)
        val depths = ArrayList<Depth>(hyperTrees.size)
        hyperTrees.forEach { hyperTree ->
            depths.add(Depth(concept, hyperTree.size, hyperTree[0]))
        }
        depths.sortBy { it.depth }
        return depths
    }

    class Depth internal constructor(
        val leaf: Concept,
        val depth: Int,
        val root: Concept?
    ) {
        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    fun getShortestDepth(concept: Concept): Int {
        return requireNotNull(getSynsetDepths(concept))[0].depth
    }
}
