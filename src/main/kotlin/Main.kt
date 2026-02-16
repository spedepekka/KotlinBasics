package fi.kranu

data class User(
    val id: Int,
    val name: String,
    val email: String
)

/**
 * Extension Function:
 * We "extend" the String class with a new function called 'shout'.
 * Inside the function, 'this' refers to the actual string instance.
 */
fun String.shout(): String {
    return this.uppercase() + "!!!"
}

/**
 * Extension Function for a custom class:
 * Formats user info for display.
 */
fun User.getDisplayName(): String {
    return "$name <$email> (ID: $id)"
}

/**
 * Nullable Extension Function:
 * Can be called even if the reference is null.
 */
fun String?.orEmptyMessage(): String {
    return this ?: "No message provided"
}

fun main() {
    // 1. Using extension on standard type
    val greeting = "hello world"
    println(greeting.shout()) // HELLO WORLD!!!

    // 2. Using extension on custom type
    val user = User(1, "Alice", "alice@example.com")
    println(user.getDisplayName())

    // 3. Using nullable extension
    val nullableString: String? = null
    println(nullableString.orEmptyMessage()) // No message provided
    
    val actualString: String? = "Kotlin"
    println(actualString.orEmptyMessage()) // Kotlin
}