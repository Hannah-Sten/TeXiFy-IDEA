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

- 状态：`TODO`
- 目标：确定新旧 XML 兼容策略与最小可交付范围。
- 产出：
  - 步骤序列序列化草案（新增标签，不破坏现有标签）
  - 回滚策略（开关或 feature flag）
- 验收：
  - 现有 `LatexRunConfigurationTest` 全绿
  - 不改动用户已有配置行为

### Phase 1：在 `new-ui` 引入步骤域模型（无 UI 变更）

- 状态：`TODO`
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

- 状态：`TODO`
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

- 状态：`TODO`
- 目标：保留现有能力，避免一次性移除旧机制。
- 任务：
  - 用“桥接步骤”封装当前 `bibtex/makeindex/external tool` 运行链
  - 把 `RunBibtexListener` / `RunMakeindexListener` / `RunExternalToolListener` 的触发点从“隐式链”迁到“显式步骤”
- 验收：
  - biber/bibtex/makeindex/external tool 仍可执行
  - 失败短路逻辑可配置且可测试

### Phase 4：分片式 UI 外壳落地

- 状态：`TODO`
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

- 状态：`TODO`
- 目标：支持可组合步骤与排序（核心体验）。
- 任务：
  - 引入 `CompileSequenceFragment` 与可视化 step pills
  - 支持新增/删除/双击配置/拖拽重排
- 验收：
  - 用户可按顺序组合 LaTeX/bib/index/pythontex(命令行)/viewer
  - 排序结果与执行顺序一致

### Phase 6：高级步骤与自动推断

- 状态：`TODO`
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

- [ ] Phase 0: 兼容边界冻结
- [ ] Phase 1: 步骤域模型
- [ ] Phase 2: 序列化兼容
- [ ] Phase 3: 旧能力桥接
- [ ] Phase 4: Fragment UI 外壳
- [ ] Phase 5: 序列碎片与拖拽
- [ ] Phase 6: 自动推断与高级步骤
- [ ] Phase 7: 清理与收口

## 更新日志

- 2026-02-22
  - 初始化迁移文档。
  - 基于 `run-config-ui` 和 `new-ui` 的结构差异，确定“先模型、后 UI、最后清理”的迁移顺序。
  - 明确不直接迁移旧分支中未完成的自定义执行控制台实现。

