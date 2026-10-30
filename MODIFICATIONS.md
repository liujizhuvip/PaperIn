# 修改说明 / MODIFICATIONS

本文件依据 GNU GPLv3 第 5(a) 条，记录 **PaperIn（纸间扫描）** 相对于上游项目
**FairScan**（https://github.com/pynicolas/FairScan）所做的主要修改。

修改日期：2026-10-30
修改者：The PaperIn authors

---

## 一、标识与包名（品牌独立，避免与上游混淆）

| 项目 | 上游 | 本应用 |
| --- | --- | --- |
| 应用名 | FairScan | PaperIn / 纸间扫描 |
| applicationId / 包名 | `org.fairscan.app` | `com.paperin.scan` |
| 图像处理模块包名 | `org.fairscan.imageprocessing` | `com.paperin.scan.imageprocessing` |
| 评估模块包名 | `org.fairscan.evaluation` | `com.paperin.evaluation` |
| Application 类 | `FairScanApp` | `PaperInApp` |
| Compose 主题 | `FairScanTheme` / `Theme.FairScan` | `PaperInTheme` / `Theme.PaperIn` |
| DataStore 文件名 | `fairscan_settings` | `paperin_settings` |
| 外部调用 action | `org.fairscan.app.action.SCAN_TO_PDF` | `com.paperin.scan.action.SCAN_TO_PDF` |
| 分割模型资源名 | `fairscan-segmentation-model.tflite` | `document-segmentation.tflite` |
| PDF Creator 元数据 | `FairScan x.y.z` | `PaperIn x.y.z` |
| 版本号 | 2.3.0 (96) | 1.0.4 (13) |

> 说明：分割模型仍然从上游仓库下载（该文件是上游发布的模型本体，未做修改），
> 仅在 APK 内换了资源文件名，因此下载 URL 保持不变。

## 二、UI 重做（蓝白风格）

整体视觉从上绿白 Material3 改为蓝白配色，主要改动：

- `ui/theme/Color.kt`：新增蓝色主色板（`#2B6CFF` 系）、近白背景 `#F4F6FB`、
  白色分层容器色，以及 `Brand` 对象（渐变色、取景框色、闪光色）。深色模式同步调整。
- `ui/theme/Theme.kt`：补充 `surfaceContainer*` 与 `outlineVariant` 映射，
  新增圆角体系（8/12/16/20/28dp）。
- `ui/theme/Type.kt`：标题字重提升，中文场景下字距归零。
- `ui/components/Buttons.kt`：主按钮改为蓝色胶囊（48dp 高）；
  新增次级胶囊按钮 `GhostActionButton`、浮动图标按钮（白底圆角+投影）、
  圆形半透明返回按钮、白色胶囊计数徽标 `CountBadge`。
- `ui/components/Scaffold.kt`：底部面板改为白色、上圆角 24dp、带投影的浮层；
  去掉 `BottomAppBar`，改为自绘操作区并处理导航栏内边距；菜单与返回按钮加半透明底。
- `ui/components/PageList.kt`：缩略图圆角 12dp，选中态蓝色 2dp 描边 + 投影，
  页码徽标改为主色圆角标签。
- `screens/camera/CameraScreen.kt`：新增 `ScanFrameOverlay` 四角取景框
  （未检测到文档时白色 + 提示文案，检测到后转蓝），快门改为白环 + 蓝色内芯 +
  半透明光晕，手电筒按钮加圆形底，底部操作改为双胶囊按钮。
- `screens/camera/CameraPreview.kt`：检测四边形改为蓝色描边 + 外发光。
- `screens/document/DocumentScreen.kt`：预览图放入白色圆角卡片（带投影），
  页码改为蓝色胶囊，底部按钮改为胶囊。
- `screens/export/ExportScreen.kt`：信息卡与操作卡改为 20dp 圆角白卡 + 细边，
  文件名输入框 14dp 圆角，导出按钮改为胶囊，顶栏改为透明背景 + 大标题。
