# Walkthrough - Batch Member Invitations

This update introduces the ability to invite multiple members to an organizer at once, with integrated user searching and role assignment.

## 1. Batch Member Invitations
Previously, members had to be added one by one. Now, a dedicated page allows for batch invitations.

### Key Components:
- **[AddOrganizerMemberPage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/AddOrganizerMemberPage.kt)**:
    - Features a dynamic list of invitation cards.
    - Integrated **User Search**: Fetches all users via `GET /user` and filters them as you type.
    - **Role Selection**: Allows choosing from the roles available in the current organizer.
- **Batch Logic**: The `OrganizerViewModel` handles the sequential API calls to `POST /organizer/{uuid}/members`.

## 2. API & Model Updates
- **[UserService.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/network/service/UserService.kt)**: Added `getAllUsers` to support the search functionality.
- **[OrganizerService.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/network/service/OrganizerService.kt)**: Added the `addMember` endpoint.
- **Models**: Added `UserListResponse`, `CreateMemberRequest`, and `MemberResponse` to `UserModels.kt` and `OrganizerModels.kt`.

## 3. UI/UX Consistency
- The page is integrated into the existing navigation graph, ensuring the **bottom navigation bar** remains correctly selected.
- Linked the **"Tambah Anggota"** button in `OrganizerDetailPage` to the new invitation page.

## Verification Results
- **Build Status**: Successful (`./gradlew app:assembleDebug`).
- **Functionality**: Verified that searching for users works and that multiple POST requests are sent sequentially upon saving.
