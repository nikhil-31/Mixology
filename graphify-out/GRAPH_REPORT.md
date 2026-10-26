# Graph Report - Mixology  (2026-10-24)

## Corpus Check
- 169 files · ~142,370 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1238 nodes · 2225 edges · 87 communities (71 shown, 16 thin omitted)
- Extraction: 80% EXTRACTED · 20% INFERRED · 0% AMBIGUOUS · INFERRED: 449 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3867bce9`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AnalyticsTracker
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- openDrinkIntent
- SearchIntent
- MixologyApp
- BarViewModel
- SearchViewModelTest
- MixologyDatabase
- FakeCocktailService
- CocktailDbDrink
- CocktailURLs.java
- MainIntent
- ShoppingItemEntity
- Overlay
- FakeChatRepository
- ThemeMode
- PlayUpdateController
- DrinkGridViewModel
- fetch_random_drink.py
- Drink
- Mixology
- IngredientMeasure
- DrinkDetailsIntent
- fetch_randomixer_csv.py
- CocktailDbResponse
- gradlew
- DrinkRepository
- DrinkDetailsViewModelTest.kt
- custom.md
- PlayReviewClient
- .setState
- FilterKind
- MainDispatcherRule
- DrinkRecipeBody
- DrinkCard
- Play Store listing
- DrinkDetailsViewModel
- SharedPrefsReviewPromptStore
- MviViewModel
- MixologyDatabaseMigrationTest
- ActivityMainEspressoTest
- MemoryReviewPromptStore
- BarViewModelTest.kt
- DrinkDetailsContent
- PlayReviewController
- BarMatcherTest
- openPlayStoreListing
- ChatStreamEvent
- ReviewPromptStore
- cocktailDrink
- IntentExtras.kt
- IntentExtrasTest
- NetworkMonitorTest
- PlayProvidesModule
- MixologyBottomBar
- MainViewModel
- BannerAd
- DrawerDestination
- ScreenHeader
- ActivityMain
- AdaIntent
- HotViewModelTest.kt
- SearchViewModel
- AuthModels.kt
- NetworkModule
- DrinkViewPreferences
- FakeCocktailService.kt
- MixologyThemeTest
- AdaUiState
- DrinkFilterTest
- SseParser
- SearchMode
- Session
- NetworkChatRepository
- ChatModels.kt
- AdaPlushie.kt
- ApiErrors

## God Nodes (most connected - your core abstractions)
1. `Drink` - 86 edges
2. `MixologyTheme()` - 57 edges
3. `DrinkRepository` - 46 edges
4. `DrinkDao` - 33 edges
5. `MixologyApp()` - 26 edges
6. `cocktailDrink()` - 22 edges
7. `CocktailDbResponse` - 21 edges
8. `AdaViewModel` - 21 edges
9. `SearchScreen()` - 21 edges
10. `FakeCocktailService` - 20 edges

## Surprising Connections (you probably didn't know these)
- `reloadSameDrink_logsViewOnce()` --calls--> `Drink`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/data/Drink.kt
- `catalog()` --calls--> `CatalogListResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt
- `seedMargarita()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt
- `bind_loadsCachedFilterDrinks()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt
- `toggleSaved_savesDrinkWithoutMessage()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt

## Import Cycles
- None detected.

## Communities (87 total, 16 thin omitted)

### Community 0 - "AnalyticsTracker"
Cohesion: 0.06
Nodes (19): analyticsScreenName(), AnalyticsTracker, firebaseAnalyticsOrNull(), forTests(), Context, LoggedEvent, AppEntryPoint, MyApplication (+11 more)

### Community 1 - "DrinkDao"
Cohesion: 0.08
Nodes (11): CatalogSeed, CatalogSeedDrink, CatalogSeedPayload, Context, CatalogTermEntity, DrinkDao, Flow, DrinkEntity (+3 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.35
Nodes (4): SearchUiState, SearchScreen(), MixologyTheme(), SearchScreenTest

### Community 4 - "HotViewModel"
Cohesion: 0.09
Nodes (23): recordCrash(), featuredDrink(), HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink (+15 more)

### Community 5 - "openDrinkIntent"
Cohesion: 0.13
Nodes (16): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+8 more)

### Community 6 - "SearchIntent"
Cohesion: 0.15
Nodes (12): Back, filterIngredientSuggestions(), NavigateBack, OpenDrink, Search, SearchEffect, SearchIntent, SelectSuggestion (+4 more)

### Community 7 - "MixologyApp"
Cohesion: 0.23
Nodes (13): detailsRoute(), encodeRouteArg(), filterKindFromRoute(), gridRoute(), isDetailsRoute(), isOverlayRoute(), isSearchRoute(), DrinkFilter (+5 more)

### Community 8 - "BarViewModel"
Cohesion: 0.08
Nodes (28): BarEffect, BarIntent, BarUiState, ClosePicker, filterBarTerms(), Load, OpenDrink, OpenPicker (+20 more)

### Community 10 - "MixologyDatabase"
Cohesion: 0.06
Nodes (13): BarDao, Flow, BarIngredientEntity, create(), importLegacySavedDrinks(), Context, MixologyDatabase, Flow (+5 more)

### Community 18 - "MainIntent"
Cohesion: 0.17
Nodes (11): DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails, OpenMenu (+3 more)

### Community 19 - "ShoppingItemEntity"
Cohesion: 0.10
Nodes (22): ShoppingItemEntity, groupShoppingSections(), isOnShoppingList(), orderShoppingItems(), ShoppingSection, ClearChecked, Remove, ShoppingEffect (+14 more)

### Community 20 - "Overlay"
Cohesion: 0.18
Nodes (9): Animator, Activity, Overlay, AnimatorListenerAdapter, SaveConfetti, Spark, Canvas, MotionEvent (+1 more)

### Community 21 - "FakeChatRepository"
Cohesion: 0.13
Nodes (11): AuthResult, ChatRepository, ChatTurn, Failure, Success, FakeChatRepository, Flow, send_errorEventSurfacesDetailAndStops() (+3 more)

### Community 22 - "ThemeMode"
Cohesion: 0.16
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, SharedPreferences, rememberThemeMode(), ThemeMode, DARK (+4 more)

### Community 23 - "PlayUpdateController"
Cohesion: 0.10
Nodes (15): Activity, SharedFlow, PlayUpdateController, PlayUpdateEvent, Downloaded, UpToDate, PlayUpdateAction, COMPLETE (+7 more)

### Community 24 - "DrinkGridViewModel"
Cohesion: 0.15
Nodes (13): Bind, DrinkGridEffect, DrinkGridIntent, DrinkGridUiState, OpenDrink, QueryChanged, ShowMessage, ShowMessageRes (+5 more)

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.12
Nodes (30): datetime, Path, adb(), dump_xml(), main(), parse_bounds(), shot(), tap() (+22 more)

### Community 26 - "Drink"
Cohesion: 0.16
Nodes (9): Drink, filterSavedDrinks(), DrinkGridRoute(), DrinkGridScreen(), DrinkFilter, SnackbarHostState, SwipeUnsaveRow(), DrinkGridScreenTest (+1 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "IngredientMeasure"
Cohesion: 0.13
Nodes (4): IngredientListConverter, IngredientMeasure, IngredientListConverterTest, IngredientMeasureTest

### Community 29 - "DrinkDetailsIntent"
Cohesion: 0.13
Nodes (14): AddToShoppingList, Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, OpenUrl, OpenVideo (+6 more)

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.32
Nodes (11): cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main(), parse_args(), Any (+3 more)

### Community 31 - "CocktailDbResponse"
Cohesion: 0.20
Nodes (3): CocktailService, CatalogListResponse, CocktailDbResponse

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 33 - "DrinkRepository"
Cohesion: 0.07
Nodes (8): WidgetEntryPoint, BarAlmostDrink, BarMatcher, BarRecommendations, DrinkRepository, hasUsableThumb(), DrinkFilter, Flow

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.33
Nodes (8): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), reloadSameDrink_logsViewOnce(), seedMargarita(), share_logsShareEvent(), toggleSaved_logsWishlistEvents()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 36 - "PlayReviewClient"
Cohesion: 0.24
Nodes (5): GooglePlayReviewClient, Activity, Activity, NoOpPlayReviewClient, PlayReviewClient

### Community 39 - ".setState"
Cohesion: 0.31
Nodes (4): AdaMessage, AdaViewModel, Job, withSaved()

### Community 40 - "FilterKind"
Cohesion: 0.05
Nodes (46): androidx, CatalogListItem, FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, CatalogEffect (+38 more)

### Community 41 - "MainDispatcherRule"
Cohesion: 0.40
Nodes (3): MainDispatcherRule, Description, TestWatcher

### Community 42 - "DrinkRecipeBody"
Cohesion: 0.18
Nodes (10): DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock(), instructionSteps(), Modifier, RecipeChip(), RecipeSectionTitle(), IngredientRow() (+2 more)

### Community 43 - "DrinkCard"
Cohesion: 0.18
Nodes (9): DrinkCard(), Dp, Modifier, DrinkImage(), Modifier, DrinkListItem(), FavoriteButton(), Modifier (+1 more)

### Community 44 - "Play Store listing"
Cohesion: 0.40
Nodes (4): English, Español, Play Store listing, Screenshot checklist

### Community 45 - "DrinkDetailsViewModel"
Cohesion: 0.29
Nodes (3): DrinkDetailsViewModel, Job, setUp()

### Community 46 - "SharedPrefsReviewPromptStore"
Cohesion: 0.22
Nodes (3): SharedPreferences, SharedPrefsReviewPromptStore, SharedPrefsReviewPromptStoreTest

### Community 47 - "MviViewModel"
Cohesion: 0.33
Nodes (9): CollectMviEffects(), Flow, StateFlow, MviStore, MviViewModel, E, I, S (+1 more)

### Community 50 - "MemoryReviewPromptStore"
Cohesion: 0.18
Nodes (5): MemoryReviewPromptStore, ReviewPromptPolicy, ReviewPromptState, PlayReviewControllerTest, RecordingReviewClient

### Community 51 - "BarViewModelTest.kt"
Cohesion: 0.31
Nodes (9): awaitItemUntil(), T, load_emptyBarHasNoRecommendations(), martini(), negroni(), recommendationsFailure_keepsTheBarOnScreen(), toggleIngredient_addsAndRemovesBarItem(), toggleSaved_logsWishlistEvents() (+1 more)

### Community 52 - "DrinkDetailsContent"
Cohesion: 0.24
Nodes (9): DrinkDetailsUiState, DrinkDetailsContent(), DrinkDetailsRoute(), DrinkDetailsScaffold(), FullscreenDrinkImage(), HeroActionButton(), ImageVector, SnackbarHostState (+1 more)

### Community 53 - "PlayReviewController"
Cohesion: 0.25
Nodes (4): disabled(), Activity, SharedFlow, PlayReviewController

### Community 55 - "openPlayStoreListing"
Cohesion: 0.22
Nodes (5): findActivity(), Activity, Context, openPlayStoreListing(), PlayStoreIntentsTest

### Community 56 - "ChatStreamEvent"
Cohesion: 0.12
Nodes (14): ChatStreamer, Flow, ChatHttpException, ChatStreamEvent, Delta, Done, Error, Exception (+6 more)

### Community 58 - "cocktailDrink"
Cohesion: 0.12
Nodes (9): cocktailDrink(), DrinkRepositoryTest, awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, loadedDrink(), openDrink_emitsEffect(), setUp() (+1 more)

### Community 59 - "IntentExtras.kt"
Cohesion: 0.40
Nodes (4): drinkExtra(), DrinkIntents, Intent, putDrinkExtra()

### Community 62 - "PlayProvidesModule"
Cohesion: 0.29
Nodes (5): Context, PlayBindsModule, PlayProvidesModule, AppUpdateManager, ReviewManager

### Community 63 - "MixologyBottomBar"
Cohesion: 0.33
Nodes (5): BottomNavItem, isBottomNavTab(), isSelectedBottomNav(), MixologyBottomBar(), MixologyBottomBarTest

### Community 64 - "MainViewModel"
Cohesion: 0.27
Nodes (3): MainViewModel, Activity, MainViewModelTest

### Community 65 - "BannerAd"
Cohesion: 0.32
Nodes (4): BannerAd(), bannerAdUnitRes(), Modifier, BannerAdTest

### Community 66 - "DrawerDestination"
Cohesion: 0.25
Nodes (8): Ada, Bar, Catalog, DrawerDestination, Filter, Hot, Settings, Shopping

### Community 67 - "ScreenHeader"
Cohesion: 0.32
Nodes (3): OfflineBanner(), ScreenHeader(), MixologyAppChromeTest

### Community 68 - "ActivityMain"
Cohesion: 0.29
Nodes (4): ActivityMain, Intent, AppCompatActivity, Bundle

### Community 69 - "AdaIntent"
Cohesion: 0.14
Nodes (14): AdaEffect, AdaIntent, DisplayNameChanged, DraftChanged, EmailChanged, Load, OpenDrink, PasswordChanged (+6 more)

### Community 70 - "HotViewModelTest.kt"
Cohesion: 0.39
Nodes (6): awaitItemUntil(), createViewModel(), T, load_fallsBackToPresetDrinkTypesWhenCatalogEmpty(), load_usesDrinkTypeCatalogTerms(), save_addsRecentlyViewedRowFirst()

### Community 72 - "AuthModels.kt"
Cohesion: 0.24
Nodes (6): AuthService, ApiDetail, AuthTokenResponse, AuthUser, LoginRequest, SignupRequest

### Community 73 - "NetworkModule"
Cohesion: 0.24
Nodes (6): Context, NetworkModule, HttpLoggingInterceptor, Interceptor, OkHttpClient, Retrofit

### Community 74 - "DrinkViewPreferences"
Cohesion: 0.40
Nodes (3): DrinkViewPreferences, DrinkViewToggle(), Context

### Community 75 - "FakeCocktailService.kt"
Cohesion: 0.28
Nodes (4): ApiBarAlmost, ApiBarRecommendations, ApiDrink, toApiDrink()

### Community 77 - "AdaUiState"
Cohesion: 0.46
Nodes (7): AdaUiState, AdaMessageRow(), AdaRoute(), AdaScreen(), AdaSignIn(), AdaTranscript(), Modifier

### Community 79 - "SseParser"
Cohesion: 0.31
Nodes (5): findSeparator(), parseSseBlock(), SseEvent, SseParser, SseParserTest

### Community 80 - "SearchMode"
Cohesion: 0.29
Nodes (7): searchModeFromRoute(), SearchMode, INGREDIENT, NAME, SnackbarHostState, SearchModeChips(), SearchRoute()

### Community 81 - "Session"
Cohesion: 0.27
Nodes (3): SharedPreferences, Session, SessionStore

### Community 83 - "ChatModels.kt"
Cohesion: 0.22
Nodes (6): ChatService, ChatHistoryResponse, ChatMessageDto, DeltaPayload, DonePayload, RecommendationsPayload

## Knowledge Gaps
- **112 isolated node(s):** `Delta`, `Recommendations`, `Done`, `Error`, `CocktailURLs` (+107 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkDao`, `MixologyTheme`, `HotViewModel`, `openDrinkIntent`, `MixologyApp`, `BarViewModel`, `FakeCocktailService`, `CocktailDbDrink`, `ShoppingItemEntity`, `DrinkGridViewModel`, `IngredientMeasure`, `DrinkRepository`, `DrinkDetailsViewModelTest.kt`, `.setState`, `DrinkCard`, `DrinkDetailsViewModel`, `ActivityMainEspressoTest`, `BarViewModelTest.kt`, `DrinkDetailsContent`, `BarMatcherTest`, `IntentExtras.kt`, `IntentExtrasTest`, `MainViewModel`, `SearchViewModel`, `FakeCocktailService.kt`?**
  _High betweenness centrality (0.305) - this node is a cross-community bridge._
