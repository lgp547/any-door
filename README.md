# AnyDoor 任意门

目标：执行Spring项目任意对象的任意方法

执行项目的任意方法提出者：[Lin ZiHao](https://github.com/schneiderlin)

## 适合场景
- xxlJob
- rpc入口
- mq入口
- 小改动的测试

## 功能展示
![img](dosc/image/功能展示.gif)


## 快速开始（结合插件）
> 前置要求：需要运行项目有 spring-context 依赖
### 1 安装插件
插件市场直接搜索 anydoor

![img.png](dosc/image/安装插件.png)

### 2 启动项目
说明：一般情况下会主动填充运行进程的进程id（可在配置页面修改）

### 3 执行调用
1. 找到想要执行的方法，右键弹出选择打开任意门（有对应的快捷键）

![img.png](dosc/image/打开任意门.png)

2. 填写要调用的参数，并点击 ok 按钮

![img.png](dosc/image/启动.png)

3. **你将会发现当前方法被执行了！（可进行断点查看）**

## 插件属性说明

![img.png](dosc/image/配置页.png)


## 核心包支持功能
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
  - 时间支持：LocalDateTime传yyyy-MM-dd'T'HH:mm:ss、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd

## 插件支持功能
- 执行参数将会缓存
- 可在任意地方重复上一次的调用
- 配置页面可打印Idea插件的路径
- 保留通过Http进行调用



## 地址
[AnyDoorPlugin-idea插件中心](https://plugins.jetbrains.com/plugin/20385-anydoor)

## 后续支持
详情见issues