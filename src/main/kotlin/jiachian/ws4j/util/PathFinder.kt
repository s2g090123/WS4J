package jiachian.ws4j.util

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.Link
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


class PathFinder(private val db: ILexicalDatabase) {
    fun getAllPaths(
        concept1: Concept,
        concept2: Concept,
        tracer: StringBuilder?
    ): List<Subsumer> {
        val paths = mutableListOf<Subsumer>()
        val lTrees = getHypernymTrees(concept1, HashSet())
        val rTrees = getHypernymTrees(concept2, HashSet())
        for (lTree in lTrees) {
            for (rTree in rTrees) {
                val subsumer = getSubsumerFromTrees(lTree, rTree) ?: continue
                var lCount = 0
                val lPath = mutableListOf<Concept>()
                val reversedLTree = lTree.reversed()
                for (synset in reversedLTree) {
                    lCount++
                    if (synset == subsumer) break
                    lPath.add(synset)
                }
                var rCount = 0
                val rPath = mutableListOf<Concept>()
                val reversedRTree = rTree.reversed()
                for (synset in reversedRTree) {
                    rCount++
                    if (synset == subsumer) break
                    rPath.add(synset)
                }
                paths.add(
                    Subsumer(
                        Concept(subsumer.synsetID, concept1.pos),
                        rCount + lCount - 1,
                        lPath,
                        rPath
                    )
                )
                if (tracer != null) {
                    tracer.append("HyperTree(").append(concept1).append(") = ").append(lTree).append("\n")
                    tracer.append("HyperTree(").append(concept2).append(") = ").append(rTree).append("\n")
                }
            }
        }
        paths.sortBy { it.pathLength }
        return paths
    }

    fun getShortestPaths(
        concept1: Concept,
        concept2: Concept,
        tracer: StringBuilder?
    ): List<Subsumer> {
        val returnList = mutableListOf<Subsumer>()
        val paths = getAllPaths(concept1, concept2, tracer)
        if (paths.isEmpty()) return returnList
        val bestLength = paths[0].pathLength
        returnList.add(paths[0])
        for (i in 1 until paths.size) {
            if (paths[i].pathLength > bestLength) break
            returnList.add(paths[i])
        }
        return returnList
    }

    fun getHypernymTrees(
        synset: Concept,
        history: MutableSet<Concept>
    ): List<List<Concept>> {
//        if (WS4JConfiguration.getInstance().useCache()) {
//			cache?.get(synset)?.let {
//                return clone(it)
//            }
//		}
        if (synset.synsetID == "0") {
            val trees = mutableListOf(listOf(Concept("0")))
//            if (WS4JConfiguration.getInstance().useCache()) {
//                cache?.put(synset, clone(trees))
//            }
            return trees
        }
        val synLinks = db.getLinkedSynsets(synset, Link.HYPERNYM)
        val returnList = mutableListOf<List<Concept>>()
        if (synLinks.isEmpty()) {
            val tree = listOf(Concept("0")) + synset
            returnList.add(tree)
        } else {
            for (hypernym in synLinks) {
                if (history.contains(hypernym)) continue
                history.add(hypernym)
                val hypernymTrees = getHypernymTrees(hypernym, history)
                for (hypernymTree in hypernymTrees) {
                    val list = hypernymTree + synset
                    returnList.add(list)
                }
                if (returnList.isEmpty()) {
                    val newList = listOf(Concept("0")) + synset
                    returnList.add(newList)
                }
            }
        }
//        if (WS4JConfiguration.getInstance().useCache()) {
//            cache?.put(synset, clone(returnList))
//        }
        return returnList
    }

    class Subsumer internal constructor(
        val subsumer: Concept,
        val pathLength: Int,
        val lPath: List<Concept>,
        val rPath: List<Concept>
    ) {
        var iC: Double = 0.0

        override fun toString(): String {
            return "{" +
                    "subsumer=" + subsumer +
                    ", pathLength=" + pathLength +
                    ", ic=" + iC +
                    ", lPath=" + lPath +
                    ", rPath=" + rPath +
                    '}'
        }
    }

    fun getRoot(synset: Concept): Concept? {
        val paths = getHypernymTrees(synset, mutableSetOf())
        return if (paths.isNotEmpty()) {
            if (paths[0].size > 1) paths[0][1] else paths[0][0]
        } else {
            null
        }
    }

    fun getLCSByPath(
        concept1: Concept,
        concept2: Concept,
        tracer: StringBuilder?
    ): List<Subsumer> {
        val paths = getAllPaths(concept1, concept2, tracer)
        val returnPaths = paths.filter { path -> path.pathLength <= paths[0].pathLength }
        return returnPaths
    }

    companion object {
        private var cache: ConcurrentMap<Concept, List<List<Concept>>>? =
            if (WS4JConfiguration.getInstance().useCache()) {
                ConcurrentHashMap()
            } else {
                null
            }

        private fun getSubsumerFromTrees(
            concepts1: List<Concept>,
            concepts2: List<Concept>
        ): Concept? {
            val tree1 = concepts1
                .map { obj -> obj.synsetID }
                .reversed()
            val tree2 = concepts2
                .map { obj -> obj.synsetID }
                .reversed()
            val tree1Joined = " " + tree1.joinToString(" ") + " "
            for (synset in tree2) {
                if (tree1Joined.contains(synset)) {
                    return Concept(synset)
                }
            }
            return null
        }

        private fun clone(originals: List<List<Concept>>): List<List<Concept>> {
            val clone = originals.map { it.toList() }
            return clone
        }
    }
}