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

Decision Heuristic (Senior-Level Rule)

Ask these three questions:

1. Do I need the object back?

YES → apply / also

2. Am I transforming it?

YES → let / run

3. Am I configuring it?

YES → apply

4. Am I logging/side-effecting?

YES → also

5. Do I just want grouping?

YES → with

| Use Case                        | Best Function |
| ------------------------------- | ------------- |
| Null-safe pipelines             | `let`         |
| Computation with object context | `run`         |
| Object construction/config      | `apply`       |
| Logging/side-effects            | `also`        |
| Grouping operations             | `with`        |

### Advanced let — Request → Domain → Persistence Pipelines

Example: REST Controller DTO → Entity Flow

Typical backend transformation pipeline:

```kotlin
fun createUser(request: CreateUserRequest): UserResponse {
    return request.email
        ?.let { emailValidator.validate(it) }
        ?.let { normalizedEmail -> request.toEntity(normalizedEmail) }
        ?.let { userRepository.save(it) }
        ?.let { UserResponse.from(it) }
        ?: throw IllegalArgumentException("Invalid email")
}
```

Why let here?

* This forms a null-safe transformation pipeline:
* Each stage transforms data
* If any stage returns null → chain stops

Eliminates nested if blocks

Equivalent Java code would be significantly more verbose.

Example: Conditional Repository Lookup

```kotlin
fun findActiveUser(id: Long?): User? {
    return id
        ?.let(userRepository::findById)
        ?.filter { it.isActive }
}
```
This pattern appears constantly in service layers.

### Advanced run — Business Logic Computation Blocks

run shines when you need object-centric computation.

Example: Transactional Business Logic

```kotlin
@Transactional
fun transferMoney(cmd: TransferCommand): TransferResult {
return accountRepository.find(cmd.fromAccountId).run {

        require(balance >= cmd.amount) { "Insufficient funds" }

        balance -= cmd.amount
        accountRepository.save(this)

        val target = accountRepository.find(cmd.toAccountId)
        target.balance += cmd.amount
        accountRepository.save(target)

        TransferResult.Success
    }
}
```

Why run?

* Keeps business logic inside object context
* Avoids repetitive variable references
* Improves readability of domain operations

Example: Lazy Fallback Fetch

Common cache pattern:

```kotlin
fun getUser(id: Long): User {
    return cache.get(id) ?: run {
        val dbUser = userRepository.find(id)
        cache.put(id, dbUser)
        dbUser
    }
}
```

This is extremely common in backend systems.

### Advanced apply — Entity Construction & Configuration

apply is the most common scope function in backend code.

Example: Building JPA Entities

```kotlin
fun CreateOrderRequest.toEntity(): Order {
    return Order().apply {
        customerId = this@toEntity.customerId
        status = OrderStatus.PENDING
        createdAt = Instant.now()
        totalAmount = calculateTotal()
    }
}
```

This replaces traditional builder patterns.

Example: HTTP Client Configuration

```kotlin
val client = HttpClient().apply {
connectTimeout = 3000
readTimeout = 5000
retryPolicy = RetryPolicy.exponential()
}
```

Example: Test Fixture Creation

Very common in backend testing:

```kotlin
val user = User().apply {
    id = 1
    email = "test@example.com"
    isActive = true
}
```

### Advanced also — Logging, Auditing, Metrics

also is the **best tool for side-effects** in production code.

Example: Logging After Persistence

```kotlin
fun saveUser(user: User): User {
    return userRepository.save(user)
        .also { logger.info("User saved: ${it.id}") }
}
```

Clean separation:

* Core logic remains unchanged
* Side effects are explicit

Example: Metrics Tracking

```kotlin
fun processPayment(payment: Payment): PaymentResult {
    return paymentGateway.process(payment)
        .also { metrics.increment("payments.processed") }
        .also { auditService.record(it) }
}
```

This pattern is extremely common in microservices.

Example: Validation Hooks

```kotlin
fun registerUser(user: User): User {
    return user.also {
        require(it.email.contains("@"))
        require(it.password.length > 8)
    }
}
```

### Advanced with — Bulk Domain Operations

Best when performing multiple operations on a known object.

Example: Aggregating Order Totals

```kotlin
fun calculateInvoice(order: Order): Invoice {
    return with(order) {
        Invoice(
        subtotal = items.sumOf { it.price },
        tax = calculateTax(),
        total = calculateTotal()
        )
    }
}
```

Example: Batch Updates

```kotlin
with(account) {
    lastLogin = Instant.now()
    loginCount++
    isActive = true
}
```

Cleaner than repeating `account.` everywhere.

### Real Microservice Flow Example (Combined Usage)

This is very close to production service code:

```kotlin
fun createOrder(request: CreateOrderRequest): OrderResponse {
    return request
        .let(orderValidator::validate)
        .let(orderMapper::toEntity)
        .apply { status = OrderStatus.CREATED }
        .also { orderRepository.save(it) }
        .also { eventPublisher.publish(OrderCreatedEvent(it.id)) }
        .let(orderMapper::toResponse)
}
```

This pipeline:

1. Validates input
2. Maps DTO → entity
3. Configures entity
4. Persists entity
5. Publishes domain event
6. Maps to response

This style is very idiomatic Kotlin backend code.

### Database Transaction Pattern

Another real-world pattern:

```kotlin
@Transactional
fun deactivateUser(id: Long): User {
    return userRepository.find(id)
        .apply { isActive = false }
        .also { userRepository.save(it) }
        .also { audit.logDeactivation(it.id) }
}
```

This is extremely typical in service layers.

### When NOT to Use Scope Functions in Backend

Important real-world rule:

Avoid scope functions when they:

* Hide important side-effects
* Reduce readability
* Obscure business logic flow

Example anti-pattern:

```kotlin
// Hard to read in real systems
request.let(::validate).let(::map).also(::save).also(::publish)
```

In business-critical code, **explicit variables may be better**.

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