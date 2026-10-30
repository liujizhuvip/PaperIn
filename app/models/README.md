# 随包资源 / Bundled binaries

这个目录存放**随 APK 一起分发**的二进制文件，使构建与运行都不需要网络。

## document-segmentation.tflite

文档分割模型，来自上游 FairScan 项目发布的模型本体：

- 来源：https://github.com/pynicolas/fairscan-segmentation-model/releases/tag/v1.2.0
- 上游文件名：`fairscan-segmentation-model.tflite`
- 大小：4,921,040 字节
- SHA-256：`96e14d7e610dd0c27b768b228fbc553b4ec119ebe68f3a3594029a25400691d2`

构建时由 `PrepareTFLiteModelTask` 复制到 assets，APK 内改名为
`document-segmentation.tflite`（`ImageSegmentation.kt` 按此名读取）。

更新模型时：替换本文件，并同步修改 `app/build.gradle.kts` 里的 `modelVersion`。

> 该模型未经修改，仅重命名。再分发前请确认其许可允许你的用途（见 `NOTICE`）。
> 校验来源：`sha256sum document-segmentation.tflite`

## OCR 语言包

语言包位于 `app/src/main/assets/tessdata/`，取自上官方 `tesseract-ocr/tessdata_fast`
（Apache-2.0）。校验方式：

```bash
cd app/src/main/assets/tessdata && sha256sum -c sha256.txt
```
