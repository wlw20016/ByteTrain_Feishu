# VS Code Android Debug Workflow Evidence

Date: 2026-06-08

## Decision

The checked-in workflow supports VS Code/Trae Android breakpoint preparation through Android JDWP attach.

Chosen path:

- Use VS Code/Trae for editing, indexing, helper tasks, app build/run commands, debug attach, code completion, and diagnostic context.
- Use `.vscode/launch.json` configuration `Android: Attach ByteTrain App (JDWP)` for breakpoint debugging.
- Use `scripts/commands/ide-build.ps1 -Target android-jdwp-debug` as the debug preLaunch path.
- Keep Android Studio fallback for environments where the VS Code Java Debugger extension is unavailable or Android-specific tools such as Logcat/device selection are needed.

## Tooling Evaluation

Required VS Code extensions:

```text
redhat.java
vscjava.vscode-java-debug
```

These are now recommended in `.vscode/extensions.json`.

Commands checked:

```powershell
code --list-extensions
Get-Command adb
adb devices
```

Observed local VS Code extensions before adding the new recommendation:

```text
bazelbuild.vscode-bazel
fwcd.kotlin
redhat.java
rust-lang.rust-analyzer
vadimcn.vscode-lldb
zxh404.vscode-proto3
```

`adb` is available at `D:\Android\AndroidSDK\platform-tools\adb.exe`.

Connected Android device:

```text
000002f62f118b6e    device
```

## Checked-In Debug Workflow

VS Code launch configuration:

```text
.vscode/launch.json
Android: Attach ByteTrain App (JDWP)
type: java
request: attach
hostName: localhost
port: 8700
preLaunchTask: VS Code: prepare Android JDWP debug
```

PreLaunch task:

```text
.vscode/tasks.json
VS Code: prepare Android JDWP debug
scripts/commands/ide-build.ps1 -Target android-jdwp-debug
```

VS Code helper plugin commands:

```text
bytetrain.bazelHelper.prepareAndroidJdwpDebug
bytetrain.bazelHelper.startAndroidJdwpDebug
```

`startAndroidJdwpDebug` attaches directly to an already prepared `localhost:8700` JDWP session through the VS Code debug API. This avoids workspace-folder launch lookup problems when the app is already showing `Waiting for Debugger`.

The `android-jdwp-debug` target performs:

- `.\gradlew.bat :app:assembleDebug`
- `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- `adb shell am force-stop com.bytetrain.feishuclone`
- `adb shell am set-debug-app -w com.bytetrain.feishuclone`
- `adb shell am start -n com.bytetrain.feishuclone/.MainActivity`
- `adb shell pidof com.bytetrain.feishuclone`
- `adb forward tcp:8700 jdwp:<pid>`

## Validation Status

Automated validation covers the checked-in launch/task/plugin consistency:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-004.ps1
node --check tools\vscode-bazel-helper\src\extension.js
openspec.cmd validate add-vscode-android-debug-workflow --strict
```

Device-side preLaunch validation was run on 2026-06-08:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target android-jdwp-debug
```

Result:

- `:app:assembleDebug` completed successfully.
- `adb install -r -g` was rejected by the device with `INSTALL_FAILED_ABORTED: User rejected permissions`.
- The script detected that `com.bytetrain.feishuclone` was already installed and continued.
- The script force-stopped the app, set `am set-debug-app -w com.bytetrain.feishuclone`, started `com.bytetrain.feishuclone/.MainActivity`, resolved process id `15730`, and forwarded `tcp:8700` to `jdwp:15730`.
- Output ended with `VS Code Android JDWP debug is ready.`
- Cleanup after validation removed `tcp:8700`, cleared debug-app state, and force-stopped `com.bytetrain.feishuclone`.

Manual breakpoint validation steps:

- Install the recommended `vscjava.vscode-java-debug` extension if it is not already installed.
- Connect device `000002f62f118b6e` or another online Android device/emulator.
- Set a breakpoint in `app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt`.
- Start `Android: Attach ByteTrain App (JDWP)` in VS Code/Trae.
- Confirm the app waits for debugger, attach succeeds through `localhost:8700`, and the app code breakpoint is hit.

Residual risk:

- The repository can provide the JDWP launch/task/plugin workflow, but the actual breakpoint hit still depends on the local VS Code Java Debugger extension being installed and enabled.
