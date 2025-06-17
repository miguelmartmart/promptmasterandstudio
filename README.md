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
- **Zoomable Content:** Allows users to zoom in on the main content area for better readability and accessibility.

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

## Technical Stack

PromptMaster is built on a modern Android development stack, leveraging the following key technologies:

*   **Framework:** Android (Native)
*   **Language:** Kotlin (JVM Target 17)
*   **UI Framework:** Jetpack Compose (for declarative UI development)
*   **Build System:** Gradle (using Kotlin DSL)
*   **Dependency Injection:** Hilt (for robust and scalable dependency management)
*   **Data Persistence:** Room Persistence Library (SQLite ORM for local data storage)
*   **Asynchronous Operations:** Kotlin Coroutines, Jetpack WorkManager (for background tasks and database pre-population)
*   **Navigation:** Jetpack Navigation Compose
*   **JSON Serialization/Deserialization:** Gson
*   **Image Loading:** Coil (for efficient image loading in Compose)
*   **Advertising:** Google Mobile Ads (AdMob)
*   **Preferences:** AndroidX Preference KTX

## Functional Overview

In addition to the features listed above, PromptMaster provides:

*   **Comprehensive Prompt Management:** Beyond basic CRUD, the app supports duplicating prompts and marking them as used, influencing their sorting order.
*   **Dynamic UI:** Utilizes Jetpack Compose for a responsive and modern user interface, including a custom zoomable content area for enhanced accessibility.
*   **Theming & Localization:** Supports dark mode and multiple languages, with dynamic language switching at runtime.
*   **Background Data Operations:** Employs WorkManager for efficient background tasks like initial prompt data pre-population and backup/restore operations, ensuring a smooth user experience.
*   **Ad Integration:** Incorporates a custom affiliate ad system that dynamically fetches product details from affiliate links and displays them in an alternating fashion.

## Managing Affiliate Ads

The application displays affiliate advertisements by dynamically fetching product information from a list of affiliate links. This section explains how to manage these ads.

### 1. Affiliate Links File

All affiliate links are stored in a plain text file:
*   **Path:** `app/src/main/assets/affiliate_links.txt`

### 2. Adding New Ads

To add new affiliate ads:
1.  Open the `affiliate_links.txt` file.
2.  Add each new affiliate product link on a **new line**.
    *   **Example:**
        ```
        https://www.amazon.com/your-product-link-1
        https://www.example.com/another-affiliate-link
        ```
3.  Save the file.

### 3. Overriding Scraped Data (Manual Configuration)

If the automatic scraping for a particular affiliate link does not yield satisfactory results (e.g., missing image, incorrect title/description), you can provide manual override data.

*   **Configuration File:** `app/src/main/assets/manual_ads_config.json`
*   **Purpose:** This JSON file allows you to specify the exact `imageUrl`, `title`, and `description` for a given affiliate link. The application will prioritize this manual data if the dynamic scraping fails or returns empty values for those fields.

To add or update manual override data:
1.  Open the `manual_ads_config.json` file.
2.  Add a new entry (or modify an existing one) where the **key is the full affiliate link** and the **value is a JSON object** containing the `imageUrl`, `title`, and `description` you wish to use.
    *   **Example:**
        ```json
        {
          "https://amzn.to/4jWRLR7": {
            "imageUrl": "https://your-custom-image.jpg",
            "title": "Your Custom Product Title",
            "description": "Your custom product description here."
          },
          "https://www.example.com/another-affiliate-link": {
            "imageUrl": "https://another-custom-image.png",
            "title": "Another Product Title",
            "description": "Another custom description."
          }
        }
        ```
    *   **Note:** You only need to include the fields you want to override. If a field is omitted from the JSON object, the application will still attempt to scrape it dynamically.
3.  Save the file.

### 4. How Ads Are Processed

The application processes and displays affiliate advertisements using a multi-stage approach to ensure maximum reliability and flexibility:

1.  **Reading Affiliate Links:** The process begins by reading the raw affiliate links, one per line, from the `app/src/main/assets/affiliate_links.txt` file. These are the primary links that the application will attempt to display ads for.

2.  **Dynamic Content Scraping (First Attempt):** For each affiliate link, the application attempts to dynamically fetch the product's information (title, image URL, and description) directly from the linked webpage. This is done by:
    *   Making an HTTP request to the affiliate link to retrieve its HTML content.
    *   Parsing the HTML to look for standard **Open Graph (OG) meta tags** (e.g., `<meta property="og:title" content="...">`, `<meta property="og:image" content="...">`, `<meta property="og:description" content="...">`). These tags are widely used by websites for social media sharing and SEO, making them a common and reliable source.
    *   If OG tags are not found or are incomplete, the system attempts to extract information from common HTML elements as **generic fallbacks** (e.g., the `<title>` tag for the product title, `<img>` tags with `src` attributes for images, or `div` elements with `itemprop="description"` for descriptions).
    *   **Special Handling for Amazon Links:** Due to Amazon's complex and dynamic page structure, additional logic is applied specifically for Amazon links if the generic scraping methods fail. This includes looking for Amazon-specific HTML elements (like `#productTitle` for title, and `data-a-dynamic-image`, `#imgBlkFront`, `.image.item.view-image`, `img[data-old-hires]` attributes for images) to improve the chances of successful data extraction. Relative image URLs (starting with `//`) are automatically converted to `https://` URLs.

