# 当前数据库结构的接口测试

`CurrentSchemaApiTest` 对已启动的服务发真实 HTTP 请求，覆盖 28 个业务路由和
`/actuator/health`。测试使用项目 `application.yaml` 配置的数据库执行持久化断言；
启动前先检查 `scores.student_course_id`、`student_course.term/status`，并验证
HTTP 服务与测试连接的是同一数据库。测试记录使用唯一标记，结束时按外键顺序清理。

在项目根目录运行：

```powershell
mvn "-Dstusys.api.integration=true" "-Dtest=CurrentSchemaApiTest" test
```

如服务不在默认地址 `http://127.0.0.1:8080`：

```powershell
mvn "-Dstusys.api.integration=true" "-Dstusys.api.base-url=http://127.0.0.1:8081" "-Dtest=CurrentSchemaApiTest" test
```

用例覆盖增删改查、分页筛选、必填参数校验、选课重复提交与状态转换。
断言依据当前控制器、服务实现和数据库结构；不包含旧结构的成绩字段或旧的
“退选后重新选课”预期。测试默认不运行，避免普通 `mvn test` 操作已启动服务的数据。

测试结果见 `target/surefire-reports/com.furinafans.stusys.CurrentSchemaApiTest.txt`。
新测试文件位于 `src/test`，可随仓库一起保存。
