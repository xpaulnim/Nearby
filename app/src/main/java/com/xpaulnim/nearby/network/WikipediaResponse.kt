package com.xpaulnim.nearby.network

data class Position(val latitude: Double, val longitude: Double, val radius: Int = 1000) {
    val ggscoord: String = "${latitude}|${longitude}"
}

data class BoundingBox(val top: Double, val left: Double, val bottom: Double, val right: Double) {
    val ggsbbox: String = "${top}|${left}|${bottom}|${right}"
}

data class WikipediaResponse(
    val batchcomplete: Boolean?,
    val query: QueryResult?
)

data class QueryResult(
    val geosearch: List<GeoSearchResult>?,
    // TODO: Should this be a separate class?
    val pages: List<PageSearchResult>?
)

data class GeoSearchResult(
    val pageid: Int,
    val ns: Int,
    val title: String,
    val lat: Double,
    val lon: Double,
    val dist: Double,
    val primary: Boolean
)

data class PageSearchResult(
    val pageid: Int,
    val ns: Int,
    val title: String,
    val extract: String?,                   // prop=extracts
    val thumbnail: WikiThumbnail?,          // prop=pageimages
    val pageimage: String?,                 // prop=pageimages
    val coordinates: List<WikiCoordinate>?, // prop=coordinates
    val description: String?,               // prop=description
    val descriptionsource: String?,         // prop=description
)

data class WikiThumbnail(
    val source: String?,
    val width: String?,
    val height: String?
)

data class WikiCoordinate(
    val lat: Double?,
    val lon: Double?,
    val primary: Boolean?,
    val globe: String?
)
