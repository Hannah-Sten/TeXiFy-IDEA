package nl.hannahsten.texifyidea.util

import com.jetbrains.rd.util.concurrentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class CacheValueTimed<T>(
    val value: T,
    val timestamp: Long = System.currentTimeMillis(),
) {
    fun isExpired(expirationInMs: Long): Boolean {
        return System.currentTimeMillis() - timestamp >= expirationInMs
    }

    fun isNotExpired(expirationInMs: Long): Boolean {
        return !isExpired(expirationInMs)
    }
}

/**
 * Tries to lock the mutex and runs the action if successful.
 * If the mutex is already locked, it skips the action.
 */
suspend inline fun Mutex.tryLockOrSkip(action: suspend () -> Unit) {
    if (!tryLock()) return
    try {
        action()
    }
    finally {
        unlock()
    }
}

abstract class AbstractCacheServiceBase<K : Any, V> {
    protected val caches: MutableMap<K, CacheValueTimed<V>> = concurrentMapOf()
    protected val computingState = ConcurrentHashMap<K, Mutex>()

    protected fun getComputingState(key: K): Mutex {
        return computingState.computeIfAbsent(key) { Mutex() }
    }

    protected fun putValue(key: K, value: V) {
        caches[key] = CacheValueTimed(value)
    }

    protected fun getTimedValue(key: K): CacheValueTimed<V>? {
        return caches[key]
    }

    protected fun getValueOrNull(key: K): V? {
        return caches[key]?.value
    }

    protected fun clearAllCache() {
        caches.clear()
    }

    protected fun clearOutdatedCache(expirationInMs: Long) {
        caches.entries.removeIf { it.value.isExpired(expirationInMs) }
    }
}

abstract class AbstractBackgroundCacheService<K : Any, V : Any>(private val coroutineScope: CoroutineScope) : AbstractCacheServiceBase<K, V>() {

    protected abstract suspend fun computeValueSuspend(key: K, oldValue: V?): V?

    /**
     * Calls to compute a value and updates the cache if the computation is successful.
     * If there is already a computation in progress for the same key, it will not wait for it to finish and will skip the computation.
     *
     */
    protected suspend fun computeOrSkip(key: K) {
        val computing = getComputingState(key)
        computing.tryLockOrSkip {
            val oldValue = getValueOrNull(key)
            computeValueSuspend(key, oldValue)?.also { putValue(key, it) }
        }
    }

    /**
     * Schedules a computation to be run later in a coroutine.
     * If the computation is already running, nothing happens.
     *
     */
    protected fun scheduleComputation(key: K) {
        if (getComputingState(key).isLocked) return // do not launch a new coroutine if there is already a computation in progress
        coroutineScope.launch {
            computeOrSkip(key)
        }
    }

    protected fun getAndComputeLater(key: K, expirationInMs: Long = 1000L): V? {
        val cachedValue = caches[key]
        if (cachedValue != null && cachedValue.isNotExpired(expirationInMs)) {
            return cachedValue.value
        }
        // If the value is not cached or expired, schedule the computation
        scheduleComputation(key)
        return cachedValue?.value
    }

    protected fun getAndComputeLater(key: K, expirationInMs: Long, defaultValue: V): V {
        return getAndComputeLater(key, expirationInMs) ?: defaultValue
    }

    protected suspend fun refreshAll(keys: Collection<K>) {
        for (key in keys) {
            val computing = getComputingState(key)
            computing.withLock {
                val oldValue = getValueOrNull(key)
                computeValueSuspend(key, oldValue)?.also { putValue(key, it) }
            }
        }
    }

    protected fun scheduleRefreshAll(keys: Collection<K> = caches.keys.toSet()) {
        coroutineScope.launch {
            refreshAll(keys)
        }
    }
}

abstract class AbstractBlockingCacheService<K : Any, V : Any>() : AbstractCacheServiceBase<K, V>() {

    protected abstract fun computeValue(key: K, oldValue: V?): V

    protected fun getOrComputeNow(key: K, expirationInMs: Long = 1000L): V {
        val valueTimed = getTimedValue(key)
        if (valueTimed != null && valueTimed.isNotExpired(expirationInMs)) {
            return valueTimed.value
        }
        return computeValue(key, valueTimed?.value).also {
            putValue(key, it)
        }
    }
}

/**
 * Provides a cache service for a project or an application that allows storing and retrieving values with expiration.
 */
abstract class GenericCacheService<P>(val param: P, private val coroutineScope: CoroutineScope) : AbstractCacheServiceBase<GenericCacheService.TypedKey<*>, Any?>() {

    interface TypedKey<T>

    private class PlainTypedKey<T> : TypedKey<T>
    private data class TypedKeyFromFunction<T>(val kClass: KClass<*>) : TypedKey<T>

    companion object {
        fun <T> createKey(): TypedKey<T> {
            return PlainTypedKey()
        }

        private fun <P, T> createKeyFromFunction(f: suspend (P) -> T?): TypedKey<T> {
            return TypedKeyFromFunction(f::class)
        }
    }

