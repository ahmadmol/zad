package com.example.feature.fahmanallah.domain.model

/** User ordering is separate from the verified source playlist metadata and progress records. */
object FahmPlaylistOrder {
    fun orderedIds(catalog: List<String>, stored: List<String>): List<String> {
        val known=catalog.toSet()
        return (stored.filter { it in known } + catalog).distinct()
    }
    fun move(catalog: List<String>, stored: List<String>, id: String, direction: Int): List<String> {
        require(direction == -1 || direction == 1)
        require(id in catalog) { "الدرس غير موجود" }
        val ordered=orderedIds(catalog,stored).toMutableList()
        val index=ordered.indexOf(id)
        val target=index+direction
        if(target in ordered.indices) java.util.Collections.swap(ordered,index,target)
        return ordered + stored.filter { it !in catalog }.distinct()
    }
}
