# Change: polish-ui-navigation-names

## Why

The current Android preview still shows text-heavy bottom navigation buttons and mock message titles with numeric suffixes such as `Mia Zhang 2`. This makes the UI look like test data rather than a product preview.

## What Changes

- Replace the bottom navigation text buttons with icon-based message and mail tabs.
- Keep accessible labels on the icon tabs through `contentDescription`.
- Stop appending visible numeric suffixes to mock conversation names.
- Add a focused verification script for the UI polish requirements.

## Impact

- Affects `MainActivity` bottom tab rendering.
- Affects message mock data generation.
- Adds Android vector drawable resources for the two bottom navigation icons.
- Does not change routes, paging behavior, or detail navigation.
