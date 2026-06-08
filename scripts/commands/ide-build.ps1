param(
    [ValidateSet("app", "run-app", "gradle-app", "android-jdwp-debug", "proto", "features", "rust", "query-app-deps")]
    [string] $Target = "app"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $root
$script:lastCommandOutput = @()

function Invoke-IdeCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Description,
        [Parameter(Mandatory = $true)]
        [string] $Executable,
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments,
        [string[]] $FailurePatterns = @()
    )

    Write-Host "IDE build helper: $Description"
    Write-Host "Working directory: $root"
    Write-Host "Command: $Executable $($Arguments -join ' ')"
    $matchedFailure = $false
    $script:lastCommandOutput = @()
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        & $Executable @Arguments 2>&1 | ForEach-Object {
            if ($_ -is [System.Management.Automation.ErrorRecord]) {
                $line = $_.Exception.Message
            } else {
                $line = $_.ToString()
            }
            foreach ($pattern in $FailurePatterns) {
                if ($line.Contains($pattern)) {
                    $matchedFailure = $true
                }
            }

            Write-Host $line
            $script:lastCommandOutput += $line
        }
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
    if ($matchedFailure) {
        throw "$Description reported a run prerequisite or launch failure."
    }
}

function Resolve-AdbPath {
    $adbCommand = Get-Command "adb" -ErrorAction SilentlyContinue
    if ($null -ne $adbCommand) {
        return $adbCommand.Source
    }

    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) {
        $androidHomeAdb = Join-Path $env:ANDROID_HOME "platform-tools\adb.exe"
        if (Test-Path $androidHomeAdb) {
            return $androidHomeAdb
        }
    }

    if (-not [string]::IsNullOrWhiteSpace($env:ANDROID_SDK_ROOT)) {
        $sdkRootAdb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
        if (Test-Path $sdkRootAdb) {
            return $sdkRootAdb
        }
    }

    throw "ADB was not found. Install Android SDK Platform-Tools or add adb.exe to PATH."
}

function Assert-OnlineAndroidDevice {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Adb
    )

    Write-Host "IDE build helper: ADB device preflight"
    Write-Host "Working directory: $root"
    Write-Host "Command: $Adb devices"
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $devicesOutput = & $Adb devices 2>&1
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    foreach ($line in $devicesOutput) {
        Write-Host $line.ToString()
    }

    if ($LASTEXITCODE -ne 0) {
        throw "ADB devices failed with exit code $LASTEXITCODE."
    }

    $onlineDevices = @($devicesOutput | Where-Object { $_.ToString() -match "\sdevice$" })
    if ($onlineDevices.Count -eq 0) {
        throw "No online Android device or emulator was found. Connect a device, enable USB debugging, accept the device authorization prompt, or start an emulator, then verify 'adb devices' shows a row ending in 'device'."
    }
}

function Test-AndroidPackageInstalled {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Adb,
        [Parameter(Mandatory = $true)]
        [string] $ApplicationId
    )

    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $packagePath = & $Adb shell pm path $ApplicationId 2>$null
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }

    return $LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace(($packagePath -join " "))
}

