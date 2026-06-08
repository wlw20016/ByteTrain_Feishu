## MODIFIED Requirements

### Requirement: IDE debug support must not be overstated

The project MUST only claim VS Code/Trae breakpoint support when a checked-in, validated launch or attach workflow exists. If no validated VS Code/Trae workflow exists, the project MUST document Android Studio fallback as the supported breakpoint path.

#### Scenario: VS Code breakpoint workflow is supported

- Given a checked-in VS Code launch or attach configuration exists
- When a developer follows the documented debug workflow on a connected device or emulator
- Then the app launches or attaches successfully
- And a breakpoint in app code can be hit
- And the validation evidence records the device/emulator and command path used

#### Scenario: VS Code breakpoint workflow is unavailable

- Given VS Code Android attach tooling cannot be validated
- When a developer reads the IDE workflow documentation
- Then the documentation does not claim VS Code breakpoint support
- And Android Studio fallback is documented as the supported breakpoint workflow
