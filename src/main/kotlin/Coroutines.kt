package fi.kranu

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 1. Suspend Functions:
 * Functions marked with 'suspend' can be paused and resumed without blocking the thread.
 * They can only be called from other suspend functions or a coroutine.
 */
suspend fun fetchData(): String {
    delay(1000) // Non-blocking delay
    return "Data from network"
}

/**
 * 2. Flow:
 * Asynchronous stream of values. Similar to Reactive Streams but simpler and
 * built on top of coroutines.
 */
fun getNumbersFlow(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(500)
        emit(i) // Send value to collector
    }
}

fun main() = runBlocking {
    /**
     * 3. Coroutine Scope & Structured Concurrency:
     * 'runBlocking' creates a scope that waits for all its children to finish.
     * This ensures that no coroutines are leaked (Structured Concurrency).
     */
    println("Main started on ${Thread.currentThread().name}")

    // Launching a child coroutine
    launch {
        val result = fetchData()
        println("Result: $result (on ${Thread.currentThread().name})")
    }

    println("Fetching numbers via Flow...")

    // 4. Flow vs Reactive Streams:
    // Flows are cold (start when collected) and sequential by default.
    getNumbersFlow()
        .map { it * 2 }
        .collect { value ->
            println("Flow received: $value")
        }

    println("Main finished")
}
