# AnyDoor 任意门

目标：执行Spring项目任意对象的任意方法

最初想法提出人：[Lin ZiHao](https://github.com/schneiderlin)

## 适合场景
- xxlJob
- rpc
- mq入口
- 小改动的测试

## 快速开始（结合插件）
> 前置要求：需要运行项目有 spring-context 依赖，若没有见插件的readme

下载插件，启动项目，选择方法，右键执行

[AnyDoorPlugin-开源地址](https://github.com/lgp547/any-door-plugin)

[AnyDoorPlugin-idea插件中心](https://plugins.jetbrains.com/plugin/20385-anydoor)

## 支持功能
- 对象相关：
  - 对于是Spring注册的Bean，会通过上下文拿到对象（若有代理既是代理对象），执行当前方法
  - 对于非Spring注册的Bean，会通过反射创建对象，执行当前方法
- 方法相关：
  - 支持同步或异步执行当前方法，默认异步
  - 支持私有方法
  - 打印响应结果
- 参数相关：
  - 对给的参数进行序列化，支持json格式
  - 函数的参数支持lambda表达式入参，例如：Function的可以 `A -> A`
  - 时间支持：LocalDateTime传yyyy-MM-dd'T'HH:mm:ss


## 发布版本
### 0.0.2
- 支持Bean私有方法
- 支持空字符串要解析成对象，不再是null
- 支持没有注册Spring对象进行执行
- 打印响应结果
- 调用的方法是否会走代理？会走
- 修复null参数
- 修复String类型
- 修复List类型的泛型映射

### 0.0.3
- 支持同步或异步执行，默认异步

### 0.0.4
- 修复代理Bean的私有方法调用
- 字符串类型传入null存在null字符串

### 0.0.5
- 修改支持jdk8

### 0.0.7
- 支持通过Attach进行调度运行项目

### 0.0.8
- 当同步执行时时不使用CompletableFuture以简化调用栈

### 0.0.9
- 支持lambda表达式入参

### 0.0.10
- 修复Attach传递参数过长情况（通过文件传递）
- 调整依赖，autoconfigure需要提供
- 修复直接通过接口调用方法
- 修复执行重载的私有方法
- 修复时间支持：LocalDateTime传yyyy-MM-dd'T'HH:mm:ss
- 修复Json序列化支持泛型

### 1.0.0 && 1.0.1 重大更新
- 加入Arthas依赖，支持获取到运行时的对象信息
- 调整打包依赖
- 移除mvc依赖、移除spring boot依赖


## 后续支持
详情见issues