function Invoke-AndroidJdwpDebugPrepare {
    $applicationId = "com.bytetrain.feishuclone"
    $activity = "$applicationId/.MainActivity"
    $debugPort = "8700"
    $apkPath = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
    $adb = Resolve-AdbPath
    $usedExistingApk = $false

    Assert-OnlineAndroidDevice -Adb $adb

    try {
        Invoke-IdeCommand `
            -Description "Gradle Android debug build" `
            -Executable ".\gradlew.bat" `
            -Arguments @(":app:assembleDebug")
    } catch {
        $gradleOutput = $script:lastCommandOutput -join "`n"
        $canReuseExistingApk = (Test-Path $apkPath) -and
            $gradleOutput.Contains("Unable to delete directory") -and
            $gradleOutput.Contains("app-debug.apk")
        if (-not $canReuseExistingApk) {
            throw
        }

        Write-Host "Gradle could not replace app-debug.apk because it is locked; reusing the existing debug APK for JDWP validation."
        $usedExistingApk = $true
    }

    if (-not (Test-Path $apkPath)) {
        throw "Debug APK was not found at $apkPath."
    }

    $forceAdbInstall = $env:BYTETRAIN_FORCE_ADB_INSTALL -eq "1"
    $isPackageInstalled = Test-AndroidPackageInstalled -Adb $adb -ApplicationId $applicationId
    if ($isPackageInstalled -and -not $forceAdbInstall) {
        Write-Host "$applicationId is already installed; skipping ADB install. Set BYTETRAIN_FORCE_ADB_INSTALL=1 to force reinstall."
    } else {
        Write-Host "IDE build helper: ADB install debug APK"
        Write-Host "Working directory: $root"
        Write-Host "Command: $adb install -r -g $apkPath"
        $installJob = Start-Job -ScriptBlock {
            param([string] $AdbPath, [string] $Apk)
            $lines = @(& $AdbPath install -r -g $Apk 2>&1 | ForEach-Object { $_.ToString() })
            [pscustomobject]@{
                ExitCode = $LASTEXITCODE
                Lines = $lines
            }
        } -ArgumentList $adb, $apkPath
        $installCompleted = Wait-Job -Job $installJob -Timeout 45
        if ($null -eq $installCompleted) {
            Stop-Job -Job $installJob -ErrorAction SilentlyContinue
            Remove-Job -Job $installJob -Force -ErrorAction SilentlyContinue
            throw "ADB install timed out after 45 seconds. Enable or confirm USB install on the device, or preinstall $applicationId before running debug preparation."
        }

        $installResult = Receive-Job -Job $installJob
        Remove-Job -Job $installJob -Force -ErrorAction SilentlyContinue
        foreach ($line in @($installResult.Lines)) {
            Write-Host $line
        }
        if ($installResult.ExitCode -ne 0) {
            Write-Host "ADB install failed; checking whether $applicationId is already installed."
            if (-not (Test-AndroidPackageInstalled -Adb $adb -ApplicationId $applicationId)) {
                throw "ADB install failed and $applicationId is not installed on the selected device."
            }
            Write-Host "$applicationId is already installed; continuing with JDWP attach preparation."
        }
    }

    Invoke-IdeCommand `
        -Description "ADB force-stop app before debug attach" `
        -Executable $adb `
        -Arguments @("shell", "am", "force-stop", $applicationId)

    Invoke-IdeCommand `
        -Description "ADB set app to wait for debugger" `
        -Executable $adb `
        -Arguments @("shell", "am", "set-debug-app", "-w", $applicationId)

    Invoke-IdeCommand `
        -Description "ADB start debug activity" `
        -Executable $adb `
        -Arguments @("shell", "am", "start", "-n", $activity)

    $appPid = $null
    for ($attempt = 0; $attempt -lt 40; $attempt++) {
        $pidOutput = & $adb shell pidof $applicationId 2>$null
        if ($LASTEXITCODE -eq 0 -and -not [string]::IsNullOrWhiteSpace(($pidOutput -join " "))) {
            $appPid = (($pidOutput -join " ").Trim() -split "\s+")[0]
            break
        }
        Start-Sleep -Milliseconds 500
    }

    if ([string]::IsNullOrWhiteSpace($appPid)) {
        throw "Could not resolve JDWP process id for $applicationId."
    }

    Invoke-IdeCommand `
        -Description "ADB forward Android JDWP to localhost:$debugPort" `
        -Executable $adb `
        -Arguments @("forward", "tcp:$debugPort", "jdwp:$appPid")

    Write-Host "VS Code Android JDWP debug is ready."
    Write-Host "Application: $applicationId"
    Write-Host "PID: $appPid"
    Write-Host "Attach host: localhost"
    Write-Host "Attach port: $debugPort"
    Write-Host "Launch configuration: Android: Attach ByteTrain App (JDWP)"
}

switch ($Target) {
    "app" {
        Invoke-IdeCommand `
            -Description "Bazel Android app build" `
            -Executable "bazel" `
            -Arguments @("--batch", "build", "//app:app", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    }
    "run-app" {
        Invoke-IdeCommand `
            -Description "Bazel Android app run" `
            -Executable "bazel" `
            -Arguments @("--batch", "run", "--curses=no", "--show_progress_rate_limit=60", "//app:run_app") `
            -FailurePatterns @(
                "[run-app] Bazel-built APK was not found",
                "[run-app] ADB was not found",
                "[run-app] No online Android device or emulator was found",
                "[run-app] ADB install failed",
                "[run-app] Activity launch failed"
            )
    }
    "gradle-app" {
        Invoke-IdeCommand `
            -Description "Gradle Android debug build" `
            -Executable ".\gradlew.bat" `
            -Arguments @(":app:assembleDebug")
    }
    "android-jdwp-debug" {
        Invoke-AndroidJdwpDebugPrepare
    }
    "proto" {
        Invoke-IdeCommand `
            -Description "Bazel proto build" `
            -Executable "bazel" `
            -Arguments @("build", "//proto:...", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    }
    "features" {
        Invoke-IdeCommand `
            -Description "Bazel shared and feature Kotlin build" `
            -Executable "bazel" `
            -Arguments @(
                "--batch",
                "build",
                "//shared/list:list",
                "//shared/navigation:navigation",
                "//shared/ui:ui_models",
                "//features/message:domain",
                "//features/message:data",
                "//features/message:mapper",
                "//features/message:ui",
                "//features/message:message",
                "//features/mail:domain",
                "//features/mail:data",
                "//features/mail:mapper",
                "//features/mail:ui",
                "//features/mail:mail",
                "--curses=no",
                "--show_progress_rate_limit=60",
                "--jobs=4"
            )
    }
    "rust" {
        Invoke-IdeCommand `
            -Description "Rust SDK tests" `
            -Executable "cargo" `
            -Arguments @("test", "--manifest-path", "sdk/rust/Cargo.toml")
    }
    "query-app-deps" {
        Invoke-IdeCommand `
            -Description "Bazel app dependency query" `
            -Executable "bazel" `
            -Arguments @("--batch", "query", "--notool_deps", "--noimplicit_deps", "--output=label_kind", "--curses=no", "deps(//app:app, 2)")
    }
}

