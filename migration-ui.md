# UI Migration Plan (`run-config-ui` -> `new-ui`)

## 当前状态

- 分支基线：`new-ui`
- 参考分支：`run-config-ui`
- 文档状态：初始化（第 1 版）
- 最后更新：2026-02-24

## 迁移目标

- 保留并落实 `run-config-ui` 的两个核心理念：
  - 单一运行配置：一个配置中可组合任意编译步骤，并可调整顺序。
  - 分片式 UI：默认界面精简，复杂选项通过 `Modify options` 按需启用。
- 不做整分支回滚式迁移，按能力逐步迁移到 `new-ui` 当前代码。
- 不再以历史配置兼容为约束，直接切换到 `common + steps` 新结构。

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

- 状态：`DONE`
- 目标：在新流程稳定后再清理旧实现。
- 任务：
  - 标记旧监听器链为 legacy fallback 路径（仅在 schema 缺失/非法时使用）
  - 将路径配置（aux/out）迁入主碎片区，移除 legacy UI 双入口
  - 收口 legacy advanced 描述与文案，明确其过渡角色
- 验收：
  - `Modify options` 可单独启用 `Output directory` / `Auxiliary directory`
  - legacy editor 不再写入 aux/out，避免与新碎片重复入口冲突
  - 回归测试覆盖新碎片挂载与 legacy 收口行为

### Phase 8：三段式编辑器与步骤设置面板

- 状态：`DONE`
- 目标：重排编辑器为“共通设置 / 编译序列 / 步骤设置”三段式，并引入选中步骤联动设置。
- 任务：
  - 顶部改为 `Common settings`：默认只显示 main file，其它共通项通过 `Modify options` 按需启用
  - 保留中段 `Compile sequence`，并新增步骤选中状态与高亮
  - 新增下段 `Step settings` 卡片面板（type-level），首批支持 `latex-compile` 与 `pdf-viewer`
- 验收：
  - `main file` 位于顶部固定区，`custom working directory/path directories/environment variables` 均可通过 `Modify options` 添加
  - 编译序列可选中某一步，底部设置面板随选中步骤切换
  - 不引入新的步骤 schema 持久化字段，兼容执行路径不变

### Phase 9：Step-Based Tree Log Tab

- 状态：`DONE`
- 目标：为 step 执行流程提供“按步骤分组”的树形日志，同时保留原 `Log Messages` 标签。
- 任务：
  - 引入 `StepAwareSequentialProcessHandler` 的步骤事件与每步 raw log 分桶
  - 新增 `LatexStepLogTabComponent`（左树右日志）并按 step type 选择解析器
  - 新增解析会话层：`LatexStepMessageParserSession`、`BibtexStepMessageParserSession`、`NoopStepMessageParser`
  - 在 `LatexRunConfiguration.createAdditionalTabComponents` 中对 step 流程增加 `Step Log` 标签
- 验收：
  - step 流程运行时出现 `Step Log` 标签（legacy 流程不变）
  - 树中显示步骤状态，warning/error 挂在对应步骤下
  - 右侧按步骤查看 raw log，失败后后续步骤标记为 skipped
  - 现有 `Log Messages` 在 step 流程下继续可用

### Phase 10：`LatexRunConfiguration` 结构化重构（`common + steps`）

- 状态：`IN_PROGRESS`
- 目标：把运行配置主存储从扁平字段迁移为 `model.common + model.steps + model.ui`。
- 已完成：
  - 引入 `LatexRunConfigModel` / `LatexCommonSettings` / `LatexStepConfig`（实例级 step 参数）。
  - 执行链 `LatexRunStepPlanBuilder` 改为接收 `List<LatexStepConfig>`。
  - `bibtex/makeindex/external-tool` 改为强类型 step provider（不再依赖辅助 run config 集合）。
  - `Compile sequence` 改为维护 step 实例（含 `stepId`），`Step settings` 改为按 `stepId` 编辑并保存可见项。
  - `RunConfigurationOptions` 已承载新 `model` 状态；`LatexRunConfiguration` 以 `model` 为主入口。
