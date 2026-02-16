package fi.kranu

/**
 * Value Object Modeling:
 * We use a data class to represent a "User". 
 * It's a value object because its identity is defined by its data.
 * By using 'val', we ensure immutability.
 *
 * data class creates methods like equals(), hashCode() and toString() automatically
 */
data class User(
    val id: Int,
    val name: String,
    val email: String
)

fun main() {
    // 1. Creation
    val user1 = User(1, "Alice", "alice@example.com")
    println("Original User: $user1")

    // 2. Immutability & copy()
    // Instead of changing user1 (which is impossible with 'val'), 
    // we create a new instance with specific changes.
    val user2 = user1.copy(name = "Alice Smith")
    println("Updated User (via copy): $user2")
    println("Are they the same instance? ${user1 === user2}") // false

    // 3. Destructuring
    // Data classes allow unpacking properties into separate variables.
    // Warning: Apparently this is just by ordering
    val (id, name, email) = user2
    println("Destructured: ID=$id, Name=$name, Email=$email")

    // 4. Component functions (used under the hood by destructuring)
    val nameViaComponent = user2.component2()
    println("Name via component2(): $nameViaComponent")

    with(user2) {
        println("User details: ID=$id, Name=$name, Email=$email")
    }
}