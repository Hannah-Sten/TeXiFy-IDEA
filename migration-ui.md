# UI Migration Plan (`run-config-ui` -> `new-ui`)

## 当前状态

- 分支基线：`new-ui`
- 参考分支：`run-config-ui`
- 文档状态：初始化（第 1 版）
- 最后更新：2026-02-22

## 迁移目标

- 保留并落实 `run-config-ui` 的两个核心理念：
  - 单一运行配置：一个配置中可组合任意编译步骤，并可调整顺序。
  - 分片式 UI：默认界面精简，复杂选项通过 `Modify options` 按需启用。
- 不做整分支回滚式迁移，按能力逐步迁移到 `new-ui` 当前代码。
- 保持向后兼容，避免破坏现有 `LatexRunConfiguration` 的读写与执行流程。

## `run-config-ui` 可迁移能力摘要

### A. 执行编排（高优先级）

- 顺序执行器：`src/nl/hannahsten/texifyidea/run/SequentialProcessHandler.kt`
- 步骤模型：
  - `src/nl/hannahsten/texifyidea/run/step/Step.kt`
  - `src/nl/hannahsten/texifyidea/run/step/StepProvider.kt`
  - `src/nl/hannahsten/texifyidea/run/step/*Step.kt`
- 价值：把当前“监听器链+多 run config 辅助配置”的执行方式，转为“显式步骤序列”。

### B. 分片式编辑器（高优先级）

- 编辑器入口：`src/nl/hannahsten/texifyidea/run/ui/LatexSettingsEditor.kt`
- Fragment 工具：`src/nl/hannahsten/texifyidea/run/ui/CommonLatexFragments.kt`
- 序列组件：
  - `src/nl/hannahsten/texifyidea/run/ui/LatexCompileSequenceComponent.kt`
  - `src/nl/hannahsten/texifyidea/run/ui/LatexCompileSequenceFragment.kt`
- 价值：把当前长表单拆成可选碎片，降低默认复杂度。

### C. 步骤可扩展性（中优先级）

- provider 注册点：`src/nl/hannahsten/texifyidea/util/magic/CompilerMagic.kt`
- 可扩展步骤类型：
  - LaTeX、Bibliography、PDF Viewer、Command Line、Other Run Configuration
- 价值：可持续增加 pythontex / glossary / index / custom tool 这类中间步骤。

### D. 配置兼容层思路（中优先级）

- `run-config-ui` 把旧配置移到 `run/legacy/*`，新旧并存。
- 价值：提供“平滑迁移”的策略参考，但不建议原样复制目录重命名。

## 不建议直接迁移的内容

- `run/ui/console/*` 的自定义执行控制台（存在大量未完成 TODO）。
- 旧分支里大规模 package 重命名（`run/*` <-> `run/legacy/*`）本身。
- 一次性引入完整 options 重构（`LatexRunConfigurationOptions`）并替换 `new-ui` 现有持久化。

## 分阶段迁移路线（持续更新）

### Phase 0：冻结目标与兼容边界

- 状态：`DONE`
- 目标：确定新旧 XML 兼容策略与“无开关直接迁移”执行策略。

#### Phase 0 冻结决策

| 决策项 | 决策 | 说明 |
| --- | --- | --- |
| 向后兼容策略 | 双轨兼容 | 保持现有 `texify/*` 字段可读写；新增步骤字段并行存在。 |
| 旧配置升级方式 | 惰性升级 | 读取旧配置时不立刻重写结构；仅在用户修改并保存时输出新结构。 |
| 旧字段保留期限 | 至少两个小版本 | 避免回滚时丢配置。 |
| 默认执行引擎 | 新 schema 优先 | 有效步骤 schema 存在时优先走新流程；否则回退 legacy。 |
| 启用方式 | 无 feature flag | 不使用 registry 作为中间缓冲层。 |
| UI 切换方式 | 直接迁移 | 以 `new-ui` 为目标形态推进，不做双轨 UI 开关。 |

#### Phase 0 兼容契约（XML）

- 现有读写字段继续作为稳定契约（不可破坏）：
  - `compiler`, `compiler-path`, `pdf-viewer`, `viewer-command`
  - `compiler-arguments`, `main-file`, `working-directory`
  - `output-path`, `auxil-path`, `latex-distribution`
  - `bib-run-configs`, `makeindex-run-configs`, `external-tool-run-configs`
  - 兼容旧集合格式：`bib-run-config`, `makeindex-run-config`, `external-tool-run-config`
