package nl.rubensten.texifyidea.util

import nl.rubensten.texifyidea.psi.*

/**
 * Get the token type (including `@`) of the BibTeX entry (e.g. `@article`).
 */
fun BibtexEntry.tokenType(): String? = type.text.toLowerCase()

/**
 * Get the token type (excluding `@`) of the BibTeX entry (e.g. `article`).
 */
fun BibtexEntry.tokenName(): String? = tokenType()?.substring(1)

/**
 * Get the identifier/label of the BibTeX entry (e.g. `someAuthor:23b`).
 */
fun BibtexEntry.identifier(): String? = firstChildOfType(BibtexId::class)?.text?.substringEnd(1)

/**
 * Get all the tags in the entry.
 */
fun BibtexEntry.tags(): Collection<BibtexTag> = childrenOfType(BibtexTag::class)

/**
 * Get all the key objects in the entry.
 */
fun BibtexEntry.keys(): Collection<BibtexKey> = childrenOfType(BibtexKey::class)

/**
 * Get all the names of all entry's keys.
 */
fun BibtexEntry.keyNames(): Collection<String> = keys().map { it.text }

/**
 * Get the key of the BibTeX tag.
 */
fun BibtexTag.key(): BibtexKey? = firstChildOfType(BibtexKey::class)

/**
 * Get the key of the BibTeX tag in string form.
 */
fun BibtexTag.keyName(): String? = key()?.text

/**
 * Get the content of the BibTeX tag.
 */
fun BibtexTag.content(): BibtexContent? = firstChildOfType(BibtexContent::class)