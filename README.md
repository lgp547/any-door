# AnyDoor 任意门
简单来说就是执行任意对象的任意方法

需要是Spring web项目，会在原有项目提供一个对外的路径(/any_door/run)，该路径可以调用到任意一个方法


## 快速使用
推荐结合IDEA插件使用！插件帮做了导入jar包以及封装了调用接口，做到快速使用

### IDEA插件使用
本插件只需每个项目配置一次即可
#### 添加插件
暂未对外发布

#### 配置插件属性并进行导入
- 配置插件属性

![img.png](dosc/image/插件配置说明.jpg)

- 插件属性配置完毕后点击尝试导入按钮，导入成功会有消息提示


#### 执行调用
- 找到想要执行的方法，右键弹出选择（有对应的快捷键）

![img.png](dosc/image/打开方法选择.png)

- 选择打开任意门

![img.png](dosc/image/打开任意门.png)

- 填写要调用的参数，并执行启动！

![img.png](dosc/image/启动.png)

### 常规使用
#### 添加依赖
```xml
<dependency>
    <groupId>io.github.lgp547</groupId>
    <artifactId>any-door</artifactId>
    <version>0.0.2</version>
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
    "className": "io.github.lgp547.anydoor.core.Bean",
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

