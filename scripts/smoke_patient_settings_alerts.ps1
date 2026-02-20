# Smoke test (ADB + uiautomator) for patient Settings + Alerts.
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts/smoke_patient_settings_alerts.ps1 -DeviceId emulator-5554

param(
  [string]$DeviceId = "emulator-5554",
  [string]$PackageId = "zdrowy.senior.io",
  [string]$Activity = ".MainActivity",
  [string]$Adb = "adb"
)

$ErrorActionPreference = "Stop"

function Require-Cmd([string]$name) {
  $cmd = Get-Command $name -ErrorAction SilentlyContinue
  if (-not $cmd) {
    throw "Missing '$name' in PATH. Install Android platform-tools and ensure adb is available."
  }
}

function Invoke-Adb([string[]]$AdbArgs) {
  & $Adb -s $DeviceId @AdbArgs | Out-Null
  if ($LASTEXITCODE -ne 0) {
    throw "adb failed: $Adb -s $DeviceId $($AdbArgs -join ' ')"
  }
}

function Get-AdbOut([string[]]$AdbArgs) {
  $out = & $Adb -s $DeviceId @AdbArgs
  if ($LASTEXITCODE -ne 0) {
    throw "adb failed: $Adb -s $DeviceId $($AdbArgs -join ' ')"
  }
  return $out
}

function Dump-Ui([string]$label) {
  $tempDir = Join-Path $env:TEMP "senior-mcp"
  New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
  $local = Join-Path $tempDir ("ui-{0}.xml" -f $label)
  Invoke-Adb @("shell", "uiautomator", "dump", "/sdcard/ui.xml")
  # Use plain adb pull (some wrappers return errors even when the file is pulled).
  & $Adb -s $DeviceId pull "/sdcard/ui.xml" $local | Out-Null
  if ($LASTEXITCODE -ne 0) {
    throw "adb pull failed"
  }
  return $local
}

function Load-Ui([string]$path) {
  [xml](Get-Content -LiteralPath $path -Raw -Encoding UTF8)
}

function Center-FromBounds([string]$bounds) {
  if ($bounds -match '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
    $x1 = [int]$matches[1]; $y1 = [int]$matches[2]
    $x2 = [int]$matches[3]; $y2 = [int]$matches[4]
    $cx = [int](($x1 + $x2) / 2)
    $cy = [int](($y1 + $y2) / 2)
    return @($cx, $cy)
  }
  throw "Bad bounds: $bounds"
}

function Find-NodeById([xml]$doc, [string]$id) {
  $xpath = "//node[@resource-id='$id']"
  return $doc.SelectSingleNode($xpath)
}

function Tap-ById([string]$id, [string]$label) {
  $xmlPath = Dump-Ui $label
  $doc = Load-Ui $xmlPath
  $node = Find-NodeById $doc $id
  if (-not $node) {
    throw "Missing node: $id (dump=$xmlPath)"
  }
  $xy = Center-FromBounds $node.GetAttribute("bounds")
  Invoke-Adb @("shell", "input", "tap", $xy[0], $xy[1])
}

function Wait-ForId([string]$id, [int]$timeoutSec, [string]$labelPrefix) {
  $deadline = (Get-Date).AddSeconds($timeoutSec)
  do {
    $xmlPath = Dump-Ui $labelPrefix
    $doc = Load-Ui $xmlPath
    if (Find-NodeById $doc $id) { return $xmlPath }
    Start-Sleep -Milliseconds 250
  } while ((Get-Date) -lt $deadline)
  throw "Timeout waiting for: $id"
}

function Swipe([int]$x1, [int]$y1, [int]$x2, [int]$y2, [int]$ms) {
  Invoke-Adb @("shell", "input", "swipe", $x1, $y1, $x2, $y2, $ms)
}

function Assert-True([bool]$cond, [string]$msg) {
  if (-not $cond) { throw $msg }
}

function Contains-Text([string]$xmlPath, [string]$text) {
  $raw = Get-Content -LiteralPath $xmlPath -Raw -Encoding UTF8
  return $raw.Contains($text)
}

function Assert-Text([string]$xmlPath, [string]$text) {
  Assert-True (Contains-Text $xmlPath $text) "Missing text: '$text' (dump=$xmlPath)"
}

