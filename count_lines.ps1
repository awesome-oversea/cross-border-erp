$f9 = Get-ChildItem 'd:\Project\erp' -Filter '*V9.md' | Select-Object -First 1
$f10 = Get-ChildItem 'd:\Project\erp' -Filter '*V10.md' | Select-Object -First 1
$c9 = (Get-Content $f9.FullName).Count
$c10 = (Get-Content $f10.FullName).Count
Write-Host "V9: $c9 lines"
Write-Host "V10: $c10 lines"
