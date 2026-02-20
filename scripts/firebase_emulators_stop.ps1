# Alias: emu-stop
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repo = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repo

if (-not (Test-Path ".\\firebase.json")) {
  throw "firebase.json not found in repo root: $repo"
}

$json = Get-Content ".\\firebase.json" -Raw | ConvertFrom-Json
$authPort = if ($json.emulators.auth.port) { [int]$json.emulators.auth.port } else { 9099 }
$firestorePort = if ($json.emulators.firestore.port) { [int]$json.emulators.firestore.port } else { 8080 }

$ports = @($authPort, $firestorePort)
$pids = Get-NetTCPConnection -LocalPort $ports -State Listen -ErrorAction SilentlyContinue |
  Select-Object -ExpandProperty OwningProcess -Unique

if ($pids) {
  $pids | ForEach-Object { Stop-Process -Id $_ -Force }
  Write-Host "Stopped emulator processes on ports: $($ports -join ', ')"
} else {
  Write-Host "No emulator processes found on ports: $($ports -join ', ')"
}
