"use strict";

const vscode = require("vscode");
const { spawn } = require("child_process");
const path = require("path");

const COMMANDS = [
  {
    id: "bytetrain.bazelHelper.buildApp",
    label: "Bazel: Build App",
    target: "app",
  },
  {
    id: "bytetrain.bazelHelper.runApp",
    label: "Bazel: Run App",
    target: "run-app",
  },
  {
    id: "bytetrain.bazelHelper.assembleDebug",
    label: "Gradle: Assemble Debug",
    target: "gradle-app",
  },
  {
    id: "bytetrain.bazelHelper.prepareDebug",
    label: "Android Studio: Prepare Debug",
    target: "gradle-app",
  },
  {
    id: "bytetrain.bazelHelper.prepareAndroidJdwpDebug",
    label: "VS Code: Prepare Android JDWP Debug",
    target: "android-jdwp-debug",
  },
  {
    id: "bytetrain.bazelHelper.buildProto",
    label: "Bazel: Build Proto",
    target: "proto",
  },
  {
    id: "bytetrain.bazelHelper.buildFeatures",
    label: "Bazel: Build Features",
    target: "features",
  },
  {
    id: "bytetrain.bazelHelper.testRustSdk",
    label: "Rust: Test SDK",
    target: "rust",
  },
  {
    id: "bytetrain.bazelHelper.queryAppDeps",
    label: "Bazel: Query App Deps",
    target: "query-app-deps",
  },
];

const DOCUMENT_COMMANDS = [
  {
    id: "bytetrain.bazelHelper.openBuildCommands",
    relativePath: path.join("docs", "ai-context", "build-system", "build-commands.md"),
  },
  {
    id: "bytetrain.bazelHelper.openModuleBoundaries",
    relativePath: path.join("docs", "project", "module-boundaries.md"),
  },
  {
    id: "bytetrain.bazelHelper.openCommonBuildErrors",
    relativePath: path.join("docs", "ai-context", "build-system", "common-build-errors.md"),
  },
];

let lastRun = null;

function getWorkspaceRoot() {
  const folder = vscode.workspace.workspaceFolders && vscode.workspace.workspaceFolders[0];
  if (!folder) {
    throw new Error("Open the ByteTrain workspace before running IDE build commands.");
  }

  return folder.uri.fsPath;
}

function activate(context) {
  const output = vscode.window.createOutputChannel("ByteTrain Bazel Helper");
  context.subscriptions.push(output);

  for (const command of COMMANDS) {
    context.subscriptions.push(
      vscode.commands.registerCommand(command.id, () => runTarget(command, output))
    );
  }

  context.subscriptions.push(
    vscode.commands.registerCommand("bytetrain.bazelHelper.copyDiagnosticContext", () =>
      copyDiagnosticContext(output)
    )
  );
  context.subscriptions.push(
    vscode.commands.registerCommand("bytetrain.bazelHelper.startAndroidJdwpDebug", () =>
      startAndroidJdwpDebug()
    )
  );

  for (const command of DOCUMENT_COMMANDS) {
    context.subscriptions.push(
      vscode.commands.registerCommand(command.id, () => openWorkspaceDocument(command.relativePath))
    );
  }
}

function runTarget(command, output) {
  const workspaceRoot = getWorkspaceRoot();
  const scriptPath = path.join(workspaceRoot, "scripts", "commands", "ide-build.ps1");
  const args = [
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-File",
    scriptPath,
    "-Target",
    command.target,
  ];
  const commandLine = `powershell ${args.join(" ")}`;
  const run = {
    label: command.label,
    target: command.target,
    workspaceRoot,
    commandLine,
    startedAt: new Date().toISOString(),
    exitCode: null,
    output: [],
  };
  lastRun = run;

  output.show(true);
  output.appendLine("");
  output.appendLine(`[${new Date().toISOString()}] ${command.label}`);
  output.appendLine(`Working directory: ${workspaceRoot}`);
  output.appendLine(`Command: ${commandLine}`);

  const child = spawn("powershell", args, {
    cwd: workspaceRoot,
    shell: false,
  });

  child.stdout.on("data", (data) => appendProcessOutput(output, run, data.toString()));
  child.stderr.on("data", (data) => appendProcessOutput(output, run, data.toString()));
  child.on("error", (error) => {
    appendProcessOutput(output, run, `Failed to start command: ${error.message}\n`);
    vscode.window.showErrorMessage(`${command.label} failed to start. See ByteTrain Bazel Helper output.`);
  });
  child.on("close", (code) => {
    run.exitCode = code;
    output.appendLine(`Exit code: ${code}`);
    if (code === 0) {
      vscode.window.setStatusBarMessage(`${command.label} finished`, 5000);
      return;
    }

    vscode.window.showErrorMessage(`${command.label} failed with exit code ${code}. See ByteTrain Bazel Helper output.`);
  });
}

function appendProcessOutput(output, run, text) {
  output.append(text);
  for (const line of text.split(/\r?\n/)) {
    if (line.length === 0) {
      continue;
    }

    run.output.push(line);
  }

  if (run.output.length > 80) {
    run.output.splice(0, run.output.length - 80);
  }
}

async function copyDiagnosticContext(output) {
  const contextText = formatDiagnosticContext();
  await vscode.env.clipboard.writeText(contextText);
  output.show(true);
  output.appendLine("");
  output.appendLine(`[${new Date().toISOString()}] Copied diagnostic context`);
  output.appendLine(contextText);
  vscode.window.setStatusBarMessage("ByteTrain diagnostic context copied", 5000);
}

function formatDiagnosticContext() {
  if (!lastRun) {
    return [
      "ByteTrain Bazel Helper diagnostic context",
      "No helper command has run in this extension session.",
    ].join("\n");
  }

  return [
    "ByteTrain Bazel Helper diagnostic context",
    `Started: ${lastRun.startedAt}`,
    `Label: ${lastRun.label}`,
    `Target: ${lastRun.target}`,
    `Working directory: ${lastRun.workspaceRoot}`,
    `Command: ${lastRun.commandLine}`,
    `Exit code: ${lastRun.exitCode === null ? "running or not yet reported" : lastRun.exitCode}`,
    "Recent output:",
    ...lastRun.output,
  ].join("\n");
}

async function openWorkspaceDocument(relativePath) {
  const workspaceRoot = getWorkspaceRoot();
  const document = await vscode.workspace.openTextDocument(path.join(workspaceRoot, relativePath));
  await vscode.window.showTextDocument(document);
}

async function startAndroidJdwpDebug() {
  const folder = vscode.workspace.workspaceFolders && vscode.workspace.workspaceFolders[0];
  getWorkspaceRoot();
  const started = await vscode.debug.startDebugging(folder, {
    name: "Android: Attach ByteTrain App (JDWP, already prepared)",
    type: "java",
    request: "attach",
    hostName: "localhost",
    port: 8700,
    timeout: 30000,
  });
  if (!started) {
    vscode.window.showErrorMessage("Android JDWP debug did not start. Check localhost:8700 and Java Debugger extension.");
  }
}

function deactivate() {}

module.exports = {
  activate,
  deactivate,
  COMMANDS,
  DOCUMENT_COMMANDS,
  formatDiagnosticContext,
};
