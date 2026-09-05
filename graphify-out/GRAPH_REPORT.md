# Graph Report - Mixology  (2026-09-05)

## Corpus Check
- 148 files · ~90,753 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1090 nodes · 1976 edges · 73 communities (57 shown, 16 thin omitted)
- Extraction: 79% EXTRACTED · 21% INFERRED · 0% AMBIGUOUS · INFERRED: 411 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e60bb47c`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AnalyticsTracker
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- openDrinkIntent
- SearchViewModel
- MixologyApp
- BarViewModel
- cocktailDrink
- MixologyDatabase
- CocktailDbDrink
- Drink
- CocktailURLs.java
- IntentExtras.kt
- ShoppingDao
- Overlay
- MainViewModel
- ThemeMode
- PlayUpdateController
- DrinkGridViewModel
- fetch_random_drink.py
- RandomixerViewModel
- Mixology
- DrinkDetailsIntent
- FilterKind
- fetch_randomixer_csv.py
- CocktailService
- gradlew
- DrinkRepository
- DrinkDetailsViewModelTest.kt
- custom.md
- PlayReviewClient
- BarMatcher
- .setState
- MainDispatcherRule
- IngredientMeasure
- BarMatcherTest
- Play Store listing
- .sendEffect
- SharedPrefsReviewPromptStore
- MviViewModel
- MixologyDatabaseMigrationTest
- ActivityMainEspressoTest
- MemoryReviewPromptStore
- CatalogContract.kt
- MixologyBottomBar
- PlayReviewController
- DrinkDetailsContent
- BannerAd
- DrawerDestination
- IntentExtrasTest
- BarViewModelTest.kt
- CatalogViewModelTest.kt
- ActivityMain
- NetworkMonitorTest
- PlayProvidesModule
- Flow
- AppEntryPoint
- ReviewPromptState
- openPlayStoreListing
- DrawerDestinationTest
- SwipeableDrinkCard
- DrinkFilterTest
- DrinkViewPreferences
- ReviewPromptStore
- IngredientListConverterTest

## God Nodes (most connected - your core abstractions)
1. `Drink` - 80 edges
2. `MixologyTheme()` - 64 edges
3. `DrinkRepository` - 46 edges
4. `DrinkDao` - 33 edges
5. `MixologyApp()` - 26 edges
6. `cocktailDrink()` - 26 edges
7. `IngredientMeasure` - 22 edges
8. `SearchScreen()` - 21 edges
9. `DrinkEntity` - 19 edges
10. `MviViewModel` - 19 edges

## Surprising Connections (you probably didn't know these)
- `reloadSameDrink_logsViewOnce()` --calls--> `Drink`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/data/Drink.kt
- `MixologyApp()` --calls--> `analyticsScreenName()`  [INFERRED]
  app/src/main/java/com/capstone/nik/mixology/ui/main/MixologyApp.kt → app/src/main/java/com/capstone/nik/mixology/analytics/AnalyticsTracker.kt
- `drinkExtra()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/ui/IntentExtras.kt → app/src/main/java/com/capstone/nik/mixology/data/Drink.kt
- `martini()` --calls--> `Drink`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/bar/BarViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/data/Drink.kt
- `negroni()` --calls--> `Drink`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/bar/BarViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/data/Drink.kt

## Import Cycles
- None detected.

## Communities (73 total, 16 thin omitted)

### Community 0 - "AnalyticsTracker"
Cohesion: 0.07
Nodes (21): analyticsScreenName(), AnalyticsTracker, firebaseAnalyticsOrNull(), forTests(), Context, LoggedEvent, MyApplication, forTests() (+13 more)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (11): CatalogSeed, CatalogSeedDrink, CatalogSeedPayload, Context, CatalogTermEntity, DrinkDao, Flow, DrinkEntity (+3 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.07
Nodes (26): CatalogUiState, CatalogRoute(), CatalogScreen(), CatalogTermRow(), OfflineBanner(), ScreenHeader(), RandomixerUiState, RandomixerScreen() (+18 more)

### Community 4 - "HotViewModel"
Cohesion: 0.11
Nodes (20): recordCrash(), HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter (+12 more)

### Community 5 - "openDrinkIntent"
Cohesion: 0.13
Nodes (16): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+8 more)

### Community 6 - "SearchViewModel"
Cohesion: 0.12
Nodes (14): Back, filterIngredientSuggestions(), NavigateBack, OpenDrink, Search, SearchEffect, SearchIntent, SelectSuggestion (+6 more)

### Community 7 - "MixologyApp"
Cohesion: 0.31
Nodes (13): detailsRoute(), encodeRouteArg(), filterKindFromRoute(), gridRoute(), isDetailsRoute(), isOverlayRoute(), isSearchRoute(), DrinkFilter (+5 more)

### Community 8 - "BarViewModel"
Cohesion: 0.08
Nodes (25): BarEffect, BarIntent, BarUiState, ClosePicker, filterBarTerms(), Load, OpenDrink, OpenPicker (+17 more)

