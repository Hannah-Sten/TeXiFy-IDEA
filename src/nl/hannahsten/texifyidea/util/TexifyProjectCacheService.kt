package nl.hannahsten.texifyidea.util

import com.intellij.openapi.components.Service
import com.intellij.openapi.observable.util.lockOrSkip
import com.intellij.openapi.project.Project
import com.jetbrains.rd.util.ConcurrentHashMap
import com.jetbrains.rd.util.concurrentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
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

abstract class ProjectCacheService(val project: Project, private val coroutineScope: CoroutineScope) {

    interface TypedKey<T>

    private class PlainTypedKey<T> : TypedKey<T>
    private data class TypedKeyFromFunction<T>(val kClass: KClass<*>) : TypedKey<T>

    companion object {
        fun <T> createKey(): TypedKey<T> {
            return PlainTypedKey()
        }

        private fun <T> createKeyFromFunction(f: suspend (Project) -> T?): TypedKey<T> {
            return TypedKeyFromFunction(f::class)
        }
    }

    protected val caches: MutableMap<TypedKey<*>, CacheValueTimed<*>> = concurrentMapOf()
    protected val computingState = ConcurrentHashMap<TypedKey<*>, AtomicBoolean>()

    /**
     * Puts a value in the cache with the given key instantly.
     */
    fun <T> put(key: TypedKey<T>, value: T) {
        caches[key] = CacheValueTimed(value)
    }

    /**
     * Gets a cached value by its key, or null if it does not exist.
     */
    fun <T> getTimed(key: TypedKey<T>): CacheValueTimed<T>? {
        val cachedValue = caches[key] ?: return null
        @Suppress("UNCHECKED_CAST")
        return cachedValue as CacheValueTimed<T>
    }

    /**
     * Gets a cached value by its key, or null if it does not exist or is expired.
     */
    fun <T : Any> getOrNull(key: TypedKey<T>, expirationInMs: Long = 1000L): T? {
        return getCachedValueOrNull(key, expirationInMs)?.value
    }

    private fun getComputingState(key: TypedKey<*>): AtomicBoolean {
        return computingState.computeIfAbsent(key) { AtomicBoolean(false) }
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
     * If multiple threads call this method with the same key simultaneously, multiple computations may occur.
     */
    fun <T> getOrComputeNow(key: TypedKey<T>, expirationInMs: Long = 1000L, f: (Project) -> T): T {
        val cachedValue = getCachedValueOrNull(key, expirationInMs)
        if (cachedValue != null) return cachedValue.value

        val result = f(project)
        put(key, result)
        return result
    }

    fun <T> getOrComputeNow(expirationInMs: Long = 1000L, f: (Project) -> T): T {
        return getOrComputeNow(createKeyFromFunction(f), expirationInMs, f)
    }

    /**
     * Gets a cached value (possibly expired) by its key or the instant result if no cache exists.
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
        instantResult: S, suspendComputation: suspend (Project) -> T?
    ): S {
        val cachedValue = getTimed(key)
        if(cachedValue == null || cachedValue.isExpired(expirationInMs)) {
            // If the value is not cached or expired, schedule the computation
            scheduleCompute(key, suspendComputation)
        }
        return cachedValue?.value ?: instantResult // Return the instant result while computation is in progress
    }

    fun <T : Any> getAndComputeLater(expirationInMs: Long = 1000L, instantResult: T, f: suspend (Project) -> T?): T {
        return getAndComputeLater(createKeyFromFunction(f), expirationInMs, instantResult, f)
    }

    fun <T : Any> getAndComputeLater(expirationInMs: Long = 1000L, f: suspend (Project) -> T?): T? {
        return getAndComputeLater(createKeyFromFunction(f), expirationInMs, null, f)
    }

    fun <T : Any> getAndComputeLater(key: TypedKey<T>, expirationInMs: Long = 1000L, f: suspend (Project) -> T?): T? {
        return getAndComputeLater(key, expirationInMs, null, f)
    }

    /**
     * Schedules a computation to be run later in a coroutine.
     * If the computation is already running, nothing happens.
     *
     * It is guaranteed that [f] will not run in parallel with itself for the same key.
     *
     * @param f the computation to run. It should return a value of type T or null if no value is available.
     */
    fun <T : Any> scheduleCompute(
        key: TypedKey<T>, f: suspend (Project) -> T?
    ) {
        coroutineScope.launch {
            computeAndUpdate(key, f)
        }
    }

    /**
     * Computes a value and updates the cache if the computation is successful.
     *
     * You may use this method to manually compute a value and update the cache in some background task,
     * such as [com.intellij.openapi.startup.ProjectActivity].
     *
     * It is guaranteed that [f] will not run in parallel with itself for the same key.
     */
    suspend fun <T : Any> computeAndUpdate(
        key: TypedKey<T>, f: suspend (Project) -> T?
    ) {
        val computing = getComputingState(key)
        computing.lockOrSkip {
            val result = f(project)
            if (result != null) put(key, result)
        }
    }
}

@Service(Service.Level.PROJECT)
class TexifyProjectCacheService(project: Project, coroutineScope: CoroutineScope) : ProjectCacheService(project, coroutineScope) {

    companion object {
        fun getInstance(project: Project): TexifyProjectCacheService {
            return project.getService(TexifyProjectCacheService::class.java)
        }

        fun <T> getOrCompute(
            project: Project, expirationInMs: Long = 1000L, f: (Project) -> T
        ): T {
            return getInstance(project).getOrComputeNow(expirationInMs, f)
        }

        fun <T> getOrCompute(
            project: Project, key: TypedKey<T>, expirationInMs: Long = 1000L, f: (Project) -> T
        ): T {
            return getInstance(project).getOrComputeNow(key, expirationInMs, f)
        }
    }
}
