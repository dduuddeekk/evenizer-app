# Organizer Pagination and UI Improvements

This plan outlines the steps to fix the event card background color on the Home page and implement SQLite-backed pagination for the Organizers feature using Room and Paging 3.

## User Review Required

> [!NOTE]
> The pagination implementation follows the existing pattern used for Events (RemoteMediator + Room).
> The SQLite database is located at `C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/local/AppDatabase.kt`.

## Proposed Changes

### [UI Fix] Home Page

#### [HomePage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/HomePage.kt)
- Modify `HomeEventCardFromData` to use `surfaceVariant.copy(alpha = 0.4f)` as the background color to match the `OrganizerCard` style.

---

### [Database] Organizer Persistence

#### [NEW] [OrganizerEntity.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/local/entity/OrganizerEntity.kt)
- Create a Room entity for `OrganizerData`.

#### [NEW] [OrganizerDao.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/local/dao/OrganizerDao.kt)
- Create a DAO with methods for inserting, deleting, and querying paged organizers.

#### [AppDatabase.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/local/AppDatabase.kt)
- Register `OrganizerEntity` and add `organizerDao()`.

---

### [Paging] Organizer Remote Mediator

#### [NEW] [OrganizerRemoteMediator.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/paging/OrganizerRemoteMediator.kt)
- Implement `RemoteMediator` for `OrganizerEntity` to handle network fetching and local cache synchronization.

---

### [Data Layer] Repository and ViewModel

#### [NEW] [OrganizerRepository.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/repository/OrganizerRepository.kt)
- Create a repository that provides a `Pager` for organizers.

#### [OrganizerViewModel.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/models/OrganizerViewModel.kt)
- Expose a `Flow<PagingData<OrganizerData>>` for paged organizers.

---

### [UI] Organizer Pagination

#### [OrganizerPage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/OrganizerPage.kt)
- Update the UI to collect and display paged organizers using `collectAsLazyPagingItems()`.

## Verification Plan

### Automated Tests
- No specific automated tests exist for this logic, but I will verify compilation.

### Manual Verification
1. **Home Page**: Verify the event card background color matches the organizer cards.
2. **Organizer Page**: Scroll through the list to verify that pagination works (fetching more items as needed).
3. **Database**: Verify that organizers are being cached in SQLite by checking the logs (if logging added) or inspecting the database.