- 预留新增字段（步骤模型）：
  - 建议标签：`<compile-steps><compile-step type="...">...</compile-step></compile-steps>`
  - 注意：`run-config-ui` 使用 `<compile-step step-name="...">`，新实现保留同语义但建议在外层加容器，便于版本化。

#### Phase 0 冲突优先级（当旧字段与新步骤并存）

1. 若步骤容器存在且可解析，优先使用步骤序列执行。  
2. 若步骤容器存在但解析失败，记录 warning 并回退旧执行链。  
3. 若步骤容器缺失，使用旧字段和旧执行链。  
4. 保存时：
   - Phase 0 仍写旧字段（新字段写入在 Phase 2 开启），避免当前行为波动。

#### Phase 0 最小交付范围（MVP）

- 执行侧：仅要求支持 `LaTeX Compile` + `PDF Viewer` 两种步骤。
- UI 侧：可延后；Phase 0 不要求 fragment UI 上线。
- 兼容侧：必须保证旧用户配置行为不变化。

#### Phase 0 测试清单（冻结）

- 保底回归：
  - `test/nl/hannahsten/texifyidea/run/LatexRunConfigurationTest.kt`
  - `test/nl/hannahsten/texifyidea/run/latexmk/LatexmkRunConfigurationTest.kt`
- 新增测试（Phase 1/2 立即补）：
  - 读旧配置、写新结构、再读回（round-trip）
  - 新旧字段并存时的优先级选择
  - 新步骤解析失败时自动回退旧链路

#### Phase 0 PR 切分建议

- PR-0A：文档与兼容契约冻结（无 registry 方案）。
- PR-0B：执行入口接入 schema 探测与优先级策略（仍暂走旧链）。
- PR-0C：测试骨架（优先级与回退用例先加 pending/failing test）。

### Phase 1：在 `new-ui` 引入步骤域模型（无 UI 变更）

- 状态：`DONE`
- 目标：先落地执行数据模型，再接 UI。
- 代码落点（建议）：
  - `src/nl/hannahsten/texifyidea/run/latex/step/*`
  - `src/nl/hannahsten/texifyidea/run/latex/flow/*`
- 任务：
  - 引入 `Step` / `StepSpec` / `StepProvider` 抽象
  - 迁移 `SequentialProcessHandler` 思路到 `new-ui` 包结构
  - 先实现最小步骤：`LatexCompileStep` + `PdfViewerStep`
- 验收：
  - 在不改编辑器 UI 的前提下，可按“步骤序列”执行
  - 原 `latexmk` 路径行为不回退

### Phase 2：持久化与向后兼容

- 状态：`DONE`
- 目标：序列化步骤，并兼容旧字段。
- 代码落点：
  - `src/nl/hannahsten/texifyidea/run/latex/LatexRunConfigurationPersistence.kt`
  - `src/nl/hannahsten/texifyidea/run/latex/LatexRunConfigurationSerializer.kt`
- 任务：
  - 新增 `<compile-step ...>` 节点读写
  - 旧配置自动映射为默认步骤序列（例如 LaTeX -> PDF）
  - 保持 `bib/makeindex/external` 旧字段可读
- 验收：
  - 旧配置加载后行为一致
  - 新配置写回后可再次读取且不丢失步骤顺序

### Phase 3：步骤桥接（先桥接旧能力，再替换旧能力）

- 状态：`DONE`
- 目标：保留现有能力，避免一次性移除旧机制。
- 任务：
  - 用“桥接步骤”封装当前 `bibtex/makeindex/external tool` 运行链
  - 把 `RunBibtexListener` / `RunMakeindexListener` / `RunExternalToolListener` 的触发点从“隐式链”迁到“显式步骤”
- 验收：
  - biber/bibtex/makeindex/external tool 仍可执行
  - 失败短路行为与 legacy 一致（首个失败即终止）且可测试

### Phase 4：分片式 UI 外壳落地

- 状态：`DONE`
- 目标：把 `LatexSettingsEditor` 从长表单迁移到 fragment 架构。
- 代码落点：
  - `src/nl/hannahsten/texifyidea/run/latex/ui/LatexSettingsEditor.kt`
  - 新增 `src/nl/hannahsten/texifyidea/run/latex/ui/fragments/*`
