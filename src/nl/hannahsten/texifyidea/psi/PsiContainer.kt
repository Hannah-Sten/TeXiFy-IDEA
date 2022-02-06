package nl.hannahsten.texifyidea.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.scope.PsiScopeProcessor
import nl.hannahsten.texifyidea.editor.folding.LatexImportFoldingBuilder
import nl.hannahsten.texifyidea.util.endOffset
import javax.swing.Icon

/**
 * PsiElement that contains all elements from a given start element to an end element.
 *
 * Most methods only apply to the start node.
 *
 * ***Warning:*** This is a hacky solution. In other words, not all functionality is guaranteed to work like
 * you would expect. I made this to be used in [LatexImportFoldingBuilder] to fold multiple psi elements.
 * And frankly, for that purpose it works just fine.
 * If you want to use it for other purposes: test it thoroughly and add/modify the functionality you need.
 * Use at your own risk.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * @author Hannah Schellekens
 */
open class PsiContainer(val start: PsiElement, val end: PsiElement) : PsiElement {

    fun elements() = generateSequence(start) { it -> it.nextSibling?.takeIf { it != end } }

    /**
     * Only returns `true` when `another`
     */
    override fun isEquivalentTo(another: PsiElement?) = (start == end) && (another == start)

    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement? = start.addBefore(element, anchor)

    override fun copy() = PsiContainer(start, end)

    override fun getText() = elements().joinToString(separator = "") { it.text }

    override fun getStartOffsetInParent() = start.startOffsetInParent

    override fun getPrevSibling(): PsiElement? = start.prevSibling

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        elements().forEach { it.putUserData(key, value) }
    }

    /**
     * Only replaces start element.
     */
    override fun replace(newElement: PsiElement): PsiElement = start.replace(newElement)

    override fun getContainingFile(): PsiFile? = start.containingFile

    override fun getReferences(): Array<PsiReference> = elements().flatMap { it.references.asSequence() }.toList().toTypedArray()

    @Deprecated("Overrides deprecated member.")
    override fun checkAdd(element: PsiElement) = elements().forEach {
        @Suppress("DEPRECATION") // Has to be overridden
        it.checkAdd(element)
    }

    override fun getLanguage() = start.language

    override fun addRangeAfter(first: PsiElement?, last: PsiElement?, anchor: PsiElement?) = null

    override fun getUseScope() = start.useScope

    override fun getResolveScope() = start.resolveScope

    override fun getProject() = start.project

    override fun addRange(first: PsiElement?, last: PsiElement?) = start

    override fun getContext() = start.context

    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement? = end.addAfter(element, anchor)

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        return elements().all { it.processDeclarations(processor, state, lastParent, place) }
    }

    override fun accept(visitor: PsiElementVisitor) = elements().forEach { it.accept(visitor) }

    override fun getNextSibling(): PsiElement? = end.nextSibling

    override fun getFirstChild(): PsiElement = start.firstChild

    override fun getTextRange() = TextRange(start.textOffset, end.endOffset())

    override fun <T : Any> putCopyableUserData(key: Key<T>, value: T?) = elements().forEach {
        it.putCopyableUserData(key, value)
    }

    override fun getOriginalElement(): PsiElement? = start.originalElement

    @Deprecated("Overrides deprecated member.")
    override fun checkDelete() = elements().forEach {
        @Suppress("DEPRECATION") // Has to be overridden
        it.checkDelete()
    }

    override fun getNavigationElement(): PsiElement? = start.navigationElement

    override fun findElementAt(offset: Int) = containingFile?.findElementAt(start.textOffset + offset)

    override fun getReference() = start.reference

    override fun getTextLength() = end.endOffset() - start.textOffset

    override fun textMatches(text: CharSequence) = text == getText()

    override fun textMatches(element: PsiElement) = textMatches(element.text)

    override fun getTextOffset() = start.textOffset

    override fun textToCharArray() = text.toCharArray()

    override fun add(element: PsiElement): PsiElement? = end.add(element)

    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement = start.addRangeBefore(first, last, anchor)

    override fun isPhysical() = start.isPhysical

    override fun findReferenceAt(offset: Int) = start.findReferenceAt(offset)

    override fun getNode(): ASTNode? = start.node

    override fun getManager(): PsiManager? = start.manager

    override fun isValid() = start.isValid

    override fun delete() = elements().forEach {
        it.delete()
    }

    override fun getIcon(flags: Int): Icon? = start.getIcon(flags)

    override fun deleteChildRange(first: PsiElement?, last: PsiElement?) = elements().forEach {
        it.deleteChildRange(first, last)
    }

    override fun getParent(): PsiElement? = start.parent

    override fun getChildren(): Array<PsiElement> = emptyArray()

    override fun acceptChildren(visitor: PsiElementVisitor) = elements().forEach {
        it.accept(visitor)
    }

    override fun isWritable() = elements().all { it.isWritable }

    override fun <T : Any?> getUserData(key: Key<T>): T? = start.getUserData(key)

    override fun getLastChild(): PsiElement? = end.lastChild

    override fun textContains(c: Char) = text.contains(c)

    override fun <T : Any> getCopyableUserData(key: Key<T>): T? = start.getCopyableUserData(key)
}