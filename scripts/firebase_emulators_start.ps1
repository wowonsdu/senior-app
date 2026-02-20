# Alias: emu-start
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

$jdkRoots = @(
  "C:\\Program Files\\Eclipse Adoptium",
  "C:\\Program Files\\Adoptium",
  "C:\\Program Files\\Java"
)

$jdk = $null
foreach ($root in $jdkRoots) {
  if (-not (Test-Path $root)) { continue }
  $cand = Get-ChildItem -Path $root -Directory -Filter "jdk-21*" |
    Sort-Object Name -Descending | Select-Object -First 1
  if ($cand) { $jdk = $cand; break }
}

if (-not $jdk) { throw "JDK 21 not found under: $($jdkRoots -join ', ')" }

$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME\\bin;$env:Path"

Write-Host "Repo: $repo"
Write-Host "JAVA_HOME: $env:JAVA_HOME"
Write-Host "Ports: auth=$authPort firestore=$firestorePort"

$logPath = Join-Path $repo ".firebase-emulators.log"
npx --yes firebase-tools emulators:start --only auth,firestore --project demo-senior-app | Tee-Object -FilePath $logPath
