@echo off
chcp 65001 >nul
echo ==========================================
echo novel 项目开发环境启动脚本
echo ==========================================

REM 请确保已创建 .env 文件并填入正确配置
if not exist .env (
    echo [错误] 找不到 .env 文件，请先复制 .env.template 为 .env 并填写配置
    pause
    exit /b 1
)

REM 读取环境变量（简单方式：通过 JVM 系统属性注入）
REM 实际使用时建议用 spring-boot:run 的 -D 参数

echo [提示] 正在启动 novel 后端服务...
echo [提示] 默认端口 8888，Swagger 文档 http://localhost:8888/swagger-ui.html

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"

pause
