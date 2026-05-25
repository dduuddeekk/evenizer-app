# Walkthrough - Event Rundown & Owner Actions

This update introduces the **Event Rundown** management system and adds exclusive actions for event owners within the detail page.

## 1. Owner-Exclusive FAB (3-Dot)
In the **Event Detail Page**, a new Floating Action Button (FAB) now appears **only if the logged-in user is the owner** of the event.
- **Sunting Acara**: Navigates to the event editing flow.
- **Rundown Acara**: Navigates to the rundown management page.

## 2. Event Rundown Page
A dedicated page to view the flow of the event, categorized by visibility.
- **Tabs**: Supports **PUBLIC** and **PRIVATE** rundowns using a tabbed interface.
- **Dynamic List**: Fetches real-time rundown data from `GET /api/event/:uuid/rundowns`.
- **Visibility Control**: Ensures internal team items (PRIVATE) are separated from attendee items (PUBLIC).

## 3. Add Rundown Page
Allows event owners to add new items to the event timeline.
- **Integrated Selection**:
    - **Date & Time Pickers**: Native Android dialogs for precise timing.
    - **Location Selection**: Dropdown menu populated directly from the event's associated locations (`eventLocations`).
- **Real-time Feedback**: Automatically refreshes the rundown list upon successful creation.

## Technical Details
- **ViewModel**: `EventViewModel` now manages `eventRundowns` state and creation logic.
- **Service**: Updated `EventService.kt` with GET and POST endpoints for rundowns.
- **Models**: Added `RundownData`, `CreateRundownRequest`, and response wrappers in `EventModels.kt`.
- **Navigation**: Bottom navbar correctly persists its selection during rundown management.

## Verification
- **Build**: Successfully compiled with `./gradlew app:assembleDebug`.
- **Access Control**: FAB verified to be hidden for non-owners.
- **Data Flow**: Verified that selected locations correctly pass their UUID to the creation request.
