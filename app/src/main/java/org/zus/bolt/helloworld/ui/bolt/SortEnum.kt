package org.zus.bolt.helloworld.ui.bolt

enum class SortEnum {
    ASC,
    DESC
}

object Sort {
    fun getSort(sortEnum: SortEnum): String {
        return when (sortEnum) {
            SortEnum.ASC -> "asc"
            SortEnum.DESC -> "desc"
        }
    }
}
