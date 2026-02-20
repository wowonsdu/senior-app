# Smoke test (ADB + uiautomator) for Firebase Phone Auth flows (Patient + Caregiver).
#
# Prereq:
# - Firebase Auth Emulator Suite running on host (default port 9099)
# - App DEBUG uses emulator + disables app verification for testing
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts/smoke_auth_phone.ps1 -DeviceId emulator-5554

param(
  [string]$DeviceId = "emulator-5554",
  [string]$PackageId = "zdrowy.senior.io",
  [string]$Activity = ".MainActivity",
  [ValidateSet("both","patient","caregiver")]
  [string]$Flow = "both",
  [string]$PhoneE164Patient = "+48500100200",
  [string]$PhoneE164Caregiver = "+48500100201",
  [string]$SmsCode = "123456",
  [string]$FirebaseProjectId = "demo-senior-app",
  [string]$AuthEmulatorHost = "127.0.0.1",
  [int]$AuthEmulatorPort = 9098,
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

function Dump-Ui([string]$label) {
  $tempDir = Join-Path $env:TEMP "senior-mcp"
  New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
  $local = Join-Path $tempDir ("ui-{0}.xml" -f $label)
  Invoke-Adb @("shell", "uiautomator", "dump", "/sdcard/ui.xml")
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

function Type-Text([string]$text) {
  # adb input text: no spaces in our inputs; + and digits are fine.
  Invoke-Adb @("shell", "input", "text", $text)
}

function Get-VerificationCode([string]$phone, [int]$timeoutSec) {
  $deadline = (Get-Date).AddSeconds($timeoutSec)
  $url = "http://{0}:{1}/emulator/v1/projects/{2}/verificationCodes" -f $AuthEmulatorHost, $AuthEmulatorPort, $FirebaseProjectId

  do {
    try {
      $resp = Invoke-RestMethod -Method Get -Uri $url -TimeoutSec 2
      $codes = @()
      if ($resp -and $resp.verificationCodes) {
        $codes = $resp.verificationCodes
      } elseif ($resp) {
        $codes = @($resp)
      }

      $wantDigits = ($phone -replace '\D', '')
      foreach ($c in $codes) {
        $gotDigits = ([string]$c.phoneNumber -replace '\D', '')
        if (($c.phoneNumber -eq $phone -or $gotDigits -eq $wantDigits) -and $c.code) {
          return [string]$c.code
        }
      }
    } catch {
      # ignore while emulator is starting / code not ready yet
    }
    Start-Sleep -Milliseconds 250
  } while ((Get-Date) -lt $deadline)

  return $SmsCode
}

$results = New-Object System.Collections.Generic.List[string]
function Pass([string]$name) { $results.Add("PASS $name") | Out-Null }
function Fail([string]$name, [string]$err) { $results.Add("FAIL $name - $err") | Out-Null }

Require-Cmd $Adb

try {
  Invoke-Adb @("shell", "pm", "clear", $PackageId)
  Invoke-Adb @("shell", "am", "start", "-n", "$PackageId/$Activity")

  Wait-ForId "${PackageId}:id/role_select_patient_card" 15 "01-role" | Out-Null

  if ($Flow -eq "both" -or $Flow -eq "patient") {
    # Patient flow
    Tap-ById "${PackageId}:id/role_select_patient_card" "01-role-patient"
    Wait-ForId "${PackageId}:id/patient_login_continue" 15 "02-patient-login" | Out-Null
    Tap-ById "${PackageId}:id/patient_login_phone" "02-patient-phone"
    Type-Text $PhoneE164Patient
    Tap-ById "${PackageId}:id/patient_login_continue" "02-patient-continue"
    Wait-ForId "${PackageId}:id/patient_sms_confirm" 15 "03-patient-sms" | Out-Null
    $code1 = Get-VerificationCode $PhoneE164Patient 20
    Tap-ById "${PackageId}:id/patient_sms_code" "03-patient-code"
    Type-Text $code1
    Tap-ById "${PackageId}:id/patient_sms_confirm" "03-patient-confirm"
    Wait-ForId "${PackageId}:id/patient_home_toolbar" 20 "04-patient-home" | Out-Null
    Tap-ById "${PackageId}:id/patient_home_logout" "04-patient-logout"
    Wait-ForId "${PackageId}:id/role_select_patient_card" 15 "05-role-after-patient-logout" | Out-Null
    Pass "TC-AUTH-PATIENT"
  }

  if ($Flow -eq "both" -or $Flow -eq "caregiver") {
    # Caregiver flow
    Tap-ById "${PackageId}:id/role_select_caregiver_card" "06-role-caregiver"
    Wait-ForId "${PackageId}:id/caregiver_login_continue" 15 "07-caregiver-login" | Out-Null
    Tap-ById "${PackageId}:id/caregiver_login_phone" "07-caregiver-phone"
    Type-Text $PhoneE164Caregiver
    Tap-ById "${PackageId}:id/caregiver_login_continue" "07-caregiver-continue"
    Wait-ForId "${PackageId}:id/caregiver_sms_confirm" 15 "08-caregiver-sms" | Out-Null
    $code2 = Get-VerificationCode $PhoneE164Caregiver 20
    if ($code2 -eq $SmsCode) {
      Write-Output "WARN using fallback SMS code '$SmsCode' (no code found via emulator endpoint)"
      try {
        $url = "http://{0}:{1}/emulator/v1/projects/{2}/verificationCodes" -f $AuthEmulatorHost, $AuthEmulatorPort, $FirebaseProjectId
        $raw = Invoke-RestMethod -Method Get -Uri $url -TimeoutSec 2
        ($raw | ConvertTo-Json -Depth 6) | Write-Output
      } catch {
        Write-Output "WARN unable to fetch verificationCodes for debug"
      }
    }
    Tap-ById "${PackageId}:id/caregiver_sms_code" "08-caregiver-code"
    Type-Text $code2
    Tap-ById "${PackageId}:id/caregiver_sms_confirm" "08-caregiver-confirm"
    Wait-ForId "${PackageId}:id/caregiver_home_logout" 20 "09-caregiver-home" | Out-Null
    Tap-ById "${PackageId}:id/caregiver_home_logout" "09-caregiver-logout"
    Wait-ForId "${PackageId}:id/role_select_patient_card" 15 "10-role-after-caregiver-logout" | Out-Null
    Pass "TC-AUTH-CAREGIVER"
  }
} catch {
  $msg = $_.Exception.Message
  Fail "SMOKE" $msg
} finally {
  $results | ForEach-Object { Write-Output $_ }
  if ($results | Where-Object { $_ -like "FAIL*" }) { exit 1 } else { exit 0 }
}