- `screens/settings/SettingsScreen.kt`：设置项收纳进 24dp 圆角白卡，
  章节标题改为主色小标题，目录选择卡改为淡蓝底。
- `screens/about/AboutScreen.kt`：顶部改为蓝色渐变头部（Logo + 名称 + 标语），
  各分区改为白卡，新增"开源声明"分区。
- `screens/about/Emails.kt`：不再公开邮箱地址，也删除了"用邮件发送问题反馈"
  与"附带最近一张照片发邮件"两个入口；问题反馈统一走 GitHub Issues
  （新增 `ISSUES_URL`，由 `SOURCE_URL` 派生），About 页给出可点击的 Issues 链接。
  本修改版源码地址 `SOURCE_URL` 与上游地址 `UPSTREAM_URL` 一并保留。
- `screens/about/AboutViewModel.kt`：移除 `PrepareEmailWithLastImage` 事件、
  `onContactWithLastImageClicked()` 与 `hasLastCapturedImage` 状态（`AboutUiState`
  整体删除，About 页不再依赖 `ImageRepository`）；日志内的标题由
  `FairScan diagnostics report` 改为 `PaperIn diagnostics report`。
- `screens/ResumeScanScreen.kt`：继续扫描区改为淡蓝圆角卡片，按钮改胶囊。

## 三、品牌与文案

- 新增图标：自适应图标前景 `ic_launcher_foreground.xml`、
  单色层 `ic_launcher_monochrome.xml`、背景色 `#2B6CFF`，
  以及各 dpi 的 `ic_launcher(.round).webp` 与 `drawable/icon.png`。
- `res/values/strings.xml`、`values-zh/strings.xml`：应用名、标语、
  许可说明、开源声明、取景提示等文案重写；新增 `scan_hint`、
  `about_upstream_title`、`about_upstream_body`。
- `screens/about/Emails.kt`：联系方式/官网/上游地址改为本应用常量。
- `metadata/`：删除上游截图与 F-Droid 变更日志（含原品牌界面），
  重写标题与描述，新增 `zh-CN`。

## 四、构建

- `app/build.gradle.kts`：`namespace`/`applicationId` 改为 `com.paperin.scan`，
  版本号重置，APK 输出名 `PaperIn-<version>-<abi>.apk`。
- 模型任务由 `DownloadTFLiteModelTask` 改为 `PrepareTFLiteModelTask`：优先从
  `app/models/document-segmentation.tflite` 复制，缺失时才回退下载，失败则显式报错。
- `settings.gradle.kts`：`rootProject.name` 改为 `PaperIn`。

## 五、离线改造（相对上游的行为变化）

上游允许在应用内联网下载 OCR 语言包；本版本改为完全离线。

- `OcrLanguageRepository`：删除 `downloadLanguage()` 与全部 `HttpURLConnection`
  代码，新增 `provisionBundledLanguages()`，首次启动把 `assets/tessdata/*.traineddata`
  复制到 `files/tesseract/tessdata/`（已存在则跳过）。
- 新增 `applyDefaultLanguages()`：首次启动按系统语言自动启用对应语言 + 英语，
  避免"装完发现 OCR 不可用"。
- `OcrLanguage.AVAILABLE_LANGUAGE_CODES`：由上游 130 项收敛为随包内置的 11 项。
- `OcrLanguagesScreen`：去掉"可下载语言 / 建议语言 / 下载进度 / 删除"交互，
  改为内置语言开关列表，并保证至少保留一种语言。
- `SettingsViewModel` / `SettingsUiState`：移除下载状态与相关方法。
- `AndroidManifest.xml`：移除 `INTERNET` 权限声明，并用 `tools:node="remove"`
  阻止依赖库回填。
- `assets/tessdata/`、`app/models/`：新增随包资源，使构建与运行均无需联网。

## 六、图像效果（彩色模式纸面提白）

上游的彩色增强（`multiScaleRetinexOnL`）只处理 Lab 的 L 通道，a/b 原样保留，
所以均匀了光照却保留了光照的颜色：暖光下纸面发黄，阴影下纸面发灰。

