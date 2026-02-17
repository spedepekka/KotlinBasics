package fi.kranu

/**
 * Examples of error handling in Kotlin.
 *
 * This file covers:
 * 1. Traditional try-catch (Imperative)
 * 2. Kotlin Result type (Standard Library)
 * 3. Functional error handling with Either (Custom implementation & Arrow concepts)
 * 4. Spring-style error handling (Patterns commonly used in Spring)
 */

fun main() {
    println("--- Traditional Try-Catch ---")
    traditionalTryCatch()

    println("\n--- Kotlin Result Type ---")
    kotlinResultExample()

    println("\n--- Functional Error Handling (Either) ---")
    functionalEitherExample()

    println("\n--- Spring-style Error Handling Patterns ---")
    springStyleExample()
}

// 1. Traditional Try-Catch
// Useful for unexpected errors or when interacting with Java libraries.
fun traditionalTryCatch() {
    try {
        val result = 10 / 0
        println("Result: $result")
    } catch (e: ArithmeticException) {
        println("Caught an error: ${e.message}")
    } finally {
        println("Cleanup or final steps here.")
    }

    // In Kotlin, try is an expression
    val number: Int? = try {
        "abc".toInt()
    } catch (e: NumberFormatException) {
        null
    }
    println("Parsed number: $number")
}

// 2. Kotlin Result Type (Standard Library)
// Introduced in Kotlin 1.3, it's a built-in way to represent success or failure.
fun kotlinResultExample() {
    fun divide(a: Int, b: Int): Result<Int> {
        return if (b == 0) {
            Result.failure(ArithmeticException("Division by zero"))
        } else {
            Result.success(a / b)
        }
    }

    val result = divide(10, 2)
    
    // Handling the result
    result.onSuccess { value ->
        println("Success: $value")
    }.onFailure { error ->
        println("Failed: ${error.message}")
    }

    // Functional mapping
    val doubled = result.map { it * 2 }.getOrElse { 0 }
    println("Doubled or default: $doubled")

    // Using runCatching for blocks that might throw
    val caught = runCatching {
        "not a number".toInt()
    }
    println("Was successful? ${caught.isSuccess}")
}

// 3. Functional Error Handling (Either)
// This is the common pattern in functional programming (Arrow library).
// Either<L, R> represents a value of one of two possible types (Left or Right).
// By convention, Left is used for Failure and Right for Success ("Right is Right").

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }
}

// Custom error types
sealed class DomainError {
    object NotFound : DomainError()
    data class ValidationError(val message: String) : DomainError()
    data class DatabaseError(val cause: Throwable) : DomainError()
}

fun functionalEitherExample() {
    fun findUser(id: Int): Either<DomainError, String> {
        return if (id == 1) {
            Either.Right("Alice")
        } else {
            Either.Left(DomainError.NotFound)
        }
    }

    val userResult = findUser(2)

    val message = userResult.fold(
        onLeft = { error ->
            when (error) {
                is DomainError.NotFound -> "User not found"
                is DomainError.ValidationError -> "Validation failed: ${error.message}"
                is DomainError.DatabaseError -> "DB error: ${error.cause.message}"
            }
        },
        onRight = { name -> "Hello, $name!" }
    )
    println("Either result: $message")
}

/**
 * 4. Spring-style Error Handling (without adding the library)
 * 
 * In Spring, we usually move away from manual try-catch in controllers 
 * and use centralized exception handling.
 */

// Custom business exceptions
class UserNotFoundException(message: String) : RuntimeException(message)

// Example of how a Spring Service might look
class UserService {
    fun getUser(id: Int): String {
        // Instead of returning null or Either, we throw a specific exception
        // that will be caught by a global handler.
        if (id != 1) throw UserNotFoundException("User $id not found")
        return "User1"
    }
}

// Example of @ControllerAdvice (Spring Pattern)
/*
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleUserNotFound(ex: UserNotFoundException): ErrorResponse {
        return ErrorResponse(status = 404, message = ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(500, "Internal Server Error"))
    }
}
*/

// Another Spring approach: ResponseStatusException
/*
fun getProduct(id: Int) {
    if (id < 0) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID")
    }
}
*/

fun springStyleExample() {
    val service = UserService()
    try {
        service.getUser(2)
    } catch (e: UserNotFoundException) {
        println("Spring-style service threw: ${e.message}")
        println("(In a real Spring app, @ControllerAdvice would catch this automatically)")
    }
}

/**
 * Arrow Library Note:
 * Arrow (https://arrow-kt.io/) provides a very powerful `Either` type out of the box,
 * along with `Typed Errors` (Raise DSL) which is the modern way to handle errors in Kotlin:
 * 
 * example with Arrow:
 * 
 * fun findUser(id: Int): Either<UserError, User> = either {
 *   ensure(id > 0) { InvalidId }
 *   repository.findById(id).bind()
 * }
 */
