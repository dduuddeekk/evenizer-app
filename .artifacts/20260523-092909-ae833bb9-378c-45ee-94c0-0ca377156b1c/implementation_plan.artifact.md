# Implementation Plan - Add Multiple Organizer Roles (Sie)

Implement a new page `CreateOrganizerRolesPage` that allows an organizer owner to add multiple roles ("Sie") to their organizer. Each role will be created via a separate call to the `POST /api/organizer/{uuid}/roles` endpoint.

## Proposed Changes

### [Network Component]

#### [OrganizerModels.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/network/model/OrganizerModels.kt)

- Add `@Serializable` data classes for role request and response:
    - `CreateRoleRequest(val name: String, val description: String)`
    - `RoleData(val uuid: String, val name: String, val organizerUuid: String, val description: String, val createdAt: String, val updatedAt: String)`
    - `RoleResponse(val success: Boolean, val code: String, val message: String, val data: RoleData? = null)`

#### [OrganizerService.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/data/network/service/OrganizerService.kt)

- Add the `createRole` function:
    ```kotlin
    @POST("organizer/{uuid}/roles")
    suspend fun createRole(
        @Path("uuid") uuid: String,
        @Body request: CreateRoleRequest
    ): RoleResponse
    ```

---

### [ViewModel Component]

#### [OrganizerViewModel.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/models/OrganizerViewModel.kt)

- Add a state to track multiple role creation progress.
- Add `addMultipleRoles` function:
    ```kotlin
    fun addMultipleRoles(context: Context, organizerUuid: String, roles: List<Pair<String, String>>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val service = NetworkModule.getOrganizerService(context)
                roles.forEach { (name, description) ->
                    service.createRole(organizerUuid, CreateRoleRequest(name, description))
                }
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to add roles: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    ```

---

### [UI Component]

#### [NEW] [CreateOrganizerRolesPage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/CreateOrganizerRolesPage.kt)

- Page layout with:
    - Header with back button.
    - Dynamic list of `OutlinedTextField` pairs (Name, Description).
    - "Add Another Role" button to append a new empty entry to the list.
    - "Save All Roles" button to trigger `addMultipleRoles`.
    - Handle loading state and errors.

#### [MainScreen.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/screens/MainScreen.kt)

- Add a new route `create_organizer_roles/{uuid}`.
- Map it to `CreateOrganizerRolesPage`.

#### [OrganizerDetailPage.kt](file:///C:/Users/Dudek/AndroidStudioProjects/Evenizer/app/src/main/java/com/dudek/evenizer/pages/OrganizerDetailPage.kt)

- Update `onAddRole` callback to navigate to `create_organizer_roles/{uuid}`.

---

## Verification Plan

### Automated Tests
- Build check: `./gradlew app:assembleDebug`

### Manual Verification
- Navigate to Organizer Detail.
- Click "Add Role" (Tambah Sie) in the FAB menu.
- Verify navigation to the new page.
- Add multiple role entries using "Add Another Role".
- Fill in names and descriptions.
- Click "Save All Roles".
- Verify that it returns to the detail page on success.
- (Optional) Check logs/network to see multiple POST requests being sent.