- 任务：
  - 引入 `RunConfigurationFragmentedEditor`
  - 迁移基础碎片：主文件、编译器、参数、工作目录、环境变量
- 验收：
  - 默认 UI 更精简
  - `Modify options` 可启用高级项

### Phase 5：编译序列碎片与拖拽排序

- 状态：`DONE`
- 目标：支持可组合步骤与排序（核心体验）。
- 任务：
  - 引入 `CompileSequenceFragment` 与可视化 step pills
  - 支持新增/删除/双击配置/拖拽重排
- 验收：
  - 用户可按顺序组合 LaTeX/bib/index/pythontex(命令行)/viewer
  - 排序结果与执行顺序一致

### Phase 6：高级步骤与自动推断

- 状态：`DONE`
- 目标：恢复并升级旧分支中的自动化推断能力。
- 任务：
  - 迁移 bibliography step 的 `createIfRequired` 逻辑（含 biber/bibtex 推断）
  - 新增 `CommandLineStep` 的安全参数解析（不使用简单 `split(" ")`）
  - 为 pythontex/makeglossaries/xindy 提供内置模板步骤
- 验收：
  - 首次运行可提示或自动补齐必要中间步骤
  - 不引入误判导致的冗余步骤

### Phase 7：清理旧路径与收口

- 状态：`TODO`
- 目标：在新流程稳定后再清理旧实现。
- 任务：
  - 标记并逐步移除旧监听器链入口
  - 精简重复 UI 代码和重复运行配置类型
- 验收：
  - 测试覆盖迁移后主路径
  - 兼容迁移文档和 release note 就绪

## 风险清单

- XML 兼容风险：步骤序列与旧字段并存时可能出现重复执行。
- UI API 风险：JetBrains 平台版本变化导致 fragment API 行为差异。
- 性能风险：首次运行的自动检测（bibliography/index/pythontex）可能卡顿。
- 行为回归风险：`latexmk` 自动循环与显式步骤序列的冲突处理。

## 测试策略（每阶段最低要求）

- 单元测试：
  - 步骤序列读写 round-trip
  - 失败短路/继续策略
  - 默认步骤生成逻辑
- 集成测试：
  - 从旧配置升级到新配置
  - 典型流程：LaTeX -> Bib -> LaTeX -> Viewer
  - `latexmk` 与非 `latexmk` 两条路径
- UI 测试：
  - fragment 显隐
  - 步骤拖拽排序结果

## 执行看板

- [x] Phase 0: 兼容边界冻结
- [x] Phase 1: 步骤域模型
- [x] Phase 2: 序列化兼容
- [x] Phase 3: 旧能力桥接
- [x] Phase 4: Fragment UI 外壳
- [x] Phase 5: 序列碎片与拖拽
- [x] Phase 6: 自动推断与高级步骤
- [ ] Phase 7: 清理与收口

## 更新日志

- 2026-02-22
  - 初始化迁移文档。
  - 基于 `run-config-ui` 和 `new-ui` 的结构差异，确定“先模型、后 UI、最后清理”的迁移顺序。
  - 明确不直接迁移旧分支中未完成的自定义执行控制台实现。
- 2026-02-22（Phase 0 细化）
  - 冻结了 XML 兼容契约与冲突优先级。
  - 评估过 feature flag 方案，后续按“直接迁移”决策移除。
  - 增加 Phase 0 的 PR 拆分建议（0A/0B/0C）。
- 2026-02-22（Phase 0 落地：0A/0B/0C）
  - 移除了 registry 缓冲方案，改为无 feature flag 的直接迁移策略。
  - 运行配置入口已接入步骤 schema 探测与优先级判定：`PARSED` 走步骤引擎，`MISSING/INVALID` 走 legacy 执行链。
  - 新增步骤 schema 探测与策略骨架测试，覆盖“优先级/回退”规则。
- 2026-02-22（Phase 1 启动）
  - 新增 `run/latex/step` 与 `run/latex/flow` 目录：引入最小步骤模型（LaTeX Compile / PDF Viewer）与顺序执行器。
  - `LatexRunConfiguration.getState()` 已在检测到可支持步骤 schema 时切换为 `LatexStepRunState`。
  - 增加步骤类型解析与执行状态选择测试。
