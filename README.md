# 中药学习助手 (Traditional Chinese Medicine Learning App)

一个用于学习中药知识的 Android 应用程序，基于 Jetpack Compose 构建。

## 功能特点

- **中药数据库**：包含常见中药的详细信息，如性味归经、功效、主治等
- **分类浏览**：根据中药分类（补气类、补血类、清热类等）浏览中药
- **详细介绍**：查看每种中药的详细信息和用法用量
- **学习课程**：提供中药学习课程，包括基础知识、配伍原则等
- **知识测验**：通过测验检验学习成果
- **个人中心**：记录学习进度和测验成绩

## 技术栈

- **Kotlin**：主要开发语言
- **Jetpack Compose**：现代化 UI 开发工具包
- **Navigation Compose**：处理应用内导航
- **Material Design**：遵循 Material Design 设计规范

## 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/tcm/traditionalchinesemedician/
│   │   │   ├── data/            # 数据模型和存储库
│   │   │   ├── ui/              # UI 组件
│   │   │   │   ├── screens/     # 各个屏幕
│   │   │   │   └── theme/       # 主题和样式
│   │   │   └── MainActivity.kt  # 主活动
│   │   └── res/                 # 资源文件
│   │       ├── values/          # 字符串、颜色、主题等
│   │       └── ...
│   └── ...
└── ...
```

## 安装要求

- Android 7.0 (API 级别 24) 或更高版本
- 大约 50MB 的存储空间

## 如何运行

1. 克隆本仓库
2. 在 Android Studio 中打开项目
3. 构建并运行应用程序

## 后续计划

- [ ] 添加更多中药数据
- [ ] 实现用户账户系统
- [ ] 添加中药配伍功能
- [ ] 添加中药图片库
- [ ] 实现离线数据存储
- [ ] 支持多语言（中英文）

## 贡献

欢迎提交 Pull Request 或创建 Issue 来帮助改进这个项目。

## 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。 