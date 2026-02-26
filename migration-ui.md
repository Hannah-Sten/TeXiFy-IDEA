# UI Migration Record (`run-config-ui` -> `new-ui`)

## Current Status

- Branch baseline: `new-ui`
- Migration mode: direct migration (no legacy compatibility guarantee)
- Core architecture: `common + steps` (options-only)
- Last updated: 2026-02-26

## Target Principles (from `run-config-ui`)

1. Single run configuration with explicit, reorderable compile steps.
2. Fragment-based UI with minimal defaults and `Modify options` for advanced fields.

## Current Architecture Snapshot

### 1. Data Model (`RunConfigurationOptions` only)

- Main options live in:
  - `src/nl/hannahsten/texifyidea/run/latex/LatexRunConfigurationOptions.kt`
- `common` settings are flattened on top-level options:
  - `mainFilePath`, `workingDirectoryPath`, `outputPath`, `auxilPath`, `latexDistribution`, env vars.
- Step list is polymorphic and instance-level:
  - `MutableList<LatexStepRunConfigurationOptions>`.
- Step types:
  - `latex-compile`, `latexmk-compile`, `pdf-viewer`, `bibtex`, `makeindex`, `external-tool`, `pythontex`, `makeglossaries`, `xindy`, `file-cleanup`.
- Per-step UI option visibility is stored in each step instance via `selectedOptions` (no global map).

### 2. Execution Pipeline

- Runtime entry:
  - `src/nl/hannahsten/texifyidea/run/latex/LatexRunConfiguration.kt`
- Step state:
  - `src/nl/hannahsten/texifyidea/run/latex/flow/LatexStepRunState.kt`
- Plan builder/providers:
  - `src/nl/hannahsten/texifyidea/run/latex/step/*`
- Sequential executor:
  - `src/nl/hannahsten/texifyidea/run/latex/flow/StepAwareSequentialProcessHandler.kt`

### 3. Step Contract (Phase 11.5)

- `LatexStepExecution` has been removed.
- Step lifecycle is owned by step implementations:
  - `beforeStart(context)`
  - `afterFinish(context, exitCode)`
- Two execution subinterfaces:
  - `ProcessLatexRunStep.createProcess(context)`
  - `InlineLatexRunStep.runInline(context)`
- `StepAwareSequentialProcessHandler` directly consumes `List<LatexRunStep>` and lazily executes step-by-step.

### 4. Logging / Console

- Step Log is the primary run output UI for step pipeline.
- Left side: JetBrains-style tree (step statuses + parsed messages).
- Right side: JetBrains `ConsoleView` output.
- Root-node selection shows aggregated output across steps.
- Event model uses `StepLogEvent(index, step, ...)` (not execution wrapper).

### 5. Settings UI Layout

- Editor is three sections:
  1. `Common settings`
  2. `Compile sequence`
  3. `Step settings`
- Step settings are nested fragmented editors by step type.
- `Modify options` is supported in common and step editors.

## Phase Tracker

- [x] Phase 0: scope freeze and migration strategy
- [x] Phase 1: initial step domain + sequential execution skeleton
- [x] Phase 2: step schema persistence path
- [x] Phase 3: bridge old capabilities into explicit steps
- [x] Phase 4: fragmented settings editor shell
- [x] Phase 5: compile sequence pills + add/remove/reorder
- [x] Phase 6: advanced steps + auto inference
- [x] Phase 7: aux/out/common settings consolidation
- [x] Phase 8: 3-pane editor + step settings panel
- [x] Phase 9: step-based tree log tab
- [x] Phase 9.1: JetBrains-style splitter/console alignment and output consolidation behavior
- [x] Phase 10: `common + steps` structural migration
- [x] Phase 10.1: options-only model (remove `model/*Config`, remove legacy compatibility layer)
- [x] Phase 11: execution/UI/settings closure for full step coverage
- [x] Phase 11.1: pre/post actions moved into step-level creation path
- [x] Phase 11.2: remove `StepFragmentedState`, use step options directly in fragmented editors
- [x] Phase 11.3: unify `deepCopy` with `BaseState.copyFrom` template pattern
- [x] Phase 11.4: file cleanup migrated to explicit step model
- [x] Phase 11.5: remove `LatexStepExecution`, move lifecycle to `LatexRunStep` Process/Inline interfaces
- [x] Phase 12: `LatexRunExecutionState` removed, full run-session state propagation

