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
    id: "bytetrain.bazelHelper.assembleDebug",
    label: "Gradle: Assemble Debug",
    target: "gradle-app",
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
}

function runTarget(command, output) {
  const workspaceRoot = getWorkspaceRoot();
  const scriptPath = path.join(workspaceRoot, "scripts", "ide-build.ps1");
  const args = [
    "-NoProfile",
    "-ExecutionPolicy",
    "Bypass",
    "-File",
    scriptPath,
    "-Target",
    command.target,
  ];

  output.show(true);
  output.appendLine("");
  output.appendLine(`[${new Date().toISOString()}] ${command.label}`);
  output.appendLine(`Working directory: ${workspaceRoot}`);
  output.appendLine(`Command: powershell ${args.join(" ")}`);

  const child = spawn("powershell", args, {
    cwd: workspaceRoot,
    shell: false,
  });

  child.stdout.on("data", (data) => output.append(data.toString()));
  child.stderr.on("data", (data) => output.append(data.toString()));
  child.on("error", (error) => {
    output.appendLine(`Failed to start command: ${error.message}`);
    vscode.window.showErrorMessage(`${command.label} failed to start. See ByteTrain Bazel Helper output.`);
  });
  child.on("close", (code) => {
    output.appendLine(`Exit code: ${code}`);
    if (code === 0) {
      vscode.window.setStatusBarMessage(`${command.label} finished`, 5000);
      return;
    }

    vscode.window.showErrorMessage(`${command.label} failed with exit code ${code}. See ByteTrain Bazel Helper output.`);
  });
}

function deactivate() {}

module.exports = {
  activate,
  deactivate,
  COMMANDS,
};