- 待完成：
  - 清理 legacy/桥接类与旧字段调用路径（当前仍保留 deprecate 适配层）。
  - 收敛并更新全部旧 schema / migration 测试为 V2 断言。
  - 完成 `LatexRunConfigurationPersistence` 与 options-only 路线二选一收口。

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
- [x] Phase 7: 清理与收口
- [x] Phase 8: 三段式 UI 与步骤设置面板
- [x] Phase 9: Step-Based Tree Log Tab
- [x] Phase 9.1: Step Log 收口（唯一输出页签 + 根节点汇总输出）
- [ ] Phase 10: `LatexRunConfiguration` 结构化重构

## 更新日志

- 2026-02-22
  - 初始化迁移文档。
  - 基于 `run-config-ui` 和 `new-ui` 的结构差异，确定“先模型、后 UI、最后清理”的迁移顺序。
  - 明确不直接迁移旧分支中未完成的自定义执行控制台实现。
- 2026-02-22（Phase 0 细化）
  - 冻结了 XML 兼容契约与冲突优先级。
- 2026-02-24（Phase 9）
  - 落地 Step 流程专用 `Step Log` 标签，新增树形步骤日志视图（步骤状态 + 结构化消息 + 每步 raw log）。
  - 执行层新增 step-aware 顺序处理器与步骤事件模型，支持文本转发与分桶缓存。
  - 解析层新增按步骤类型分派（LaTeX/latexmk、BibTeX、Noop）并补充对应测试。
- 2026-02-24（Phase 9.1）
  - `createAdditionalTabComponents` 收口为仅注册 `Step Log`，不再显示其它运行输出附加页签（legacy 代码保留为回滚入口）。
  - `Step Log` 左侧选中根节点时，右侧 Console 改为汇总回放全部步骤输出（按步骤顺序拼接），不再仅显示当前步骤。
  - 更新 `LatexStepLogTabComponentTest`，覆盖根节点汇总输出断言与 UI 行为回归。
- 2026-02-24（Framework 对齐：sequence + step）
  - `LatexRunConfiguration.getState()` 收敛为 step pipeline 主路径：优先使用 `stepSchemaTypes`，无可执行步骤时回退到推断步骤序列，不再回落到 legacy `LatexCommandLineState`。
  - `LatexStepRunState` 将 `Step Log` 组件作为主运行 console（`DefaultExecutionResult(stepLogConsole, handler)`），避免运行实例出现空白内容页。
  - `LegacyAuxRunConfigurationsStep` 改为内联执行子 run config（`RunProfileState.execute(...).processHandler`），不再通过 `RunConfigurationBeforeRunProvider` 触发独立运行实例。
  - 抽取 `LatexRunStepTypeInference` 统一步骤推断逻辑，供 persistence / UI / runtime 共用。
- 2026-02-24（Phase 10 进行中）
  - 引入 `LatexRunConfigModel(common + steps + ui)`，并将 `LatexRunConfiguration` 主数据入口切换到 `model`。
  - `Compile sequence` 组件改为维护 `LatexStepConfig` 实例（含 stepId），不再只维护 step type 字符串。
  - `Step settings` 改为按选中 stepId 绑定 `Fragmented` 子 editor，`Modify options` 可见项状态改为按 stepId 保存。
  - 执行层 `LatexRunStepPlanBuilder` / provider 契约改为基于 `LatexStepConfig`，并新增 `BibtexRunStep` / `MakeindexRunStep` / `ExternalToolRunStep`。
