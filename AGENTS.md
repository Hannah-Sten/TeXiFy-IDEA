# Guidelines for AI Agents working on TeXiFy-IDEA

This document provides specialized guidance for AI agents contributing to this project.

## Key Technical Context

### Plugin Dependencies and Loading
The project uses the `org.jetbrains.intellij.platform` Gradle plugin.
- Plugin dependencies are managed in `build.gradle.kts` within the `intellijPlatform` block.
- If certain platform features (like `com.intellij.java`) fail to load in tests, ensure all required platform modules are included using `bundledModule()` or `bundledPlugin()`.

### Project Structure
- `src/`: Main source code (Kotlin).
- `test/`: Unit and integration tests.
- `resources/META-INF/`: Plugin configuration files.
- `Writerside/`: Project documentation (viewable as a website).
- `gen/`: Generated code from Grammar-Kit and JFlex (do not edit directly).

## Common Tasks

### Adding metadata for LaTeX commands/environments
Metadata for autocompletion, inspections, etc., is defined in `src/nl/hannahsten/texifyidea/lang/predefined/`.
Refer to `Contributing-to-the-source-code.md` in the Writerside documentation for examples of how to use the DSL to add new definitions.

### Working with Lexers and Parsers
- Grammar files (`.bnf`) and Lexer files (`.flex`) are located in `src/nl/hannahsten/texifyidea/grammar/`.
- After modifying these files, run the corresponding Gradle tasks (e.g., `generateLatexParser`, `generateLatexLexer`) to update the generated code in `gen/`.

## Testing
- Run specific tests using `./gradlew test --tests "full.package.path.TestClassName.testMethodName"`.
- Use `BasePlatformTestCase` or its subclasses for IntelliJ Platform tests.
- If you see `java.lang.UnsupportedOperationException: class redefinition failed` when using Mockk, consider using a manual mock or recording object, as some platform test instrumentation interferes with Mockk.

## Documentation
- The user-facing documentation is located in `Writerside/topics/`.
- When adding new features, consider updating the relevant `.md` file in that directory.
- `Contributing-to-the-source-code.md` is the primary resource for developers.
