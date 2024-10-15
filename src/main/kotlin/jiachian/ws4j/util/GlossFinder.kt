package jiachian.ws4j.util

import jiachian.lexical_db.ILexicalDatabase
import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.Link

class GlossFinder(private val db: ILexicalDatabase) {
    fun getSuperGlosses(
        concept1: Concept,
        concept2: Concept
    ): List<SuperGloss> {
        val glosses = linkPairs
            .map { links ->
                SuperGloss(
                    db.getGloss(concept1, links[0]),
                    db.getGloss(concept2, links[1]),
                    links[0]?.id ?: " ",
                    links[1]?.id ?: " ",
                    1.0
                )
            }
        return glosses
    }

    class SuperGloss internal constructor(
        val gloss1: List<String>,
        val gloss2: List<String>,
        val link1: String,
        val link2: String,
        val weight: Double
    )

    companion object {
        private val linkPairs: List<Array<Link?>> =
            object : ArrayList<Array<Link?>>() {
                init {
                    add(arrayOf(null, null))
                    add(arrayOf(null, Link.HYPERNYM))
                    add(arrayOf(null, Link.HYPONYM))
                    add(arrayOf(null, Link.MERONYM))
                    add(arrayOf(null, Link.HOLONYM))

                    add(arrayOf(Link.HYPERNYM, null))
                    add(arrayOf(Link.HYPERNYM, Link.HYPERNYM))
                    add(arrayOf(Link.HYPERNYM, Link.HYPONYM))
                    add(arrayOf(Link.HYPERNYM, Link.MERONYM))
                    add(arrayOf(Link.HYPERNYM, Link.HOLONYM))

                    add(arrayOf(Link.HYPONYM, null))
                    add(arrayOf(Link.HYPONYM, Link.HYPERNYM))
                    add(arrayOf(Link.HYPONYM, Link.HYPONYM))
                    add(arrayOf(Link.HYPONYM, Link.MERONYM))
                    add(arrayOf(Link.HYPONYM, Link.HOLONYM))

                    add(arrayOf(Link.MERONYM, null))
                    add(arrayOf(Link.MERONYM, Link.HYPERNYM))
                    add(arrayOf(Link.MERONYM, Link.HYPONYM))
                    add(arrayOf(Link.MERONYM, Link.MERONYM))
                    add(arrayOf(Link.MERONYM, Link.HOLONYM))

                    add(arrayOf(Link.SYNSET, null))
                    add(arrayOf(Link.SYNSET, Link.HYPERNYM))
                    add(arrayOf(Link.SYNSET, Link.HYPONYM))
                    add(arrayOf(Link.SYNSET, Link.MERONYM))
                    add(arrayOf(Link.SYNSET, Link.HOLONYM))
                }
            }
    }
}