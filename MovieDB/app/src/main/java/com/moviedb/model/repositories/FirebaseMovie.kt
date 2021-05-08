package com.moviedb.model.repositories

class FirebaseMovie : Comparable<FirebaseMovie>{
    var id : String = ""
    var backdropPath: String? = null
    var overview: String? = null
    var title: String = ""
    var score: String = ""
    var watched: Boolean = false
    var additionDate : String = ""

    override fun compareTo(other: FirebaseMovie): Int {
        return(this.additionDate.compareTo(other.additionDate))
    }
}