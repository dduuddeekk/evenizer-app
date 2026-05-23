# Implementation Plan - Update and Delete Organizer Roles (Sie)

Implement the ability to update and delete roles (sie) from the Organizer Detail page.

## Proposed Changes

### [Network Component]

#### [OrganizerService.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/network/service/OrganizerService.kt)

- Add `updateRole` and `deleteRole` endpoints:
    ```kotlin
    @PATCH("organizer/{uuid}/roles/{roleUuid}")
    suspend fun updateRole(
        @Path("uuid") uuid: String,
        @Path("roleUuid") roleUuid: String,
        @Body request: CreateRoleRequest
    ): RoleResponse

    @DELETE("organizer/{uuid}/roles/{roleUuid}")
    suspend fun deleteRole(
        @Path("uuid") uuid: String,
        @Path("roleUuid") roleUuid: String
    ): RoleResponse
    ```

---

### [ViewModel Component]

#### [OrganizerViewModel.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/models/OrganizerViewModel.kt)

- Add `updateRole` and `deleteRole` functions:
    ```kotlin
    fun updateRole(context: Context, organizerUuid: String, roleUuid: String, name: String, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.updateRole(organizerUuid, roleUuid, CreateRoleRequest(name, description))
                if (response.success) {
                    onSuccess()
                    fetchOrganizerDetail(context, organizerUuid)
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to update role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRole(context: Context, organizerUuid: String, roleUuid: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                val response = service.deleteRole(organizerUuid, roleUuid)
                if (response.success) {
                    onSuccess()
                    fetchOrganizerDetail(context, organizerUuid)
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete role: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    ```

---

### [UI Component]

#### [OrganizerDetailPage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/OrganizerDetailPage.kt)

- Add Edit (pencil) and Delete (trash) icons to each role card.
- Implement delete confirmation dialog.
- Link edit icon to navigation.

#### [NEW] [UpdateOrganizerRolePage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/UpdateOrganizerRolePage.kt)

- Create a page for editing a specific role.
- Fields for Name and Description (pre-filled).

#### [MainScreen.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/screens/MainScreen.kt)

- Add route for `update_organizer_role/{uuid}/{roleUuid}/{name}/{description}`.

---

## Verification Plan

### Automated Tests
- Build check: `./gradlew app:assembleDebug`

### Manual Verification
- Navigate to Organizer Detail as owner.
- Click Delete icon on a role, confirm, and verify it's removed.
- Click Edit icon on a role, change values, save, and verify it's updated.
