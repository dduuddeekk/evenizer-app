# Walkthrough - Event Organizer Invitations

This update introduces the **Penyelenggara Acara** (Event Organizer) invitation system, allowing event owners to manage third-party organizers for their events.

## 1. Event Organizer List
Event owners can now view a list of all organizers invited to their event.
- **API Integration**: Fetches data from `GET /api/organizer/event/:eventUuid`.
- **Status Grouping**: The list is categorized into **ACCEPTED**, **PENDING**, and **FINISHED** sections for better clarity.
- **Role Display**: Each organizer card shows the specific roles (Sie) they have been assigned for that event.

## 2. Invite Organizer Workflow
A dedicated page for inviting new organizers to an event.
- **Integrated Selection**:
    - **Organizer Search**: Search and select from all available organizers via `GET /api/organizer`.
    - **Multi-Role Selection**: Once an organizer is selected, you can choose multiple roles (Sie) for them from their specific role list.
- **Invitation Logic**: Sends a `POST /api/event/:uuid/organizers` request with the selected `organizerUuid` and `roleUuids`.

## 3. UI/UX and Navigation
- **Owner Access**: The "Penyelenggara Acara" option only appears in the `EventDetailPage` FAB for the event owner.
- **Navbar Selection**: Bottom navigation bar remains correctly active during the entire invitation and management flow.
- **Visual Consistency**: High-quality header design, consistent with the rest of the application.

## Technical Details
- **ViewModels**: `EventViewModel` manages the event organizer list and invitation logic.
- **Models**: Added `InviteOrganizerRequest`, `EventOrganizerData`, and response containers.
- **Services**: Updated `OrganizerService.kt` and `EventService.kt` with the new endpoints.

## Verification
- **Build**: Successfully compiled using `./gradlew app:assembleDebug`.
- **Functionality**: Verified that selecting an organizer dynamically loads its roles for assignment.
