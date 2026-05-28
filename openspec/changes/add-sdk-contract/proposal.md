# Proposal: add-sdk-contract

## Why

The current proto files and Rust SDK are placeholders. The project requires a stable contract between Android UI and the SDK layer, with mock data parsing/async service boundaries and protobuf as the shared data contract.

## What

- Align Kotlin domain models and protobuf schemas for message, mail, and paging.
- Define page response contracts for message and mail data.
- Implement Rust SDK mock data models, pagination, and error handling.
- Define the async SDK boundary and Kotlin adapter strategy.
- Prepare UI data source replacement from Kotlin mock repositories to SDK-backed repositories.

## Impact

- Affects `proto/`, `sdk/rust/`, and feature data-layer adapters.
- May require Bazel/rules integration from `wire-bazel-build` before full cross-language generation is validated.
- Keeps UI rendering behavior stable while allowing data source migration.
