# KnowFlow 部署方式

本文档说明本地源码部署流程。推荐先用 Docker 启动 MySQL、Redis、MinIO、Elasticsearch 8.x 和 Kafka，再分别启动后端与前端。

## 1. 准备环境

需要提前安装：

- JDK 17+
- Maven 3.8+
- Node.js 18.20+
- pnpm 8.7+
- Docker / Docker Compose

## 2. 配置 `.env`

在项目根目录复制环境变量模板：

```bash
cp .env.example .env
```

Windows PowerShell 可使用：

```powershell
Copy-Item .env.example .env
```

然后修改 `.env` 中的密码和密钥，至少需要替换这些值：

- MySQL：`MYSQL_ROOT_PASSWORD`、`SPRING_DATASOURCE_PASSWORD`
- Redis：`REDIS_PASSWORD`、`SPRING_DATA_REDIS_PASSWORD`
- MinIO：`MINIO_ROOT_USER`、`MINIO_ROOT_PASSWORD`、`MINIO_ACCESS_KEY`、`MINIO_SECRET_KEY`
- Elasticsearch：`ELASTIC_PASSWORD`、`ELASTICSEARCH_PASSWORD`
- JWT：`JWT_SECRET_KEY`
- 大模型与向量模型：`DEEPSEEK_API_KEY`、`EMBEDDING_API_KEY`

`docs/docker-compose.yaml` 默认 Kafka 不开启密码认证，后端通过 `SPRING_KAFKA_BOOTSTRAP_SERVERS=127.0.0.1:9092` 连接即可。如需 Kafka 密码认证，需要额外开启 Kafka SASL，并同步扩展 Spring Kafka 客户端配置。

如果使用本文档提供的 Docker Compose 启动 MinIO 和 Elasticsearch，建议确认 `.env` 中以下值与容器端口和协议一致：

```env
MINIO_ENDPOINT=http://localhost:19000
MINIO_PUBLIC_URL=http://localhost:19000
ELASTICSEARCH_SCHEME=http
```

## 3. 启动基础设施

在项目根目录执行：

```bash
docker compose --env-file .env -f docs/docker-compose.yaml up -d
```

该命令会启动：

- MySQL 8
- Redis
- MinIO
- Elasticsearch 8.10.4
- Kafka

查看服务状态：

```bash
docker compose -f docs/docker-compose.yaml ps
```

常用访问地址：

- 后端默认端口：`http://localhost:8081`
- MinIO API：`http://localhost:19000`
- MinIO Console：`http://localhost:19001`
- Elasticsearch：`http://localhost:9200`
- Kafka：`127.0.0.1:9092`

## 4. 启动后端

确认基础设施已启动后，在项目根目录执行：

```bash
mvn spring-boot:run
```

也可以先打包再运行：

```bash
mvn -DskipTests package
java -jar target/KnowFlow-0.0.1-SNAPSHOT.jar
```

首次启动时，Hibernate 会根据实体自动更新 MySQL 表结构。Elasticsearch 索引会由后端初始化逻辑创建。

## 5. 启动前端

进入前端目录：

```bash
cd frontend
pnpm i
pnpm run dev
```

前端开发服务启动后，按终端输出的本地地址访问即可。默认开发代理会把 API 请求转发到后端。

## 6. 停止服务

停止基础设施：

```bash
docker compose -f docs/docker-compose.yaml down
```

如需同时删除容器数据卷：

```bash
docker compose -f docs/docker-compose.yaml down -v
```
