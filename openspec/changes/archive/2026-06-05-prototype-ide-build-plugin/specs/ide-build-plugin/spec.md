## ADDED Requirements

### Requirement: IDE build plugin prototype must invoke the shared build helper

The IDE plugin prototype MUST invoke `scripts/commands/ide-build.ps1` for build, test, and query commands instead of duplicating Bazel command strings.

#### Scenario: User runs app build from IDE plugin

- **WHEN** the user selects the app build command from the plugin prototype
- **THEN** the plugin invokes `scripts/commands/ide-build.ps1 -Target app`

### Requirement: Plugin output must be visible in the IDE

The IDE plugin prototype MUST expose command output and failure exit codes in an IDE-visible output channel or equivalent log.

#### Scenario: Command fails

- **WHEN** a plugin command exits with a non-zero status
- **THEN** the user can see the failed command, exit code, and output summary inside the IDE

### Requirement: Trae compatibility must be documented

The project MUST document how Trae can use the same build entry points as VS Code.

#### Scenario: Trae user runs a build

- **WHEN** a Trae user wants to run a project build
- **THEN** the documentation identifies whether to use VS Code tasks, plugin commands, or direct `scripts/commands/ide-build.ps1` invocation