- `PostProcessing.kt`：新增 `whitenPaper()`，在彩色增强的最后一步按通道估计纸面亮度再逐
  通道相除。纸面因此落到同一个接近纯白的电平上，同时把环境光的色偏一起去掉；比纸暗的内
  容（文字、印章、照片）保持相对亮度与色相。
  - 光照场只由"纸面像素"（三通道最大值高于 p95 的 70%）统计，其余内容通过归一化卷积
    `blur(通道×纸面掩膜) / blur(掩膜)` 外推得到。直接用闭运算会把任何宽于核的深色块
    当成纸并提亮，印章与照片会被洗白，故不采用。
  - 估计在 480px 的小图上做，再放大回原分辨率，代价与拍摄分辨率无关；
  - 逐通道串行处理，4 MP 页面不会同时存在三份 float 缓冲；
  - 增益上限 3 倍、纸面覆盖率低于 5% 时整体跳过（画面里没有纸时不做提亮）；
  - 目标电平 253 而非 255，给 JPEG 色度抽样留余量，背景不会糊成死白。
- 新增单元测试 `PaperWhiteningTest`：光照场核尺寸必须为奇数、在 [9,81] 内且随工作尺寸增长。

## 七、图像效果（输出分辨率与锐化）

上游默认导出上限 2 MP（A4 约 150 dpi），高质量档 4 MP（约 210 dpi），且透视校正之后没有
任何锐化。拍摄输入是 4400×3300（14.5 MP），默认导出因此丢掉了约 85% 的像素，剩下的边缘
又是插值出来的软边——这是"不如扫描仪"的主要来源。

- `domain/ExportQuality.kt`：三档像素上限改为以 A4 为基准的扫描仪规格。
  低 2.0 MP（约 150 dpi，即旧"平衡"档）、平衡 8.7 MP（约 300 dpi，扫描仪默认值）、
  高 12.0 MP（约 350 dpi）；JPEG 质量 70 / 85 / 92（原 60 / 75 / 80）。
  同时去掉黑白模式的 `maxPixels × 4` 倍数：该倍数在 2 MP 时代用于补偿单通道的存储成本，
  现在基数已是 300 dpi，再放大四倍只会让 Sauvola 在大图上分配数百 MB 的 float 缓冲。
- 新增 `Sharpening.kt`：`sharpenDocument()`，在透视校正之后做一次 unsharp mask
  `out = src + 0.6 × (src − blur(src))`，半径随页面尺寸缩放（A4 300 dpi 约 1.3 px）。
  差值小于 2 灰阶的部分不计入高通，避免放大传感器噪点与 JPEG 块；黑白模式跳过，
  二值化后每条边已是黑白两色，再锐化只会加粗笔画。
  - 按 256 行分块处理，块与块之间留 3σ 的上下文再裁掉，因此 12 MP 页面的 float 缓冲
    保持在个位数 MB（整页 float 副本要 140 MB 以上）。
- `DocumentDetection.extractDocument()`：各阶段用完即释放，不再让 warp / resize / enhance
  三份全分辨率图同时存在；`PostProcessing.multiScaleRetinexOnL()` 在生成小图后立即释放
  全分辨率的 L 副本。
- 新增单元测试 `SharpeningTest`：半径随页面尺寸增长且被限制在 [0.8, 1.6] px。

代价：单页 JPEG 从约 0.4 MB 增至约 2 MB，内存峰值相应上升；低端机若吃紧，可在设置里选
"低"档（2 MP / 150 dpi），该档与旧版平衡档的画质一致。

## 八、未修改的部分

- `evaluation` 模块未改动，仅改了包名。
- `imageprocessing`：除上文新增的彩色纸面提白与锐化外，文档检测、透视校正、
  黑白二值化（Sauvola）、灰度增强的算法实现未改动。
- PDF 生成与 OCR 逻辑未改动。
- `LICENSE`（GPLv3 全文）与各源文件的原始版权头未改动。
