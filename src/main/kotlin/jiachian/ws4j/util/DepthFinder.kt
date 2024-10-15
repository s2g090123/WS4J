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
        var depthList = mutableListOf<Depth>()
        for (s in paths) {
            val depths = getSynsetDepths(s.subsumer)
            if (depths.isEmpty()) return null
            val depth = depths[0]
            depthList.add(depth)
        }
        val toBeDeleted = depthList.filter { d -> depthList[0].depth != d.depth }
        depthList.removeAll(toBeDeleted)
        val map = mutableMapOf<Int, Depth>()
        for (d in depthList) {
            val key = d.toString().hashCode()
            map[key] = d
        }
        depthList = ArrayList(map.values)
        return depthList
    }

    private fun getSynsetDepths(concept: Concept): List<Depth> {
        val hyperTrees = pathFinder.getHypernymTrees(concept, mutableSetOf())
        val depths = hyperTrees
            .map { hyperTree -> Depth(concept, hyperTree.size, hyperTree[0]) }
            .sortedBy { it.depth }
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
        return getSynsetDepths(concept)[0].depth
    }
}
