package eliopi.nile

import java.sql.Date

class Poster() {

    var id: Long = 0

    lateinit var name: String

    lateinit var about: String

    lateinit var date: String

    constructor(name: String, about: String, date: String): this() {
        this.name = name
        this.about = about
        this.date = date
    }
}