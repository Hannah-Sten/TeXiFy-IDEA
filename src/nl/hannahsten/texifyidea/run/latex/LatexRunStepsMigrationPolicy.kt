package nl.hannahsten.texifyidea.run.latex

internal enum class StepSchemaReadStatus {
    MISSING,
    PARSED,
    INVALID,
}

internal enum class ExecutionPipelineMode {
    LEGACY,
    STEPS,
}

/**
 * Migration policy for switching from legacy run chain to step-based execution.
 *
 * No feature-flag buffer is used: once a valid step schema is present, it is preferred.
 */
internal object LatexRunStepsMigrationPolicy {

    fun chooseExecutionPipeline(stepSchemaStatus: StepSchemaReadStatus): ExecutionPipelineMode = when (stepSchemaStatus) {
        StepSchemaReadStatus.PARSED -> ExecutionPipelineMode.STEPS
        StepSchemaReadStatus.MISSING,
        StepSchemaReadStatus.INVALID,
        -> ExecutionPipelineMode.LEGACY
    }
}
