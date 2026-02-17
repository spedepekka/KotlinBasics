# KotlinBasics

Here is a simple Kotlin program to demonstrate the basics of Kotlin. Some of them have been used in this repo in current code or in earlier commits.

Kotlin’s core strengths include:

* Strong null safety
* Functional + OO hybrid design
* Concise syntax with expressive APIs
* Coroutines for structured concurrency
* Type-safe DSL capabilities

These features collectively make Kotlin highly suitable for backend services, Android development, multiplatform systems, and modern concurrent applications.

## 1. Data Classes

What:  
Special classes designed to hold data with automatically generated equals(), hashCode(), toString(), and copy() methods.

Why:  
Reduces boilerplate for DTOs, entities, and value objects.

Example:

```kotlin
data class User(val id: Long, val name: String, val email: String? = null)

// Usage
val user1 = User(1, "Alice")
val user2 = user1.copy(email = "alice@example.com")
println(user1) // User(id=1, name=Alice, email=null)
```

In Spring Boot, data classes are perfect for request/response DTOs or domain value objects.

## 2. Extension Functions and Properties

What:  
Add new functions or properties to existing classes without inheritance or modifying the original class.

Why:  
Enhances readability and modularity; great for domain-specific utilities.

Example:

```kotlin
fun String.isValidEmail(): Boolean = this.contains("@") && this.contains(".")

val email = "test@example.com"
println(email.isValidEmail()) // true
```

In Spring, you might add extension functions to HttpServletRequest or ResponseEntity for cleaner controller code.

## 3. Sealed Classes and Interfaces

What:  
Restricted class hierarchies where all subclasses are known at compile time.

Why:  
Perfect for modeling domain states, events, or results with exhaustive when expressions.

Example:

```kotlin
sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    data class Failure(val error: String) : PaymentResult()
}

fun handlePayment(result: PaymentResult) = when(result) {
    is PaymentResult.Success -> println("Success: ${result.transactionId}")
    is PaymentResult.Failure -> println("Failure: ${result.error}")
}
```

This pattern is great for domain events or API response modeling.

## 4. Smart Casts

What:  
Kotlin automatically casts types after type checks, reducing explicit casting.

Why:  
Cleaner and safer code.

Example:

```kotlin
fun printLength(obj: Any) {
    if (obj is String) {
        // No explicit cast needed
        println(obj.length)
    }
}
```

In Spring controllers or services, this reduces verbosity when handling polymorphic inputs.

## 5. Destructuring Declarations

What:  
Unpack data class properties or other components into variables.

Why:  
Improves readability when extracting multiple values.

Example:

```kotlin
data class User(val id: Long, val name: String)

val user = User(1, "Alice")
val (id, name) = user
println("User $name has id $id")
```

Useful in service layers or mapping database rows to domain objects.

## 6. Default and Named Arguments

What:  
Functions can have default parameter values and callers can specify arguments by name.

Why:  
Simplifies function calls and improves readability.

Example:

```kotlin
fun sendEmail(to: String, subject: String = "No Subject", body: String = "") {
// send email logic
}

sendEmail(to = "user@example.com", body = "Hello!") // subject uses default
```

In Spring Boot, this reduces overloads and clarifies intent in service methods.

## 7. Inline Functions and Reified Type Parameters

What:  
inline functions reduce overhead of lambdas; reified allows access to type parameters at runtime.

Why:  
Enables powerful DSLs and type-safe builders.

Example:

```kotlin
inline fun <reified T> Gson.fromJson(json: String): T =
this.fromJson(json, T::class.kotlin)

// Usage
val user: User = gson.fromJson(jsonString)
```

In Spring, useful for generic JSON parsing or building type-safe APIs.

## 8. Companion Objects and Object Declarations

What:  
Singleton-like objects and static members.

Why:  
Replace kotlin static methods and singletons idiomatically.

Example:

```kotlin
class User {
    companion object {
        fun createGuest() = User()
    }
}

val guest = User.createGuest()
```

In Spring, companion objects can hold factory methods or constants.

## 9. Collections and Functional APIs

What:  
Rich standard library for collections with functional operations like map, filter, fold.

Why:  
Write concise, expressive data transformations.

Example:

```kotlin
val users = listOf(User(1, "Alice"), User(2, "Bob"))
val names = users.filter { it.id > 1 }.map { it.name }
println(names) // [Bob]
```

In Spring services, this reduces imperative loops and improves readability.

## 10. Scope Functions (let, run, apply, also, with)

What:  
Functions that provide context for executing blocks with the receiver object.

Why:  
Improve readability and reduce temporary variables.

Example:

```kotlin
val user = User(1, "Alice").apply {
    println("Created user $name")
}

val emailLength = user.email?.let { it.length } ?: 0
```

In Spring, scope functions help with configuring beans, chaining calls, or null-safe operations.

## 11. Error Handling

Traditional vs Functional
* Kotlin supports traditional try-catch exception handling.
* Additionally, Kotlin encourages functional error handling using the built-in Result type or libraries like Arrow.

Real-World Example: Using Result for Functional Error Handling

```kotlin
fun parseIntResult(str: String): Result<Int> =
    runCatching { str.toInt() }

val result = parseIntResult("123")

result
    .onSuccess { println("Parsed number: $it") }
    .onFailure { println("Failed to parse: ${it.message}") }
```

Why Use Functional Error Handling?

* Makes error cases explicit in function signatures.
* Avoids unchecked exceptions.
* Encourages composable and clean error handling.

Spring Boot Example

```kotlin
fun findUserAge(userId: String): Result<Int> {
    val user = userRepository.findById(userId)
        ?: return Result.failure(NoSuchElementException("User not found"))
    return runCatching { user.ageString.toInt() }
}
```

## 12. Coroutines

What Are Coroutines?
* Lightweight threads for asynchronous, non-blocking programming.
* Built into Kotlin with first-class support.
* Great for I/O-bound operations like database calls, HTTP requests.

Real-World Example: Asynchronous Service Call

```kotlin
suspend fun fetchUserData(userId: String): User {
    return userApi.getUserAsync(userId) // suspending function
}

suspend fun processUser(userId: String) {
    val user = fetchUserData(userId)
    println("User name: ${user.name}")
}
```

Integration with Spring Boot
* Use @Async or Spring WebFlux with Kotlin coroutines.
* Example with Spring WebFlux controller:

```kotlin
@RestController
class UserController(val userService: UserService) {

    @GetMapping("/users/{id}")
    suspend fun getUser(@PathVariable id: String): User =
        userService.fetchUserData(id)
}
```

Benefits
* Simplifies asynchronous code without callbacks or reactive streams complexity.
* Improves scalability by freeing threads during I/O.

## 13. Null Safety

Kotlin’s Null Safety Features
* Nullable types (String?) vs non-nullable (String).
* Compiler enforces null checks at compile time.
* Safe calls (?.), Elvis operator (?:), and non-null assertions (!!).

Real-World Example: Handling Nullable Data

```kotlin
fun getUserEmail(user: User?): String {
    return user?.email ?: "no-email@example.com"
}
```
Spring Boot Example: Handling Optional Request Parameters

```kotlin
@GetMapping("/search")
fun search(@RequestParam query: String?, @RequestParam page: Int? = 1): List<Result> {
    val safeQuery = query ?: ""
    val safePage = page ?: 1
    return searchService.search(safeQuery, safePage)
}
```

Benefits
* Eliminates many NullPointerExceptions.
* Makes nullability explicit in APIs.
* Encourages safer and more readable code.