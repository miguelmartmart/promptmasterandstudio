# PromptMaster

PromptMaster is an Android application designed to help users manage and organize their prompts.

## Features

- **Prompt Management:** Easily add, edit, delete, and duplicate your prompts.
- **Favorites:** Mark your most used prompts as favorites for quick access.
- **Search and Filtering:** Find prompts quickly by searching and filtering by category, subcategory, and other criteria.
- **Progressive Loading:** The prompt list efficiently handles a large number of prompts by loading them in batches as you scroll. A loading indicator is shown while more prompts is being fetched.
- **Backup and Restore:** Export your prompts to a JSON file for backup and import them later to restore your collection.
- **Data Reset:** Reset the application data to its initial state.
- **Multilanguage Support:** The application supports multiple languages.
- **Dark Mode:** Switch to dark mode for a more comfortable viewing experience in low light conditions.

## Technical Enhancements

Recent updates have significantly improved the application's data handling and UI reactivity:

-   **Reactive Data Flow with Database Readiness Signal:**
    *   Implemented a `databaseReadyEvent` (`SharedFlow`) in the `PromptRepository` to signal when the Room database has been fully initialized and preloaded with initial data.
    *   This signal is integrated into the database's `RoomDatabase.Callback` (both `onCreate` and `onOpen`), ensuring that data fetching is synchronized with database readiness.
    *   The `PromptDataFetcher` now observes this event, guaranteeing that prompts are only fetched and displayed after the database is confirmed to be populated, resolving initial empty list issues.

-   **Guaranteed UI Refresh Mechanism:**
    *   Introduced a robust `_refreshTrigger` (`MutableStateFlow<Int>`) within `PromptDataFetcher`.
    *   This trigger is part of the data `combine` flow, and its value is incremented whenever a UI refresh is explicitly needed (e.g., after marking as favorite, duplicating, deleting, marking as used, importing, or resetting data).
    *   This ensures that the UI consistently reflects the latest database state, providing immediate feedback to user actions.

-   **Improved Prompt Sorting Logic:**
    *   Modified the underlying SQL queries in `PromptDao` to prioritize sorting. Prompts are now ordered by `isFavorite DESC` (favorited prompts appear first) and then by `lastUsed DESC` (most recently used prompts appear at the top within their favorite/non-favorite groups). This ensures that the "last used" prompt always moves to the first position.

-   **Enhanced Debugging Capabilities:**
    *   Added comprehensive `android.util.Log.d` statements with the tag "PromptMasterDebug" across key components (UI, ViewModel, Data Operations, Repository, DAO, Data Fetcher). These logs facilitate easier tracing and debugging of data flow and user interactions.

-   **Dependency Injection with Hilt:**
    *   **Enhanced Modularity:** Integrated Hilt, a dependency injection framework, to manage the creation and provision of core application components (e.g., `PromptRepository`, `PromptBackupManager`, `PromptUtils`, `PromptDataOperations`). This significantly decouples classes, making them more independent and easier to maintain.
    *   **Simplified ViewModel Management:** ViewModels (`PromptViewModel`, `FavoritesViewModel`) are now directly injected into Composables using `@HiltViewModel`, eliminating the need for manual ViewModel factories and reducing boilerplate code.
    *   **Improved Testability:** The explicit dependency graph provided by Hilt makes it much easier to create isolated unit and integration tests for various parts of the application.
    *   **AI-Friendly Codebase:** By clearly defining dependencies and promoting smaller, more focused classes, the codebase becomes more understandable and navigable for AI tools, facilitating automated code analysis, refactoring, and generation.
