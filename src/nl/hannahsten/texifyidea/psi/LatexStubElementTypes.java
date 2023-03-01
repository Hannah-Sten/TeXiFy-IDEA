package nl.hannahsten.texifyidea.psi;

import nl.hannahsten.texifyidea.index.stub.LatexCommandsStubElementType;
import nl.hannahsten.texifyidea.index.stub.LatexEnvironmentStubElementType;
import nl.hannahsten.texifyidea.index.stub.LatexMagicCommentStubElementType;

/**
 * Grammar-Kit cannot generate this file containing only stub element types, so we have to maintain it manually.
 * This file has to be in Java, otherwise it will not be recognised as lazily loadable by IStubElementType#checkNotInstantiatedTooLateWithId
 */
public interface LatexStubElementTypes {

    LatexCommandsStubElementType COMMANDS = (LatexCommandsStubElementType) LatexTypes.COMMANDS;
    LatexEnvironmentStubElementType ENVIRONMENT = (LatexEnvironmentStubElementType) LatexTypes.ENVIRONMENT;
    LatexMagicCommentStubElementType MAGIC_COMMENT = (LatexMagicCommentStubElementType) LatexTypes.MAGIC_COMMENT;
}