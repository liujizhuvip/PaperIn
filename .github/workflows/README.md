# CI 说明

`.github/workflows/build.yml` 在 GitHub 的 runner 上打包 APK，本地不需要装 Android SDK。

## 触发方式

| 事件 | 行为 |
| --- | --- |
| push 到 `main` | 构建 debug 与**已正式签名**的 release APK，产物在 Actions run 页面的 Artifacts 里 |
| push `v*` 标签 | 同上，并自动创建 GitHub Release，把 APK 作为附件直接挂上去（无需登录即可下载） |
| Pull Request | 只构建 debug APK，用于验证改动能编译 |
| 手动 | Actions → Build APK → Run workflow |

## 产物

每次构建产出 6 个 APK（debug / release 各 3 个，按 ABI 拆分，
`PaperIn-1.0.0-<buildType>-<abi>.apk`）：

- `arm64-v8a`：绝大多数手机
- `armeabi-v7a`：2016 年前的老设备
- `x86_64`：模拟器 / x86 平板

发 Release 时只挂**已签名的 release 包**（debug 包体积三倍且签名不同，不作分发）；
若某次构建没配签名 secret，才退回挂 debug 包，避免 Release 空着。

## 打标签发版

```bash
git tag v1.0.0
git push origin v1.0.0
```

CI 跑完后到 Releases 页面就能看到带 APK 附件的版本，下载链接是公开的——
比 Actions 的 Artifacts 方便（后者必须登录 GitHub 才能下载）。

## 签名密钥

release APK 由工作流自动签名。签名材料存放在仓库的 Actions secrets 里
（Settings → Secrets and variables → Actions）：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE_BASE64` | keystore `.jks` 的 base64（单行） |
| `KEYSTORE_PASSWORD` | keystore 密码（store 与 key 同一密码） |
| `KEY_ALIAS` | `paperin` |

工作流把 `KEYSTORE_BASE64` 解码成 `keystore/release.jks`，生成
`keystore/keystore.properties`，再跑 `assembleRelease`。验签步骤会检查
APK 是否真的没有 `INTERNET` 权限——如果依赖库哪天把它带回来了，构建会直接失败，
而不是悄悄发一个"能联网"的包出去。

> 未配置这三个 secret 时，工作流只出 debug 包（用 runner 临时 key 签名）；
> 删掉 secret 即可回到该状态，不需要改工作流文件。
