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
    var startTime = System.currentTimeMillis()

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
    
    var totalTime = System.currentTimeMillis() - startTime
    
    println("\nAll services finished!")
    results.forEach { println("Received: $it") }
    println("Total execution time: ${totalTime}ms (vs sequential ~${results.size * 1500}ms)")

    println("\n--- Flow Concurrency Examples ---")

    // 1. buffer(): Decouples emitter and collector
    // Emitter runs in a separate coroutine. It can keep producing items 
    // while the collector is busy processing.
    println("\n[buffer] Decoupling emitter and collector:")
    val bufferTime = System.currentTimeMillis()
    flow {
        repeat(100) {
            //delay(Random.nextLong(1, 1000)) // Simulate fast production
            println("Emitting $it")
            emit(it)
        }
    }
    .buffer() // Emitter works ahead!
    .collect {
        delay(10) // Simulate slow processing
        println("Collected $it")
    }

    // 2. flowOn(): Changes the context (thread) of the UPSTREAM flow
    println("\n[flowOn] Moving emission to a background thread:")
    flow {
        println("Emitter running on: ${Thread.currentThread().name}")
        emit("Data")
    }
    .flowOn(Dispatchers.Default) // Upstream runs on worker thread
    .collect {
        println("Collector running on: ${Thread.currentThread().name}")
    }

    // 3. flatMapMerge(): Parallel processing of items
    println("\n[flatMapMerge] Processing multiple items in parallel:")
    val mergeTime = System.currentTimeMillis()
    @OptIn(ExperimentalCoroutinesApi::class)
    flowOf("Task 1", "Task 2", "Task 3")
        .flatMapMerge { task ->
            flow {
                delay(500) // Each sub-flow takes time
                emit("Finished $task")
            }
        }
        .collect { println(it) }
    println("flatMapMerge time: ${System.currentTimeMillis() - mergeTime}ms (vs ~1500ms sequential)")

    println("\nMain finished")
}