    /**
     * Puts a value in the cache with the given key instantly.
     */
    fun <T> put(key: TypedKey<T>, value: T) {
        caches[key] = CacheValueTimed(value)
    }

    /**
     * Gets a timed cached value by its key, or `null` if it does not exist.
     *
     * You can manually check if the value is expired by calling [CacheValueTimed.isExpired].
     */
    fun <T> getTimed(key: TypedKey<T>): CacheValueTimed<T>? {
        val cachedValue = caches[key] ?: return null
        @Suppress("UNCHECKED_CAST")
        return cachedValue as CacheValueTimed<T>
    }

    /**
     * Gets a cached value by its key, or `null` if it does not exist.
     *
     * Note that this method does not check for expiration.
     */
    fun <T : Any> getOrNull(key: TypedKey<T>): T? {
        return getTimed(key)?.value
    }

    private fun <T> getCachedValueOrNull(key: TypedKey<T>, expirationInMs: Long): CacheValueTimed<T>? {
        val cachedValue = getTimed(key) ?: return null
        if (cachedValue.isExpired(expirationInMs)) return null
        return cachedValue
    }

    /**
     * Gets a cached value by its key, or computes it if it does not exist or is expired.
     *
     * The computation is done immediately in the current thread.
     * If multiple threads call this method with the same key simultaneously, multiple computations may occur, so [f] must be thread-safe.
     */
    fun <T> getOrComputeNow(key: TypedKey<T>, expirationInMs: Long = 1000L, f: (P) -> T): T {
        val cachedValue = getCachedValueOrNull(key, expirationInMs)
        if (cachedValue != null) return cachedValue.value

        val result = f(param)
        put(key, result)
        return result
    }

    fun <T> getOrComputeNow(expirationInMs: Long = 1000L, f: (P) -> T): T {
        return getOrComputeNow(createKeyFromFunction(f), expirationInMs, f)
    }

    /**
     * Gets a cached value (possibly expired) by its key or the [instantResult] if no cache exists.
     * If the cache does not exist or is expired, it schedules the computation to be run later, not blocking the current thread.
     *
     *
     * It is guaranteed that [suspendComputation] will not run in parallel with itself for the same key.
     *
     * @param suspendComputation the computation to run if the cache is expired or does not exist. May return null to indicate that no value is available.
     */
    fun <S, T : S & Any> getAndComputeLater(
        key: TypedKey<T>,
        expirationInMs: Long = 1000L,
        instantResult: S, suspendComputation: suspend (P) -> T?
    ): S {
        val cachedValue = getTimed(key)
        if (cachedValue == null || cachedValue.isExpired(expirationInMs)) {
            // If the value is not cached or expired, schedule the computation
            scheduleComputation(key, suspendComputation)
        }
        return cachedValue?.value ?: instantResult // Return the instant result while computation is in progress
    }

    fun <T : Any> getAndComputeLater(expirationInMs: Long = 1000L, instantResult: T, f: suspend (P) -> T?): T {
        return getAndComputeLater(createKeyFromFunction(f), expirationInMs, instantResult, f)
    }

    fun <T : Any> getAndComputeLater(expirationInMs: Long = 1000L, f: suspend (P) -> T?): T? {
        return getAndComputeLater(createKeyFromFunction(f), expirationInMs, null, f)
    }

    fun <T : Any> getAndComputeLater(key: TypedKey<T>, expirationInMs: Long = 1000L, f: suspend (P) -> T?): T? {
        return getAndComputeLater(key, expirationInMs, null, f)
    }

    /**
     * Schedules a computation to be run later in a coroutine.
     * If the computation is already running, nothing happens.
     *
     * It is guaranteed that [suspendComputation] will not run in parallel with itself for the same key.
     *
     * @param suspendComputation the computation to run. It should return a value of type T or null if no value is available.
     */
    fun <T : Any> scheduleComputation(
        key: TypedKey<T>, suspendComputation: suspend (P) -> T?
    ) {
        if (getComputingState(key).isLocked) return // do not launch a new coroutine if there is already a computation in progress
        coroutineScope.launch {
            computeOrSkip(key, suspendComputation)
        }
    }

    /**
     * Calls to compute a value and updates the cache if the computation is successful.
     * If there is already a computation in progress for the same key, it will not wait for it to finish and will skip the computation.
     *

     *
     * It is guaranteed that [suspendComputation] will not run in parallel with itself for the same key.
     */
    suspend fun <T : Any> computeOrSkip(
        key: TypedKey<T>, suspendComputation: suspend (P) -> T?
    ) {
        val computing = getComputingState(key)
        computing.tryLockOrSkip {
            suspendComputation(param)?.also { put(key, it) }
        }
    }

    /**
     * Computes a value and updates the cache if the computation is successful.
     * If there is already a computation in progress for the same key, it will wait for it to finish and re-compute the value.
     *
     * It is guaranteed that [suspendComputation] will not run in parallel with itself for the same key.
     */
    suspend fun <T : Any> ensureRefresh(
        key: TypedKey<T>, suspendComputation: suspend (P) -> T?
    ): T? {
        val computing = getComputingState(key)
        computing.withLock {
            return suspendComputation(param)?.also { put(key, it) }
        }
    }
}
