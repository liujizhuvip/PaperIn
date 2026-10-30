# CI 说明

`.github/workflows/build.yml` 在 GitHub 的 runner 上打包 APK，本地不需要装 Android SDK。

## 触发方式

| 事件 | 行为 |
| --- | --- |
| push 到 `main` | 构建 debug APK，产物在 Actions run 页面的 Artifacts 里 |
| push `v*` 标签 | 同上，并自动创建 GitHub Release，把 APK 作为附件直接挂上去（无需登录即可下载） |
| Pull Request | 只构建 debug APK，用于验证改动能编译 |
| 手动 | Actions → Build APK → Run workflow |

## 产物

每次构建产出 3 个 APK（按 ABI 拆分，`PaperIn-1.0.0-<abi>.apk`）：

- `arm64-v8a`：绝大多数手机
- `armeabi-v7a`：2016 年前的老设备
- `x86_64`：模拟器 / x86 平板

## 打标签发版

```bash
git tag v1.0.0
git push origin v1.0.0
```

CI 跑完后到 Releases 页面就能看到带 APK 附件的版本，下载链接是公开的——
比 Actions 的 Artifacts 方便（后者必须登录 GitHub 才能下载）。

## 可选：签名密钥

默认只构建 debug APK（用 runner 生成的 debug key 签名，可直接安装）。
要产出**正式签名**的 release APK（可上架、可覆盖升级），在
Settings → Secrets and variables → Actions 里加 3 个 secret：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE_BASE64` | `base64 -w0 keystore/paperin-release.jks` 的输出 |
| `KEYSTORE_PASSWORD` | keystore 密码（store 与 key 同一密码） |
| `KEY_ALIAS` | `paperin` |

配置后工作流会自动多跑一步 `assembleRelease`，并在验签步骤里检查 APK 是否
真的没有 `INTERNET` 权限——如果依赖库哪天把它带回来了，构建会直接失败，
而不是悄悄发一个"能联网"的包出去。

> 把 keystore 放进 GitHub Secrets 意味着签名密钥存了一份在 GitHub。
> 公开仓库的 fork PR 读不到 secrets，风险可控；不放也完全可以，
> 那就本地 `./gradlew :app:assembleRelease` 出正式包。