- **Why does `DrinkRepository` connect `DrinkRepository` to `HotViewModelTest.kt`, `FilterKind`, `MixologyDatabase`, `DrinkDetailsViewModel`, `MemoryReviewPromptStore`, `ShoppingItemEntity`, `FakeChatRepository`, `cocktailDrink`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `AnalyticsTracker`, `BannerAd`, `MainViewModel`, `ScreenHeader`, `ActivityMain`, `HotViewModel`, `BarViewModel`, `FilterKind`, `AdaUiState`, `MviViewModel`, `SearchMode`, `ShoppingItemEntity`, `DrinkDetailsContent`, `ThemeMode`, `openPlayStoreListing`, `Drink`, `MixologyBottomBar`?**
  _High betweenness centrality (0.117) - this node is a cross-community bridge._
- **Are the 39 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.shoppingList_groupsIngredientsByDrinkAndKeepsMeasures()`) actually correct?**
  _`Drink` has 39 INFERRED edges - model-reasoned connections that need verification._
- **Are the 56 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 56 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `DrinkRepository` (e.g. with `setUp()` and `setUp()`) actually correct?**
  _`DrinkRepository` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 21 inferred relationships involving `MixologyApp()` (e.g. with `.onCreate()` and `analyticsScreenName()`) actually correct?**
  _`MixologyApp()` has 21 INFERRED edges - model-reasoned connections that need verification._