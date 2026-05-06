$KONG_ADMIN = "http://localhost:8001"

Write-Host "=== Kong Gateway Initialization ===" -ForegroundColor Cyan
Write-Host "Kong Admin API: $KONG_ADMIN" -ForegroundColor Yellow

$services = @(
    @{ Name = "erp-iam-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-iam-route"; Paths = @("/iam/api") },
    @{ Name = "erp-pdm-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-pdm-route"; Paths = @("/pdm/api") },
    @{ Name = "erp-som-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-som-route"; Paths = @("/som/api") },
    @{ Name = "erp-ads-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-ads-route"; Paths = @("/ads/api") },
    @{ Name = "erp-oms-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-oms-route"; Paths = @("/oms/api") },
    @{ Name = "erp-scm-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-scm-route"; Paths = @("/scm/api") },
    @{ Name = "erp-wms-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-wms-route"; Paths = @("/wms/api") },
    @{ Name = "erp-fba-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-fba-route"; Paths = @("/fba/api") },
    @{ Name = "erp-tms-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-tms-route"; Paths = @("/tms/api") },
    @{ Name = "erp-crm-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-crm-route"; Paths = @("/crm/api") },
    @{ Name = "erp-fms-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-fms-route"; Paths = @("/fms/api") },
    @{ Name = "erp-bi-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-bi-route"; Paths = @("/bi/api") },
    @{ Name = "erp-sys-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-sys-route"; Paths = @("/sys/api") },
    @{ Name = "erp-dashboard-service"; Url = "http://erp-gateway:8080"; RouteName = "erp-dashboard-route"; Paths = @("/dashboard/api") }
)

foreach ($svc in $services) {
    Write-Host "`nCreating service: $($svc.Name)" -ForegroundColor Green
    $svcBody = @{
        name = $svc.Name
        url = $svc.Url
    } | ConvertTo-Json

    try {
        $existing = Invoke-RestMethod -Uri "$KONG_ADMIN/services/$($svc.Name)" -Method GET -ErrorAction SilentlyContinue
        Write-Host "  Service already exists, updating..." -ForegroundColor Yellow
        Invoke-RestMethod -Uri "$KONG_ADMIN/services/$($svc.Name)" -Method PATCH -Body $svcBody -ContentType "application/json" | Out-Null
    } catch {
        Invoke-RestMethod -Uri "$KONG_ADMIN/services" -Method POST -Body $svcBody -ContentType "application/json" | Out-Null
    }

    Write-Host "  Creating route: $($svc.RouteName)" -ForegroundColor Green
    $routeBody = @{
        name = $svc.RouteName
        service = @{ name = $svc.Name }
        paths = $svc.Paths
        strip_path = $false
    } | ConvertTo-Json

    try {
        $existingRoute = Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$($svc.RouteName)" -Method GET -ErrorAction SilentlyContinue
        Write-Host "  Route already exists, updating..." -ForegroundColor Yellow
        Invoke-RestMethod -Uri "$KONG_ADMIN/routes/$($svc.RouteName)" -Method PATCH -Body $routeBody -ContentType "application/json" | Out-Null
    } catch {
        Invoke-RestMethod -Uri "$KONG_ADMIN/routes" -Method POST -Body $routeBody -ContentType "application/json" | Out-Null
    }
}

Write-Host "`n=== Configuring Global Rate Limiting ===" -ForegroundColor Cyan
$rateLimitBody = @{
    name = "rate-limiting"
    config = @{
        minute = 100
        hour = 1000
        policy = "local"
    }
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KONG_ADMIN/plugins" -Method POST -Body $rateLimitBody -ContentType "application/json" | Out-Null
    Write-Host "  Global rate limiting configured: 100/min, 1000/hour" -ForegroundColor Green
} catch {
    Write-Host "  Rate limiting plugin may already exist" -ForegroundColor Yellow
}

Write-Host "`n=== Kong Initialization Complete ===" -ForegroundColor Cyan
Write-Host "External entry: http://localhost:8000" -ForegroundColor Yellow
Write-Host "Admin API: http://localhost:8001" -ForegroundColor Yellow
