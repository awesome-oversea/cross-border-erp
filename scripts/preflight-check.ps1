$ErrorActionPreference = "Continue"
$Host.UI.RawUI.WindowTitle = "ERP Environment Preflight Check"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Cross-Border ERP - Environment Preflight  " -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$allPassed = $true
$projectRoot = Split-Path -Parent $PSScriptRoot
$projectJavaHome = ${env:ERP_JAVA_HOME}
if ([string]::IsNullOrWhiteSpace($projectJavaHome)) {
    $projectJavaHome = "D:\erp\jdk17\jdk-17.0.18+8"
}
$projectJava = Join-Path $projectJavaHome "bin\java.exe"
$projectMaven = ${env:ERP_MAVEN_CMD}
if ([string]::IsNullOrWhiteSpace($projectMaven)) {
    $projectMaven = "D:\erp\maven\apache-maven-3.9.9\bin\mvn.cmd"
}

function Test-Dependency {
    param(
        [string]$Name,
        [string]$Command,
        [string[]]$Arguments = @(),
        [scriptblock]$VersionExtractor,
        [scriptblock]$Validator
    )
    Write-Host "Checking $Name..." -ForegroundColor Yellow -NoNewline
    try {
        $output = & $Command @Arguments 2>&1 | Out-String
        $version = if ($VersionExtractor) { & $VersionExtractor $output } else { "unknown" }
        $isValid = if ($Validator) { & $Validator $version $output } else { $true }
        if ($isValid) {
            Write-Host " OK (v$version)" -ForegroundColor Green
        } else {
            Write-Host " INVALID (v$version)" -ForegroundColor Red
            $script:allPassed = $false
        }
    } catch {
        Write-Host " NOT FOUND" -ForegroundColor Red
        $script:allPassed = $false
    }
}

Test-Dependency -Name "Java 17" -Command $projectJava -Arguments @("-version") -VersionExtractor {
    param($out); if ($out -match 'version "([^"]+)"') { $matches[1] } elseif ($out -match '(\d+\.\d+\.\d+)') { $matches[1] } else { "unknown" }
} -Validator {
    param($version, $out); $version -match '^17(\.|$)'
}

Test-Dependency -Name "Maven" -Command $projectMaven -Arguments @("-version") -VersionExtractor {
    param($out); if ($out -match 'Apache Maven (\S+)') { $matches[1] } else { "unknown" }
} -Validator {
    param($version, $out); $version -eq "3.9.9"
}

Test-Dependency -Name "Docker" -Command "docker" -Arguments @("--version") -VersionExtractor {
    param($out); if ($out -match 'Docker version (\S+)') { $matches[1].TrimEnd(',') } else { "unknown" }
}

Test-Dependency -Name "Docker Compose" -Command "docker" -Arguments @("compose", "version") -VersionExtractor {
    param($out); if ($out -match 'Docker Compose version v?(\S+)') { $matches[1] } else { "unknown" }
}

Test-Dependency -Name "Git" -Command "git" -Arguments @("--version") -VersionExtractor {
    param($out); if ($out -match 'git version (\S+)') { $matches[1] } else { "unknown" }
}

Write-Host ""
Write-Host "Checking Docker daemon..." -ForegroundColor Yellow -NoNewline
$dockerDaemonRunning = $false
try {
    $dockerInfo = docker info 2>&1 | Out-String
    if ($dockerInfo -match "Server Version:") {
        $dockerDaemonRunning = $true
        Write-Host " Running" -ForegroundColor Green
    } else {
        Write-Host " NOT RUNNING (optional for local Windows services mode)" -ForegroundColor Yellow
    }
} catch {
    Write-Host " NOT RUNNING (optional for local Windows services mode)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Checking required ports..." -ForegroundColor Yellow
$ports = @(
    @{ Port = 5432; Service = "PostgreSQL" },
    @{ Port = 6379; Service = "Redis" },
    @{ Port = 9092; Service = "Kafka" },
    @{ Port = 9000; Service = "MinIO" },
    @{ Port = 9200; Service = "Elasticsearch" },
    @{ Port = 8848; Service = "Nacos" },
    @{ Port = 8000; Service = "Kong Proxy" },
    @{ Port = 8080; Service = "ERP App" }
)

foreach ($p in $ports) {
    $inUse = Get-NetTCPConnection -LocalPort $p.Port -ErrorAction SilentlyContinue
    if ($inUse) {
        Write-Host "  Port $($p.Port) ($($p.Service)): IN USE" -ForegroundColor Yellow
    } else {
        Write-Host "  Port $($p.Port) ($($p.Service)): Available" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Checking JAVA_HOME..." -ForegroundColor Yellow -NoNewline
if (Test-Path $projectJavaHome) {
    $env:JAVA_HOME = $projectJavaHome
    Write-Host " $projectJavaHome" -ForegroundColor Green
} else {
    Write-Host " NOT FOUND at $projectJavaHome" -ForegroundColor Red
    $allPassed = $false
}

Write-Host ""
Write-Host "Checking project structure..." -ForegroundColor Yellow
$requiredDirs = @(
    "erp-common", "erp-gateway", "erp-app",
    "erp-domain-oms", "erp-domain-wms", "erp-domain-pdm",
    "erp-domain-scm", "erp-domain-fms", "erp-domain-ads"
)

foreach ($dir in $requiredDirs) {
    $path = Join-Path $projectRoot $dir
    if (Test-Path $path) {
        Write-Host "  $dir : OK" -ForegroundColor Green
    } else {
        Write-Host "  $dir : MISSING" -ForegroundColor Red
        $allPassed = $false
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
if ($allPassed) {
    Write-Host "  ALL CHECKS PASSED - Ready to start!" -ForegroundColor Green
} else {
    Write-Host "  SOME CHECKS FAILED - Please fix before starting" -ForegroundColor Red
}
Write-Host "============================================" -ForegroundColor Cyan
