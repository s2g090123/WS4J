package jiachian.lexical_db

import jiachian.lexical_db.data.Concept
import jiachian.lexical_db.item.Link
import jiachian.lexical_db.item.POS


interface ILexicalDatabase {
    fun getConcept(
        lemma: String,
        pos: POS,
        sense: Int
    ): Concept?

    fun getAllConcepts(
        lemma: String,
        pos: POS
    ): List<Concept>

    fun getLinkedSynsets(
        concept: Concept,
        link: Link?
    ): List<Concept>

    fun getWords(concept: Concept): List<String>

    fun getGloss(
        concept: Concept,
        link: Link?
    ): List<String>
}