### Community 9 - "cocktailDrink"
Cohesion: 0.07
Nodes (16): cocktailDrink(), DrinkRepositoryTest, awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, loadedDrink(), openDrink_emitsEffect(), setUp() (+8 more)

### Community 10 - "MixologyDatabase"
Cohesion: 0.09
Nodes (11): BarDao, Flow, BarIngredientEntity, create(), importLegacySavedDrinks(), Context, MixologyDatabase, DatabaseModule (+3 more)

### Community 12 - "Drink"
Cohesion: 0.17
Nodes (7): Drink, DrinkListItem(), DrinkGridRoute(), DrinkGridScreen(), DrinkFilter, SnackbarHostState, DrinkGridScreenTest

### Community 18 - "IntentExtras.kt"
Cohesion: 0.40
Nodes (4): drinkExtra(), DrinkIntents, Intent, putDrinkExtra()

### Community 19 - "ShoppingDao"
Cohesion: 0.07
Nodes (17): Flow, ShoppingDao, ShoppingItemEntity, ClearChecked, Remove, ShoppingEffect, ShoppingIntent, ShoppingUiState (+9 more)

### Community 20 - "Overlay"
Cohesion: 0.18
Nodes (9): Animator, Activity, Overlay, AnimatorListenerAdapter, SaveConfetti, Spark, Canvas, MotionEvent (+1 more)

### Community 21 - "MainViewModel"
Cohesion: 0.12
Nodes (14): DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails, OpenMenu (+6 more)

### Community 22 - "ThemeMode"
Cohesion: 0.16
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, SharedPreferences, rememberThemeMode(), ThemeMode, DARK (+4 more)

### Community 23 - "PlayUpdateController"
Cohesion: 0.10
Nodes (15): Activity, SharedFlow, PlayUpdateController, PlayUpdateEvent, Downloaded, UpToDate, PlayUpdateAction, COMPLETE (+7 more)

### Community 24 - "DrinkGridViewModel"
Cohesion: 0.16
Nodes (12): Bind, DrinkGridEffect, DrinkGridIntent, DrinkGridUiState, OpenDrink, ShowMessage, ShowMessageRes, ToggleListView (+4 more)

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.13
Nodes (30): datetime, Path, adb(), dump_xml(), main(), parse_bounds(), shot(), tap() (+22 more)

### Community 26 - "RandomixerViewModel"
Cohesion: 0.13
Nodes (16): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, ShowUndo, SwipeDiscard, SwipeSave, ToggleHideSaved (+8 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "DrinkDetailsIntent"
Cohesion: 0.14
Nodes (13): AddToShoppingList, Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, OpenUrl, OpenVideo (+5 more)

### Community 29 - "FilterKind"
Cohesion: 0.18
Nodes (8): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, hasUsableThumb(), DrinkFilter, matchesFilterValue()

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.32
Nodes (11): Exception, cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main(), parse_args() (+3 more)

### Community 31 - "CocktailService"
Cohesion: 0.07
Nodes (9): Context, NetworkModule, CocktailService, CatalogListItem, CatalogListResponse, CocktailDbResponse, catalog(), FakeCocktailService (+1 more)

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.29
Nodes (9): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), reloadSameDrink_logsViewOnce(), seedMargarita(), setUp(), share_logsShareEvent() (+1 more)

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 36 - "PlayReviewClient"
Cohesion: 0.21
Nodes (6): PlayBindsModule, GooglePlayReviewClient, Activity, Activity, NoOpPlayReviewClient, PlayReviewClient

### Community 39 - "BarMatcher"
Cohesion: 0.38
Nodes (3): BarAlmostDrink, BarMatcher, BarRecommendations

### Community 40 - ".setState"
Cohesion: 0.24
Nodes (7): CatalogIntent, Load, OpenTerm, QueryChanged, SelectKind, CatalogViewModel, Job

### Community 41 - "MainDispatcherRule"
Cohesion: 0.40
Nodes (3): MainDispatcherRule, Description, TestWatcher

### Community 42 - "IngredientMeasure"
Cohesion: 0.20
Nodes (5): IngredientListConverter, IngredientRow(), Modifier, IngredientMeasure, IngredientMeasureTest

### Community 44 - "Play Store listing"
Cohesion: 0.40
Nodes (4): English, Español, Play Store listing, Screenshot checklist

### Community 46 - "SharedPrefsReviewPromptStore"
Cohesion: 0.24
Nodes (3): SharedPreferences, SharedPrefsReviewPromptStore, SharedPrefsReviewPromptStoreTest

### Community 47 - "MviViewModel"
Cohesion: 0.33
Nodes (9): CollectMviEffects(), Flow, StateFlow, MviStore, MviViewModel, E, I, S (+1 more)

### Community 50 - "MemoryReviewPromptStore"
Cohesion: 0.27
Nodes (3): MemoryReviewPromptStore, PlayReviewControllerTest, RecordingReviewClient