$results = New-Object System.Collections.Generic.List[string]
function Pass([string]$name) { $results.Add("PASS $name") | Out-Null }
function Fail([string]$name, [string]$err) { $results.Add("FAIL $name - $err") | Out-Null }

Require-Cmd $Adb

try {
  Invoke-Adb @("shell", "pm", "clear", $PackageId)
  Invoke-Adb @("shell", "am", "start", "-n", "$PackageId/$Activity")

  Wait-ForId "${PackageId}:id/role_select_patient_card" 10 "01-role" | Out-Null
  Tap-ById "${PackageId}:id/role_select_patient_card" "01-role"
  Wait-ForId "${PackageId}:id/patient_login_continue" 10 "02-login" | Out-Null
  Tap-ById "${PackageId}:id/patient_login_continue" "02-login"
  Wait-ForId "${PackageId}:id/patient_sms_confirm" 10 "03-sms" | Out-Null
  Tap-ById "${PackageId}:id/patient_sms_confirm" "03-sms"
  Wait-ForId "${PackageId}:id/patient_home_action_settings" 10 "04-home" | Out-Null
  Tap-ById "${PackageId}:id/patient_home_action_settings" "04-home"
  Wait-ForId "${PackageId}:id/patient_settings_toolbar" 10 "05-settings" | Out-Null
  Pass "TC-NAV-SETTINGS"

  # Personal data dialog open/close.
  Tap-ById "${PackageId}:id/patient_settings_personal" "06-personal-open"
  $pXml = Wait-ForId "${PackageId}:id/patient_personal_save" 10 "06-personal"
  Assert-True (Contains-Text $pXml "Dodaj dane osobowe") "Unexpected personal dialog title"
  Tap-ById "${PackageId}:id/patient_personal_close" "06-personal-close"
  Wait-ForId "${PackageId}:id/patient_settings_toolbar" 10 "06-back-settings" | Out-Null
  Pass "TC-SET-PERSONAL-DIALOG"

  # Scroll to tiles row and open Alerts.
  $sXml = Dump-Ui "07-settings-pre-scroll"
  if (-not (Contains-Text $sXml "patient_settings_alerts")) {
    Swipe 540 2000 540 600 400
  }
  Wait-ForId "${PackageId}:id/patient_settings_alerts" 10 "07-settings" | Out-Null
  Tap-ById "${PackageId}:id/patient_settings_alerts" "07-settings"
  $aXml = Wait-ForId "${PackageId}:id/patient_alerts_overview_toolbar" 10 "08-alerts-overview"
  Assert-True (Contains-Text $aXml "patient_alerts_list") "Alerts list not present"

  # Regression: mojibake separator from earlier runs should be gone.
  $badSep = ([string][char]226) + ([string][char]8364) + ([string][char]728)  # "â€˘"
  Assert-True (-not (Contains-Text $aXml $badSep)) "Found mojibake separator 'â€˘' in alerts overview"
  Pass "TC-ALERT-OVERVIEW-SEPARATOR"

  Tap-ById "${PackageId}:id/alerts_overview_save" "09-alerts-config-open"
  $cXml = Wait-ForId "${PackageId}:id/patient_alerts_config_toolbar" 10 "09-alerts-config"
  Assert-Text $cXml "Konfiguracja wartosci krytycznych"
  Assert-Text $cXml "Wartosci krytyczne"
  Assert-Text $cXml "Krytycznie niska"
  Assert-Text $cXml "Krytycznie wysoka"
  Assert-Text $cXml "Zmiana (%)"
  Assert-Text $cXml "W ciagu pomiarow"

  # Scroll down to channels section to assert labels.
  Swipe 540 2000 540 600 400
  $c2 = Dump-Ui "10-alerts-config-scroll"
  Assert-Text $c2 "Kanaly powiadomien"
  Assert-Text $c2 "Wlacz wszystkie"
  Assert-Text $c2 "Wylacz wszystkie"
  Assert-Text $c2 "SMS"
  Assert-Text $c2 "Email"
  Assert-Text $c2 "W aplikacji"
  Pass "TC-ALERT-CONFIG-TEXTS"
} catch {
  $msg = $_.Exception.Message
  Fail "SMOKE" $msg
} finally {
  $results | ForEach-Object { Write-Output $_ }
  if ($results | Where-Object { $_ -like "FAIL*" }) { exit 1 } else { exit 0 }
}
