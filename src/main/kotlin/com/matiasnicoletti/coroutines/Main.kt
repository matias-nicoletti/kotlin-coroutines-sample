package com.matiasnicoletti.coroutines

import com.matiasnicoletti.coroutines.CoroutinesUtils.timedMockIOOperation
import com.matiasnicoletti.coroutines.CoroutinesUtils.timedRunWithCustomDispatcher
import com.matiasnicoletti.coroutines.CoroutinesUtils.timedRunWithDispatcherIO
import com.matiasnicoletti.coroutines.CoroutinesUtils.timedRunWithGlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime

const val testUrl = "http://www.google.com"
const val mockedIOOperationDelay = 100L
const val noOfIOOperations = 200
const val customThreadPoolSize = 128

@ExperimentalTime
fun main() {
    println("Stats for CustomDispatcher - mocked IO: "
        + timedRunWithCustomDispatcher { timedMockIOOperation(mockedIOOperationDelay) })
    println("Stats for DispatcherIO - mocked IO: "
        + timedRunWithDispatcherIO { timedMockIOOperation(mockedIOOperationDelay) })
    println("Stats for GlobalScope - mocked IO: "
        + timedRunWithGlobalScope { timedMockIOOperation(mockedIOOperationDelay) })

    //    println("Stats for DispatcherIO - URL ping: " + timedRunWithDispatcherIO { timedPingUrl(`kotlin-coroutines-sample`.testUrl) })
    //    println("Stats for GlobalScope - URL ping: " + timedRunWithGlobalScope { timedPingUrl(`kotlin-coroutines-sample`.testUrl) })
}

object CoroutinesUtils {
    val customCoroutineDispatcher = newFixedThreadPoolContext(customThreadPoolSize, "customCoroutineDispatcher")
        .asExecutor()
        .asCoroutineDispatcher()

    fun timedRunWithDispatcherIO(ioOperation: () -> Long): ExecutionStats {
        var individualTimes: List<Long>
        val totalElapsedTime = measureTimeMillis {
            individualTimes = runBlocking {
                (1..noOfIOOperations).map { async(Dispatchers.IO) { ioOperation() } }.awaitAll()
            }
        }
        return ExecutionStats(totalElapsedTime, individualTimes)
    }

    fun timedRunWithGlobalScope(ioOperation: () -> Long): ExecutionStats {
        var individualTimes: List<Long>
        val totalElapsedTime = measureTimeMillis {
            individualTimes = runBlocking {
                (1..noOfIOOperations).map { GlobalScope.async { ioOperation() } }.awaitAll()
            }
        }
        return ExecutionStats(totalElapsedTime, individualTimes)
    }

    fun timedRunWithCustomDispatcher(ioOperation: () -> Long): ExecutionStats {
        var individualTimes: List<Long>
        val totalElapsedTime = measureTimeMillis {
            individualTimes = runBlocking {
                (1..noOfIOOperations).map { async(customCoroutineDispatcher) { ioOperation() } }.awaitAll()
            }
        }
        return ExecutionStats(totalElapsedTime, individualTimes)
    }

    fun timedPingUrl(url: String): Long =
        measureTimeMillis {
            HttpURLConnection.setFollowRedirects(false)
            val httpUrlConnection = (URL(url).openConnection() as HttpURLConnection).also {
                it.requestMethod = "HEAD"
            }
            check(httpUrlConnection.responseCode == HttpURLConnection.HTTP_OK)
        }

    fun timedMockIOOperation(delay: Long) =
        measureTimeMillis {
            runBlocking { delay(delay) }
        }
}

data class ExecutionStats(
    val totalElapsedTime: Long,
    val individualTimes: List<Long>,
    val individualTimesSum: Long = individualTimes.sum(),
)

