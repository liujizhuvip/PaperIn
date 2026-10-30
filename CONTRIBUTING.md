## 参与贡献

感谢你愿意改进纸间扫描（PaperIn）。

本项目是 [FairScan](https://github.com/pynicolas/FairScan) 的衍生版本，
请先阅读：

- [MODIFICATIONS.md](MODIFICATIONS.md)：相对上游做了哪些修改
- [COMPLIANCE.md](COMPLIANCE.md)：GPLv3 义务与发布注意事项

### 项目定位

主打**简单**：用户几步之内拿到一份干净、可分享的 PDF，不需要手动调参。

- 目标不是复刻同类应用的全部功能
- 目标不是让用户控制 PDF 生成的每个细节
- 目标不是为某个垂直场景牺牲通用简洁性

### 提交代码

1. 保持所有源文件头部的原始版权声明，新增文件使用 `LICENSE_HEADER` 模板
2. 修改上游文件时，在头部追加 `Modifications Copyright 2026 The PaperIn authors (see NOTICE)`
3. UI 改动请沿用 `ui/theme/` 里的色板与圆角体系，不要在页面里硬编码颜色
4. 提交前跑 `./gradlew :app:check :imageprocessing:check`

### 反馈问题

反馈请提交到本项目的仓库 Issue，而不是上游 FairScan 的仓库。

如果是自动处理效果不好，请尽量附上**原始图片**：在"关于 → 支持 →
报告最后一张照片的问题"里可以取到原图。截图通常无法帮助定位图像处理问题。

描述问题时，请优先说清楚"在什么场景下、期望什么、实际发生了什么"，
比直接提出某个改法更有价值。

### 翻译

上游使用 Weblate 管理翻译。本项目的文案改动集中在
`app/src/main/res/values/strings.xml` 与 `values-zh/strings.xml`，
新增字符串请同时补齐英文与中文。