- 2026-02-22（Phase 1 完成）
  - 引入 `StepSpec`、`StepProvider` 与 Provider Registry，替换硬编码步骤映射。
  - `LatexStepRunState` 增加 `RegexpFilter`，保持与 legacy 路径一致的行号跳转能力。
  - 通过 `LatexRunStepsMigrationPolicyTest`、`LatexRunStepStateSelectionTest`、`LatexRunConfigurationTest` 回归验证。
- 2026-02-22（Phase 2 启动）
  - 新增 `compile-steps` 写入：在保留 legacy 字段的同时并行写入步骤 schema。
  - 对缺失步骤 schema 的旧配置增加默认步骤推断（`latex-compile` + `pdf-viewer`）。
  - 对非法步骤 schema 保留 `INVALID` 状态并回退到 legacy 推断步骤，避免中断升级。
- 2026-02-22（Phase 2 完成）
  - `LatexRunConfigurationSerializer` 增加 `compile-steps` 写入能力与类型提取。
  - `LatexRunConfigurationPersistence` 完成新旧 schema 双向兼容：优先读新 schema，缺失/异常时回退 legacy 推断。
  - 新增 `LatexRunStepSchemaPersistenceTest`，验证写入、旧配置推断、异常 schema 回退与显式 schema 优先。
- 2026-02-22（Phase 3 启动）
  - 为 `legacy-bibtex`、`legacy-makeindex`、`legacy-external-tool` 引入 bridge step/provider。
  - 旧配置推断步骤序列时，补充 aux step 与 `compileTwice` 的显式表示。
- 2026-02-23（Phase 3 第二段收口）
  - bridge steps 改为在步骤引擎内直接执行：`Legacy*RunStep` 通过 `RunConfigurationBeforeRunProvider` 触发旧 run configurations。
  - 移除 bridge steps 的“强制回退 legacy pipeline”中间态，显式步骤路径与执行路径保持一致。
  - 对 `MISSING` 步骤 schema 保持状态不变，仅推断步骤类型用于后续写回，避免旧配置在首次读取后误切换到新执行器。
  - 增加/更新桥接执行与迁移策略测试，覆盖别名映射、执行器选择和步骤序列推断。
- 2026-02-23（Phase 4 完成）
  - `LatexSettingsEditor` 切换为 `RunConfigurationFragmentedEditor`，引入 fragment 化外壳。
  - 迁移基础碎片：compiler、main file、compiler arguments、working directory、environment variables。
  - 保留 `LegacyLatexSettingsEditor` 作为可选“Advanced options (legacy)”碎片，确保高级配置在过渡期可继续编辑。
  - 新增编辑器切换测试，原有表单行为测试改为验证 `LegacyLatexSettingsEditor`。
- 2026-02-23（Phase 5 完成）
  - 新增 `LatexCompileSequenceComponent` / `LatexCompileSequenceFragment`，支持步骤添加、删除、拖拽重排与双击改类型。
  - 编译序列碎片直接写回 `stepSchemaTypes`，并在应用配置时将 `stepSchemaStatus` 标记为 `PARSED`。
  - 对无显式 schema 的配置，序列碎片默认按 legacy 字段推断步骤顺序（含 aux 与 `compileTwice`）。
  - 新增 `LatexCompileSequenceComponentTest` 验证步骤 schema 写回行为。
- 2026-02-23（Phase 6 完成）
  - 新增 `LatexRunStepAutoInference`：在步骤执行前按文档内容自动补齐必要中间步骤（`legacy-bibtex` / `pythontex-command` / `makeglossaries-command` / `xindy-command`），并在需要时自动补一个后置 `latex-compile`。
  - `legacy-bibtex` 步骤执行时，若无现成 bib run config，会按旧逻辑自动生成后执行，等效迁移 `createIfRequired` 思路。
  - 新增 `CommandLineRunStep` 与 `CommandLineRunStepParser`，命令行参数解析改用 `ParametersListUtil.parse`，不再使用简单空格切分。
  - 引入 pythontex/makeglossaries/xindy 三个内置模板步骤 provider，并接入步骤 registry 与 UI step 描述。
  - 新增 `LatexRunStepAutoInferenceTest`、`CommandLineRunStepParserTest`，并扩展迁移策略测试覆盖新模板步骤别名映射。
