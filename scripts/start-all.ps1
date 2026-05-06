$ErrorActionPreference = "Stop"
$Host.UI.RawUI.WindowTitle = "ERP Infrastructure Start"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Cross-Border ERP - Infrastructure Start  " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $projectRoot "docker-compose.yml"
$javaHome = ${env:ERP_JAVA_HOME}
if ([string]::IsNullOrWhiteSpace($javaHome)) {
    $javaHome = "D:\erp\jdk17\jdk-17.0.18+8"
}
$mavenCmd = ${env:ERP_MAVEN_CMD}
if ([string]::IsNullOrWhiteSpace($mavenCmd)) {
    $mavenCmd = "D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd"
}

Write-Host "[1/4] Preparing infrastructure..." -ForegroundColor Yellow
$dockerDaemonRunning = $false
try {
    $dockerInfo = docker info 2>&1 | Out-String
    if ($dockerInfo -match "Server Version:") {
        $dockerDaemonRunning = $true
    }
} catch {
    $dockerDaemonRunning = $false
}
if ($dockerDaemonRunning) {
    if (-not (Test-Path $composeFile)) {
        Write-Host "docker-compose.yml not found at $composeFile" -ForegroundColor Red
        exit 1
    }
    Push-Location $projectRoot
    docker compose -f $composeFile up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Docker Compose failed. Please check $composeFile" -ForegroundColor Red
        exit 1
    }
    Pop-Location
    Write-Host "  Docker services started with Docker Compose." -ForegroundColor Green
} else {
    Write-Host "  Docker daemon not running, using local Windows services/processes mode." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[2/4] Waiting for services to be healthy..." -ForegroundColor Yellow
$services = @(
    @{ Name = "PostgreSQL"; Port = 5432; MaxWait = 30 },
    @{ Name = "Redis"; Port = 6379; MaxWait = 15 },
    @{ Name = "Nacos"; Port = 8848; MaxWait = 60 },
    @{ Name = "Kafka"; Port = 9092; MaxWait = 45 },
    @{ Name = "MinIO"; Port = 9000; MaxWait = 20 },
    @{ Name = "Elasticsearch"; Port = 9200; MaxWait = 60 },
    @{ Name = "Kong"; Port = 8000; MaxWait = 60 }
)

foreach ($svc in $services) {
    $waited = 0
    Write-Host "  Waiting for $($svc.Name) (port $($svc.Port))..." -NoNewline
    while ($waited -lt $svc.MaxWait) {
        $conn = Get-NetTCPConnection -LocalPort $svc.Port -ErrorAction SilentlyContinue
        if ($conn) {
            Write-Host " OK (${waited}s)" -ForegroundColor Green
            break
        }
        Start-Sleep -Seconds 1
        $waited++
    }
    if ($waited -ge $svc.MaxWait) {
        Write-Host " TIMEOUT" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "[3/4] Initializing Kong routes..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
$kongReady = $false
$retries = 0
while (-not $kongReady -and $retries -lt 10) {
    try {
        $status = Invoke-RestMethod -Uri "http://localhost:8001/status" -TimeoutSec 5 -ErrorAction Stop
        $kongReady = $true
        Write-Host "  Kong is ready." -ForegroundColor Green
    } catch {
        $retries++
        Write-Host "  Waiting for Kong... ($retries/10)" -ForegroundColor Yellow
        Start-Sleep -Seconds 3
    }
}

if ($kongReady) {
    & "$projectRoot\scripts\init-kong.ps1"
} else {
    Write-Host "  Kong not ready, skipping route init." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[4/4] Starting ERP application..." -ForegroundColor Yellow
if (-not (Test-Path $javaHome)) {
    Write-Host "Java 17 not found at $javaHome" -ForegroundColor Red
    exit 1
}
if (-not (Test-Path $mavenCmd)) {
    Write-Host "Maven not found at $mavenCmd" -ForegroundColor Red
    exit 1
}
$env:JAVA_HOME = $javaHome
$env:Path = "$($javaHome)\bin;D:\erp\maven\apache-maven-3.9.9\bin;" + $env:Path
Push-Location $projectRoot
& $mavenCmd spring-boot:run -pl erp-app -am
Pop-Location

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  ERP System Started!" -ForegroundColor Green
Write-Host "  Kong External:  http://localhost:8000" -ForegroundColor Yellow
Write-Host "  Kong Admin:     http://localhost:8001" -ForegroundColor Yellow
Write-Host "  ERP App:        http://localhost:8080" -ForegroundColor Yellow
Write-Host "  Nacos:          http://localhost:8848/nacos" -ForegroundColor Yellow
Write-Host "  MinIO Console:  http://localhost:9001" -ForegroundColor Yellow
Write-Host "  Sentinel:       http://localhost:8858" -ForegroundColor Yellow
Write-Host "============================================" -ForegroundColor Cyan
