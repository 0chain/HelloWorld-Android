package org.zus.bolt.helloworld.models.selectapp

data class DetailsModel(
    val title: String,
    val value: String,
    val showArrowButton: Boolean
)

data class DetailsListModel(
    val title: String,
    val detailsList: List<DetailsModel>
)
