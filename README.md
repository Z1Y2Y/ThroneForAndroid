# MyTheone（基于 ThroneForAndroid）

> 📢 **Telegram 交流频道**：[https://t.me/OwnBoxs](https://t.me/OwnBoxs)<br>
> 基于 ThroneForAndroid v2.2.0 与 sing-box，保留 MyTheone 定制并优化交互体验与网络稳定性。

---

### ✨ 核心二改与特色功能

* ⚡ **自动选择器深度重构（核心亮点）**
  * **支持手动选择指定节点**：打破原版只能绑定整组的局限，可在多个订阅间自由勾选任意节点组成专属优选池。
  * **跨分组即时多选**：顶部横向即时切换订阅分组，支持按名称实时搜索过滤与一键全选/反选。
  * **防丢失智能重绑**：独创重绑机制，订阅更新时自动按节点名无感迁移，自选节点绝不丢失变少。
  * **便捷管理与抢跑**：支持一键清空已选列表，支持国家代码过滤排除，健康节点达标后立即抢跑连接。

* 📱 **实况通知灵动胶囊**：锁屏与状态栏动态显示出口 IP、地理位置、节点名及延迟，支持模板自定义与即时热刷新。

* 🎯 **路由扩展出站目标**：规则设置直接展示出站目标，支持直选代理、直连、拦截或指定特定节点，底层全链路生效。

* 🚀 **国内流量 QUIC 嗅探**：默认常驻开启全协议嗅探，调优识别超时，彻底解决国内应用误入代理导致的转圈卡死。

* 🛠️ **Google Play 下载修复**：规则自动放行底层系统下载器，锁定核心 CDN 走远程代理并阻断 QUIC，根治 0% 卡顿。

* 🌐 **DNS 架构治理防死锁**：根除 FakeDNS 循环超时死锁，直连恢复原生 Local 解析，杜绝 DNS 与 TUN 泄露。

* 📶 **TUN 180s 长连接抗断流**：UDP 超时放宽至 180 秒，结合平滑切换打断与全锥型 NAT，彻底解决通话与息屏断流。

* 🔄 **官方 v2.2.0 特性同步**：新增服务可用性检测、路由路径测试、自动选择分层与真实连接学习、逐跳测速、节点置顶、QUIC Opener 与多 DNS 并发竞速。

* 📦 **独立包名无感升级**：采用 MyTheone 独立包名 `com.nb4a.thronef`，与官方原版并存，版本规范递增，覆盖安装不报错。

---

### 📥 快速下载

* **最新 MyTheone APK**：[前往 MyTheone fork 的 Releases 页面](https://github.com/Z1Y2Y/ThroneForAndroid/releases/latest)<br>
  *(主流安卓手机请下载 `arm64-v8a`，全架构通用请下载 `universal`)*

---

### 📜 开源致谢

基于 GPL-3.0 协议开源，致谢上游项目：<br>
[ThroneForAndroid](https://github.com/throneproj/ThroneForAndroid) · [sing-box](https://github.com/SagerNet/sing-box) · [Xray-core](https://github.com/XTLS/Xray-core)