## Removed / Deprecated Paths (Completed)

- Removed model-layer abstractions:
  - `LatexRunConfigModel`, `LatexCommonSettings`, `LatexStepConfig`, `LatexUiState`.
- Removed old serializer/persistence bridge files for legacy schema migration.
- Removed legacy advanced editor entry from active main flow.
- Removed `LatexStepExecution` hierarchy and `createStepExecution(...)` contract.

## Remaining Work (Open)

1. Continue visual polish and UX parity checks in compile-sequence pill selection rendering.
2. Expand parser coverage for non-LaTeX/BibTeX step logs (currently raw log fallback for many steps).
3. Keep regression tests growing around mixed step pipelines and cancellation/short-circuit behavior.

## Validation Baseline

- Build:
  - `./gradlew :compileKotlin :compileTestKotlin`
- Core tests:
  - `nl.hannahsten.texifyidea.run.latex.flow.StepAwareSequentialProcessHandlerTest`
  - `nl.hannahsten.texifyidea.run.latex.steplog.LatexStepLogTabComponentTest`
  - `nl.hannahsten.texifyidea.run.latex.step.MakeindexRunStepTest`
  - `nl.hannahsten.texifyidea.run.latex.step.FileCleanupRunStepTest`
  - `nl.hannahsten.texifyidea.run.latex.LatexRunStepStateSelectionTest`
  - `nl.hannahsten.texifyidea.run.latex.ui.fragments.LatexStepSettingsComponentTest`

## Change Log (Recent)

### 2026-02-25

- Phase 11.5 completed:
  - Removed `LatexStepExecution` / `ProcessLatexStepExecution` / `InlineLatexStepExecution`.
  - `LatexRunStep` now owns lifecycle + execution contracts via Process/Inline subinterfaces.
  - `StepAwareSequentialProcessHandler` now consumes `List<LatexRunStep>` and runs steps lazily.
  - `StepLogEvent` payload switched to `index + step`.
  - Step Log component migrated from execution metadata to step metadata.
  - Related flow/step/steplog tests were updated and passing.

### 2026-02-24

- Phase 11.4 completed:
  - File cleanup migrated into explicit `file-cleanup` step.
  - Cleanup no longer relies on legacy listener chain.
- Phase 11.3 completed:
  - Step options deep-copy logic consolidated with template-based implementation.
- Phase 11.2 completed:
  - Removed `StepFragmentedState`; step editors bind directly to `LatexStepRunConfigurationOptions`.
- Phase 11.1 completed:
  - Step pre/post behaviors were moved into step-level execution composition.
- Phase 11 / 10.1 completed:
  - Options-only `common + steps` model became the single active configuration model.

### 2026-02-26

- Phase 12 completed:
  - Replaced shared `LatexRunExecutionState` with per-run `LatexRunSessionState`.
  - `LatexRunStepContext` now carries `session`; step chain no longer reads mutable run-config runtime state.
  - `LatexExecutionStateInitializer` renamed to `LatexSessionInitializer`.
  - `LatexCompiler.getCommand(...)` and `LatexmkModeService` switched to explicit `session` input.
  - Viewer forward-search API now accepts optional `session`; Zathura prefers session-resolved output path, then falls back to config-based guessing.
  - Removed legacy aux listener path files:
    - `src/nl/hannahsten/texifyidea/run/latex/RunLatexListener.kt`
    - `src/nl/hannahsten/texifyidea/run/bibtex/RunBibtexListener.kt`
    - `src/nl/hannahsten/texifyidea/run/latex/LatexRerunScheduler.kt`
