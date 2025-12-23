/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.hannahsten.texifyidea.inspections

import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.safeDelete.SafeDeleteHandler

/**
 * Source: com.intellij.codeInsight.daemon.impl.quickfix.SafeDeleteFix
 */
open class SafeDeleteFix(element: PsiElement) : LocalQuickFixAndIntentionActionOnPsiElement(element) {

    override fun getText(): String {
        val startElement = startElement
        return "Safe delete " + startElement.text
    }

    override fun getFamilyName(): String = "Safe delete"

    override fun invoke(
        project: Project,
        file: PsiFile,
        editor: Editor?,
        startElement: PsiElement,
        endElement: PsiElement
    ) {
        if (!FileModificationService.getInstance().prepareFileForWrite(file)) return
        val elements = arrayOf(startElement)
        SafeDeleteHandler.invoke(project, elements, true)
    }

    override fun startInWriteAction(): Boolean = false
}