### Community 51 - "CatalogContract.kt"
Cohesion: 0.28
Nodes (6): CatalogEffect, filterCatalogTerms(), OpenFilter, withCatalogTerms(), withQuery(), CatalogContractTest

### Community 52 - "MixologyBottomBar"
Cohesion: 0.33
Nodes (5): BottomNavItem, isBottomNavTab(), isSelectedBottomNav(), MixologyBottomBar(), MixologyBottomBarTest

### Community 53 - "PlayReviewController"
Cohesion: 0.25
Nodes (4): disabled(), Activity, SharedFlow, PlayReviewController

### Community 54 - "DrinkDetailsContent"
Cohesion: 0.08
Nodes (25): DrinkCard(), Dp, Modifier, DrinkImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock() (+17 more)

### Community 55 - "BannerAd"
Cohesion: 0.32
Nodes (4): BannerAd(), bannerAdUnitRes(), Modifier, BannerAdTest

### Community 56 - "DrawerDestination"
Cohesion: 0.25
Nodes (8): Bar, Catalog, DrawerDestination, Filter, Hot, Randomixer, Settings, Shopping

### Community 58 - "BarViewModelTest.kt"
Cohesion: 0.33
Nodes (8): awaitItemUntil(), T, load_emptyBarHasNoRecommendations(), martini(), negroni(), toggleIngredient_addsAndRemovesBarItem(), toggleSaved_logsWishlistEvents(), togglingIngredients_splitsMakeableAndAlmost()

### Community 59 - "CatalogViewModelTest.kt"
Cohesion: 0.31
Nodes (7): awaitItemUntil(), T, load_populatesIngredientTerms(), openTerm_emitsDynamicFilter(), queryChanged_filtersTermsAcrossMultipleSearches(), selectSameKind_doesNotClearQuery(), setUp()

### Community 60 - "ActivityMain"
Cohesion: 0.29
Nodes (4): ActivityMain, Intent, AppCompatActivity, Bundle

### Community 62 - "PlayProvidesModule"
Cohesion: 0.33
Nodes (4): Context, PlayProvidesModule, AppUpdateManager, ReviewManager

### Community 66 - "openPlayStoreListing"
Cohesion: 0.22
Nodes (5): findActivity(), Activity, Context, openPlayStoreListing(), PlayStoreIntentsTest

### Community 68 - "SwipeableDrinkCard"
Cohesion: 0.44
Nodes (8): ActionCircleButton(), DrinkSwipeDetails(), DrinkSwipePhoto(), Modifier, OverlayActionButtons(), SwipeableDrinkCard(), SwipeStamp(), Color

### Community 70 - "DrinkViewPreferences"
Cohesion: 0.40
Nodes (3): DrinkViewPreferences, DrinkViewToggle(), Context

## Knowledge Gaps
- **93 isolated node(s):** `CocktailURLs`, `CatalogSeedDrink`, `Downloaded`, `UpToDate`, `FLEXIBLE` (+88 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **16 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `MixologyTheme`, `HotViewModel`, `openDrinkIntent`, `SearchViewModel`, `MixologyApp`, `BarViewModel`, `cocktailDrink`, `CocktailDbDrink`, `IntentExtras.kt`, `MainViewModel`, `DrinkGridViewModel`, `RandomixerViewModel`, `FilterKind`, `DrinkRepository`, `DrinkDetailsViewModelTest.kt`, `BarMatcher`, `BarMatcherTest`, `.sendEffect`, `ActivityMainEspressoTest`, `DrinkDetailsContent`, `IntentExtrasTest`, `BarViewModelTest.kt`, `Flow`, `SwipeableDrinkCard`?**
  _High betweenness centrality (0.313) - this node is a cross-community bridge._
- **Why does `DrinkRepository` connect `DrinkRepository` to `AnalyticsTracker`, `DrinkDetailsViewModelTest.kt`, `BarViewModel`, `cocktailDrink`, `MixologyDatabase`, `ShoppingDao`, `CatalogViewModelTest.kt`, `FilterKind`, `Flow`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `AnalyticsTracker`, `openPlayStoreListing`, `MixologyTheme`, `HotViewModel`, `BarViewModel`, `Drink`, `MviViewModel`, `ShoppingDao`, `MixologyBottomBar`, `MainViewModel`, `DrinkDetailsContent`, `BannerAd`, `ThemeMode`, `RandomixerViewModel`, `ActivityMain`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **Are the 35 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.drinkClick_reportsMakeableCocktail()`) actually correct?**
  _`Drink` has 35 INFERRED edges - model-reasoned connections that need verification._
- **Are the 63 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 63 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `DrinkRepository` (e.g. with `setUp()` and `queryChanged_filtersTermsAcrossMultipleSearches()`) actually correct?**
  _`DrinkRepository` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 21 inferred relationships involving `MixologyApp()` (e.g. with `.onCreate()` and `analyticsScreenName()`) actually correct?**
  _`MixologyApp()` has 21 INFERRED edges - model-reasoned connections that need verification._