package jiachian.lexical_db.data

import com.google.common.base.Objects
import jiachian.lexical_db.item.POS

data class Concept(
    val synsetID: String,
    var pos: POS? = null,
    var name: String? = null,
) {
    override fun toString(): String {
        return synsetID
    }

    override fun equals(other: Any?): Boolean {
        return other is Concept && other.synsetID == synsetID
    }

    override fun hashCode(): Int {
        return Objects.hashCode(synsetID)
    }
}
