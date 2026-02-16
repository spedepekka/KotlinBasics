package fi.kranu

fun main() {
    // Abusing the language :D
    var name: String? = "Kotlin"
    name = null
    println("Hello, $name!")

    println("length = ${name?.length ?: 0}")

    name = "LOL"
    println("length = ${name!!.length}")

    for (i in 1..3) {
        println("i = $i")
    }
}