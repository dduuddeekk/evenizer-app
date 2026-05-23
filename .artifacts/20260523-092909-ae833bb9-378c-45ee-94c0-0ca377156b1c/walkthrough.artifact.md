# Walkthrough - Skeleton Loading, Real-time Polling & Multiple Organizer Roles

This update introduces significant improvements to the user experience through better loading states, real-time data updates, and a streamlined workflow for adding multiple organizer roles.

## 1. Skeleton Loading
Replaced standard `CircularProgressIndicator` with shimmer-based skeleton loading across all main pages. This reduces layout shifts and provides a better visual cue during data fetching.

- **[SkeletonUtils.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/utils/SkeletonUtils.kt)**: Houses the `shimmerEffect` and various skeleton components (`EventCardSkeleton`, `OrganizerCardSkeleton`, etc.).
- **Pages Updated**: `HomePage`, `EventPage`, `OrganizerPage`, `ProfilePage`, `TicketPage`, and `SettingsPage`.

## 2. Real-time Data Polling
Implemented a 10-second polling mechanism to keep the application data synchronized with the server automatically.

- **ViewModels**: `EventViewModel` and `OrganizerViewModel` now include `startRealtime` and `stopRealtime` logic.
- **Lifecycle Management**: Used `DisposableEffect` in the UI pages to ensure polling only runs when the page is active, preserving battery and data.

## 3. Multiple Organizer Roles (Sie)
Added the ability for organizer owners to add multiple roles in a single session via a new dedicated page.

### Key Components:
- **[CreateOrganizerRolesPage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/CreateOrganizerRolesPage.kt)**: A dynamic form page where users can add/remove role input fields.
- **API Integration**: Sends multiple POST requests to `/api/organizer/{uuid}/roles` sequentially upon clicking "Simpan Semua Sie".
- **Navigation**: Linked the "Tambah Sie" menu in `OrganizerDetailPage` to the new creation page.

## Verification Results
- **Build Status**: Successful (`./gradlew app:assembleDebug`).
- **Functionality**: Verified that multiple role inputs generate multiple network requests and that polling keeps the list views updated.
