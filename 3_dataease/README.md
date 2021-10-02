<p align="center"><a href="https://dataease.io"><img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/dataease-logo.png" alt="DataEase" width="300" /></a></p>
<h3 align="center">人人可用的开源数据可视化分析工具</h3>
<p align="center">
  <a href="https://www.gnu.org/licenses/old-licenses/gpl-2.0"><img src="https://img.shields.io/github/license/dataease/dataease?color=%231890FF&style=flat-square" alt="License: GPL v2"></a>
  <a href="https://app.codacy.com/gh/dataease/dataease?utm_source=github.com&utm_medium=referral&utm_content=dataease/dataease&utm_campaign=Badge_Grade_Dashboard"><img src="https://app.codacy.com/project/badge/Grade/da67574fd82b473992781d1386b937ef" alt="Codacy"></a>
  <a href="https://github.com/dataease/dataease/releases/latest"><img src="https://img.shields.io/github/v/release/dataease/dataease" alt="Latest release"></a>
  <a href="https://github.com/dataease/dataease"><img src="https://img.shields.io/github/stars/dataease/dataease?color=%231890FF&style=flat-square" alt="Stars"></a>
  <a href="https://github.com/dataease/dataease/releases/latest"><img src="https://img.shields.io/github/downloads/dataease/dataease/total" alt="Downloads"></a>
</p>
<hr />
DataEase 是开源的数据可视化分析工具，帮助用户快速分析数据并洞察业务趋势，从而实现业务的改进与优化。DataEase 支持丰富的数据源连接，能够通过拖拉拽方式快速制作图表，并可以方便的与他人分享。

### DataEase 的功能：

-   图表展示：支持 PC 端、移动端及大屏;
-   图表制作：支持丰富的图表类型(基于 Apache ECharts 实现)、支持拖拉拽方式快速制作仪表板;
-   数据引擎：支持直连模式、本地模式(基于 Apache Doris / Kettle 实现);
-   数据连接：支持关系型数据库、Excel 等文件、Hadoop 等大数据平台、NoSQL 等各种数据源。

### DataEase 的优势：

-   开源开放：零门槛，线上快速获取和安装；快速获取用户反馈、按月发布新版本；
-   简单易用：极易上手，通过鼠标点击和拖拽即可完成分析；
-   秒级响应：集成 Apache Doris，超大数据量下秒级查询返回延时；
-   安全分享：支持多种数据分享方式，确保数据安全。

## UI 展示

![de-ui](https://www.fit2cloud.com/dataease/images/screenshot/dataease-v1.gif)

## 功能架构

![de-architecture](https://dataease.oss-cn-hangzhou.aliyuncs.com/img/de-architecture.png)

## 在线体验

-   环境地址：<https://demo.dataease.io/>
-   用户名：demo
-   密码：dataease

## 快速开始

仅需两步快速安装 DataEase：

1.  准备一台不小于 8 G内存的 64位 Linux 主机；
2.  以 root 用户执行如下命令一键安装 DataEase。

```sh
curl -sSL https://github.com/dataease/dataease/releases/latest/download/quick_start.sh | sh
```

-   [在线文档](https://dataease.io/docs/)
-   [演示视频](https://www.bilibili.com/video/BV1UB4y1K7jA)

## 微信群

<img src="https://dataease.oss-cn-hangzhou.aliyuncs.com/img/wechat-group.png" width="156" height="156"/>

## 技术栈

-   后端：[Spring Boot](https://spring.io/projects/spring-boot)
-   前端：[Vue.js](https://vuejs.org/)、[Element](https://element.eleme.cn/)、[Apache ECharts](https://github.com/apache/echarts)
-   中间件：[MySQL](https://www.mysql.com/)
-   数据处理：[Kettle](https://github.com/pentaho/pentaho-kettle)、[Apache Doris](https://github.com/apache/incubator-doris/)
-   基础设施：[Docker](https://www.docker.com/)

## License & Copyright

Copyright (c) 2014-2021 飞致云 FIT2CLOUD, All rights reserved.

Licensed under The GNU General Public License version 2 (GPLv2)  (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<https://www.gnu.org/licenses/gpl-2.0.html>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
