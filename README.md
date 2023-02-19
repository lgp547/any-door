# AnyDoor 任意门

目标：执行Spring项目任意对象的任意方法

最初灵感来源：[Lin ZiHao](https://github.com/schneiderlin)

目前阶段：要求是Spring web项目，本jar包会在项目提供一个对外的路径(/any_door/run)，该路径实现了调用到任意一个方法

目前情况：由于更多的是本地环境使用，以及每次填写请求参数过于麻烦，故开发了一个对应[任意门AnyDoor](https://plugins.jetbrains.com/plugin/20385-anydoor)插件，做到简单配置即可快速使用

## 快速开始
请选择 插件方式 还是 常规方式

推荐使用 **插件方式**


### 1.插件方式（快速使用）
本插件只需每个项目配置一次即可

[任意门AnyDoor插件](https://plugins.jetbrains.com/plugin/20385-anydoor)

[任意门AnyDoor插件github地址](https://github.com/lgp547/any-door-plugin)

### 2.常规方式（导包启动）

#### 添加依赖
```xml
<dependency>
    <groupId>io.github.lgp547</groupId>
    <artifactId>any-door</artifactId>
    <version>0.0.5</version>
</dependency>
```

#### 启动项目
有打印出日记说明添加成功（是info级别）
![img.png](dosc/image/启动成功.jpg)

#### 调用接口

路径：/any_door/run

端口：既是启动项目所在的端口

参数说明：
- className      要调用的类的全链路名
- methodName     要调用的方法名
- content        入参参数，要求是json类型（允许为null）
- parameterTypes 参数类型 （若是方法名是唯一的，允许为空）

```shell script
curl --location --request GET 'http://localhost:8080/any_door/run'
--header 'Content-Type: application/json' \
--data-raw '{
    "content": {"name":"any_door"},
    "methodName": "oneParam",
    "className": "io.github.lgp547.anydoor.core.test.Bean",
    "parameterTypes": []
}'
```
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

## 适合场景
- xxlJob
- rpc
- mq入口
- 小改动的测试


## 后续支持
- 对于Dao层进行支持，目前会有丢失参数的情况、增加测试方法
- 对LocalDateTime(OffsetDateTime)的支持、增加测试方法
- 对于没有注册到Spring类，并且没有无参构造函数的类进行支持

- 是否能做到调度到任意的测试方法
- 匿名内部类以及懒加载的时候调用方法

- [x] 对于不支持的类型，那就直接null进去
- [x] jar包打印版本和端口号
- [x] 支持不依赖web作为入口，支持Spring boot项目