3.  **Manual Data Override (Second Attempt):** After the dynamic scraping attempts, the application checks if any of the `title`, `image URL`, or `description` fields are still missing or empty for the current affiliate link. If so, it then consults the `app/src/main/assets/manual_ads_config.json` file.
    *   If an entry exists in `manual_ads_config.json` for the specific affiliate link, any provided `imageUrl`, `title`, or `description` in that entry will be used to fill in the missing fields. This allows you to manually ensure correct ad display for links that are difficult to scrape.

4.  **Generic Placeholder (Final Fallback):** If, after both dynamic scraping and applying manual overrides, any of the `title`, `image URL`, or `description` fields are still empty, the application will use generic placeholder text and a default "No Image" image. This ensures that an ad is always displayed, even if complete product information cannot be obtained.

5.  **Ad Display:** Once all information is gathered (scraped, overridden, or generic), the complete ad (image, title, description, and the original affiliate link) is displayed on the screen. The ads then alternate every 10 seconds.

### 5. Important Considerations

*   **Internet Connection:** The device running the app must have an active internet connection to fetch ad details.
*   **Link Validity:** Ensure the links in `affiliate_links.txt` are valid and point to actual product pages. Invalid links (e.g., 404 pages) will result in the ad not being displayed.
*   **Website Structure Changes:** The dynamic fetching relies on parsing website HTML. If a website's structure changes significantly, the parsing logic might need updates to correctly extract information.
*   **Incomplete Data:** If the application cannot extract a title, image, or description for a given link, that ad will not be displayed unless a manual override is provided. Check `Logcat` for warnings from `AdRepository` (tag: "AdRepository") if ads are missing or if manual overrides are being used.

## Building and Running the Application

This section provides instructions on how to build and run the PromptMaster Android application using Gradle commands.

### Prerequisites

Before you begin, ensure you have:
*   **Java Development Kit (JDK) 17 or higher** installed.
*   **Android SDK** installed and configured.
*   **Gradle** (usually bundled with Android Studio, or can be installed separately).
*   An Android device or emulator connected and configured for debugging.

### 1. Clean the Project

It's good practice to clean the project before a fresh build to remove any old build artifacts.

*   **Command:** `./gradlew clean`
*   **Purpose:** Deletes the `build` directory and its contents for all modules, ensuring a clean slate for the next build.
*   **When to execute:** Before a fresh build, especially if you encounter unexpected build issues.

    ```bash
    ./gradlew clean
    ```

### 2. Build the Debug APK

This command compiles the application code and resources into a debug APK file.

*   **Command:** `./gradlew assembleDebug`
*   **Purpose:** Builds the debug version of the application. The generated APK can be found in `app/build/outputs/apk/debug/`.
*   **When to execute:** After making code changes and before installing the app on a device/emulator.

    ```bash
    ./gradlew assembleDebug
    ```

### 3. Install the Debug APK on a Connected Device/Emulator

Once the debug APK is built, you can install it directly onto a connected Android device or running emulator.

*   **Command:** `./gradlew installDebug`
*   **Purpose:** Installs the debug APK onto the connected device or active emulator. This command also triggers a build if the project is not up-to-date.
*   **When to execute:** After building the debug APK, to deploy and run the application.

    ```bash
    ./gradlew installDebug
    ```

    **Example Output (successful installation):**
    ```
    > Task :app:installDebug
    INSTALL_PARSE_FAILED_NO_CERTIFICATES: Failed to collect certificates from /data/app/vmdl123456789.tmp/base.apk: Signature mismatch for classes.dex
    BUILD SUCCESSFUL in 1m 30s
    ```
    *(Note: The "Signature mismatch" message might appear if you're reinstalling an app that was previously signed with a different key, but the `BUILD SUCCESSFUL` indicates the installation attempt completed.)*

### 4. Run the Application (from Android Studio)

While the `installDebug` command deploys the app, you can also run it directly from Android Studio for a more integrated development experience.

*   **Steps:**
    1.  Open the project in Android Studio.
    2.  Select your target device/emulator from the dropdown menu in the toolbar.
    3.  Click the green 'Run' button (▶) in the toolbar.
*   **Purpose:** Builds, installs, and launches the application on the selected device/emulator, and connects the debugger.
*   **When to execute:** During active development for quick testing and debugging.

### Troubleshooting Common Build Issues

*   **"Unresolved reference" errors:**
    *   Ensure all necessary dependencies are declared in `app/build.gradle.kts`.
    *   Perform a clean build (`./gradlew clean build`).
    *   In Android Studio, try `File > Invalidate Caches / Restart...`.
*   **"Type mismatch" or "if must have both main and 'else' branches" errors:**
    *   Carefully review the Kotlin code, especially `if` expressions and nullable types (`?`). Ensure all branches return the expected type, and handle `null` values explicitly using safe calls (`?.`), Elvis operator (`?:`), or `ifEmpty { "" }`/`orEmpty()`.
*   **Network-related issues (for ad fetching):**
    *   Ensure your device/emulator has an active internet connection.
    *   Check `Logcat` for `AdRepository` errors (e.g., "Error fetching ad details").
    *   Verify that the affiliate links in `app/src/main/assets/affiliate_links.txt` are valid and accessible.

By following these steps, you should be able to successfully build and run the PromptMaster application, including the newly integrated dynamic advertisement system.