- 2026-02-24（Phase 8 收尾优化）
  - `Step settings` 子标题下沉到分割线之后（作为子 editor 内固定说明片段），避免标题区拥挤。
  - `Step settings` 各可选项补齐 hover hint，行为与 `Common settings` 对齐。
  - `latex distribution` 从步骤设置迁移到 `Common settings`，作为共通可选项统一管理。
  - `path directories` 拆分为 `Output directory` 与 `Auxiliary directory` 两个独立单行 fragment。
  - `Compile sequence` 进入页面默认不选中任何步骤；用户单击步骤即选中，选中联动 `Step settings`。
  - 移除 `Compile sequence`/`Step settings` 区域底部灰字说明，改为悬停 tooltip 提示（与其他设置页交互风格一致）。
  - `Common settings` 碎片全部改为 editor 行（不再作为 command-line 横向排版），并统一移除 `setHint` 灰字提示，改为悬停 tooltip。
  - 删除 step 子 editor 内的 `typeLevelHint` 行，避免重复说明占位。
  - 为各 step 子 editor 增加独立 header fragment（`LaTeX compile step` / `latexmk step` / `PDF viewer step`）。
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
- 2026-02-23（Phase 7 完成）
  - 新增主碎片路径配置：`Output directory` 与 `Auxiliary directory`，默认通过 `Modify options` 按需启用。
  - `LegacyLatexSettingsEditor` 中 aux/out 行收口为隐藏只读过渡态，并停止对 `runConfig.outputPath/auxilPath` 写入，消除双入口冲突。
  - `LatexCommandLineState` 注释更新为 legacy fallback pipeline，明确新主路径为 `LatexStepRunState`。
  - 更新运行配置测试，覆盖路径碎片挂载与 legacy 路径入口隐藏行为。
- 2026-02-24（Phase 8 完成）
  - `LatexSettingsEditor` 重排为三段式：`Common settings`（main file + modify options）、`Compile sequence`、`Step settings`。
  - `LatexCompileSequenceComponent` 新增选中状态、选中回调、高亮，以及增删拖拽后的选择保持逻辑。
  - 新增 `LatexStepSettingsComponent` / `LatexStepSettingsFragment`：基于卡片显示步骤设置，支持 `latex-compile` 与 `pdf-viewer`，其它步骤展示过渡提示。
  - 新增/更新测试：编辑器结构、编译序列选中行为、步骤设置卡片切换与 reset/apply 回归。
- 2026-02-24（Phase 8 收口：Step settings 可选项 + latexmk 独立步骤）
  - `Step settings` 为 compile/viewer 卡片加入 `Modify options` 弹出菜单，可按需隐藏高级字段。
  - 新增 `latexmk-compile` 独立步骤类型，并接入 provider、UI 类型描述、自动推断与 legacy 推断映射。
  - 步骤执行层对 `latexmk-compile` 强制使用 `LatexCompiler.LATEXMK`，不依赖当前全局 compiler 选择。
  - 更新步骤 schema/pipeline/UI 相关测试，覆盖 `latexmk-compile` 映射与推断行为。
- 2026-02-24（Phase 8 结构解耦）
  - `Step settings` 拆分为多组件：`LatexCompileStepSettingsComponent`、`LatexmkStepSettingsComponent`、`LatexViewerStepSettingsComponent`、`LatexUnsupportedStepSettingsComponent`。
  - `LatexStepSettingsComponent` 仅保留路由与卡片切换职责，按步骤类型分发到对应设置组件。
- 2026-02-24（Phase 8 嵌套子 Editor 路线）
  - `Step settings` 切换为每个步骤类型一个嵌套 `FragmentedSettingsEditor`：`LatexCompileStepFragmentedEditor`、`LatexmkStepFragmentedEditor`、`LatexViewerStepFragmentedEditor`。
  - `Modify options` 改为直接复用平台 fragment 行为，移除各卡片手写的 options popup 与显隐状态管理。
  - 新增 `StepFragmentedState` 隔离子 editor 的 `selectedOptions`，避免与主 `LatexSettingsEditor` fragment 状态互相污染。
  - 新增 `step-ui-options` 持久化（按 `step type + option id`），并在读取时过滤非法 `type/id`。
