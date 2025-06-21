package nl.hannahsten.texifyidea.index

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.util.concurrency.annotations.RequiresReadLock

/**
 * Provide utility functions for working with [StubIndex] in a more convenient and Kotlin-friendly way.
 */
object StubIndexKt {
    /**
     * Traverse all elements in the given [indexKey] for the specified [key] and apply the action to each element.
     */
    @RequiresReadLock
    inline fun <Key : Any, reified Psi : PsiElement> traverseElements(
        indexKey: StubIndexKey<Key, Psi>,
        key: Key,
        project: Project,
        scope: GlobalSearchScope,
        crossinline action: (Psi) -> Boolean
    ): Boolean {
        return traverseElements(indexKey, key, project, scope, Psi::class.java, action)
    }

    /**
     * Traverse all elements in the given [indexKey] for the specified [key] and apply the action to each element.
     */
    @RequiresReadLock
    inline fun <Key : Any, Psi : PsiElement> traverseElements(
        indexKey: StubIndexKey<Key, Psi>,
        key: Key,
        project: Project,
        scope: GlobalSearchScope,
        clazz: Class<Psi>,
        crossinline action: (Psi) -> Boolean
    ): Boolean {
        return StubIndex.getInstance().processElements(indexKey, key, project, LatexFileFilterScope(scope), clazz) { element ->
            action(element)
        }
    }

    /**
     * Traverse all keys in the given [indexKey] and apply the action to each key.
     */
    @RequiresReadLock
    inline fun <Key : Any, Psi : PsiElement> traverseKeys(
        indexKey: StubIndexKey<Key, Psi>,
        project: Project, scope: GlobalSearchScope, crossinline action: (Key) -> Boolean
    ): Boolean {
        return StubIndex.getInstance().processAllKeys(indexKey, { key ->
            action(key)
        }, scope)
    }

    inline fun <Key : Any, Psi : PsiElement> traverseKeys(
        indexKey: StubIndexKey<Key, Psi>,
        project: Project, scope: GlobalSearchScope,
        clazz: Class<Psi>,
        crossinline action: (Key) -> Boolean
    ): Boolean {
        return StubIndex.getInstance().processAllKeys(indexKey, { key ->
            action(key)
        }, scope)
    }

    @RequiresReadLock
    inline fun <Key : Any, reified Psi : PsiElement> traverseAllElements(
        indexKey: StubIndexKey<Key, Psi>,
        project: Project,
        scope: GlobalSearchScope,
        crossinline action: (Psi) -> Boolean
    ): Boolean {
        return traverseAllElements(indexKey, project, scope, Psi::class.java, action)
    }

    inline fun <Key : Any, Psi : PsiElement> traverseAllElements(
        indexKey: StubIndexKey<Key, Psi>,
        project: Project,
        scope: GlobalSearchScope,
        clazz: Class<Psi>,
        crossinline action: (Psi) -> Boolean
    ): Boolean {
        return traverseKeys(indexKey, project, scope) { key ->
            traverseElements(indexKey, key, project, scope, clazz, action)
        }
    }

}




