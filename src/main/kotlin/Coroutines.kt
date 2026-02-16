package fi.kranu

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

/**
 * Simulates an external service with random response time.
 */
suspend fun fetchFromService(serviceName: String): String {
    val delayTime = Random.nextLong(500, 3000)
    println("[$serviceName] starting (will take ${delayTime}ms)...")
    delay(delayTime)
    return "[$serviceName] Response"
}

fun main() = runBlocking {
    println("--- Complex Coroutine Example: Parallel Services ---")
    val startTime = System.currentTimeMillis()

    /**
     * Parallel Execution using 'async':
     * We launch multiple coroutines concurrently. 
     * Each 'async' returns a 'Deferred' object (a promise of a future value).
     */
    val service1 = async { fetchFromService("Auth-Service") }
    val service2 = async { fetchFromService("Payment-Gateway") }
    val service3 = async { fetchFromService("Inventory-Check") }

    println("All services triggered. Waiting for all to complete...")

    /**
     * Waiting for results:
     * 'awaitAll' suspends the current coroutine until all provided Deferreds are finished.
     * This is structured concurrency in action: we manage multiple tasks as a single unit.
     */
    val results = awaitAll(service1, service2, service3)
    
    val totalTime = System.currentTimeMillis() - startTime
    
    println("\nAll services finished!")
    results.forEach { println("Received: $it") }
    println("Total execution time: ${totalTime}ms (vs sequential ~${results.size * 1500}ms)")

    println("\n--- Flow Example ---")
    // Flows are cold and sequential by default.
    flowOf("Task A", "Task B", "Task C")
        .onEach { delay(Random.nextLong(100, 500)) }
        .collect { println("Flow processed: $it") }

    println("Main finished")
}
