# Graph Report - Mixology  (2026-08-26)

## Corpus Check
- 135 files · ~87,447 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 971 nodes · 1806 edges · 60 communities (48 shown, 12 thin omitted)
- Extraction: 77% EXTRACTED · 23% INFERRED · 0% AMBIGUOUS · INFERRED: 416 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3e1d6c73`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- AnalyticsTracker
- DrinkDao
- Measures
- Drink
- HotViewModel
- openDrinkIntent
- SearchViewModel
- MixologyApp
- BarViewModel
- cocktailDrink
- MixologyDatabase
- CatalogViewModel
- HotViewModelTest.kt
- CocktailURLs.java
- ShoppingViewModel
- Overlay
- MainViewModel
- ThemeMode
- DrinkGridViewModelTest.kt
- DrinkGridViewModel
- fetch_random_drink.py
- RandomixerViewModel
- Mixology
- IngredientMeasure
- CocktailDbResponse
- fetch_randomixer_csv.py
- FakeCocktailService
- gradlew
- DrinkRepository
- DrinkDetailsViewModelTest.kt
- custom.md
- BarViewModelTest.kt
- BarMatcher
- NetworkModule
- MainDispatcherRule
- CocktailDbDrink
- BarMatcherTest
- Play Store listing
- RandomixerViewModelTest.kt
- DrinkDetailsIntent
- MviViewModel
- MixologyDatabaseMigrationTest
- ActivityMainEspressoTest
- SwipeableDrinkCard
- DrinkViewPreferences
- .sendEffect
- RandomixerIntent
- instructionSteps
- IngredientListConverterTest
- DrinkRecipeBody
- IntentExtrasTest
- IngredientRow
- NetworkMonitorTest

## God Nodes (most connected - your core abstractions)
1. `Drink` - 77 edges
2. `MixologyTheme()` - 63 edges
3. `DrinkRepository` - 42 edges
4. `DrinkDao` - 33 edges
5. `CocktailDbResponse` - 31 edges
6. `cocktailDrink()` - 30 edges
7. `FakeCocktailService` - 27 edges
8. `MixologyApp()` - 25 edges
9. `SearchScreen()` - 21 edges
10. `IngredientMeasure` - 20 edges

## Surprising Connections (you probably didn't know these)
- `setUp()` --calls--> `FakeCocktailService`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/hot/HotViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `setUp()` --calls--> `FakeCocktailService`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/randomixer/RandomixerViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `openDrink_emitsEffect()` --calls--> `cocktailDrink()`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `catalog()` --calls--> `CatalogListResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt
- `addToShoppingList_insertsIngredients()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt

## Import Cycles
- None detected.

## Communities (60 total, 12 thin omitted)

### Community 0 - "AnalyticsTracker"
Cohesion: 0.08
Nodes (15): analyticsScreenName(), AnalyticsTracker, firebaseAnalyticsOrNull(), forTests(), Context, LoggedEvent, AppEntryPoint, MyApplication (+7 more)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (11): CatalogSeed, CatalogSeedDrink, CatalogSeedPayload, Context, CatalogTermEntity, DrinkDao, Flow, DrinkEntity (+3 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "Drink"
Cohesion: 0.05
Nodes (34): Drink, DrinkCard(), Dp, Modifier, DrinkListItem(), DrinkViewToggle(), FavoriteButton(), Modifier (+26 more)

### Community 4 - "HotViewModel"
Cohesion: 0.11
Nodes (20): recordCrash(), HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter (+12 more)

### Community 5 - "openDrinkIntent"
Cohesion: 0.13
Nodes (16): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+8 more)

### Community 6 - "SearchViewModel"
Cohesion: 0.08
Nodes (24): Back, filterIngredientSuggestions(), NavigateBack, OpenDrink, Search, SearchEffect, SearchIntent, SearchMode (+16 more)

### Community 7 - "MixologyApp"
Cohesion: 0.05
Nodes (35): ActivityMain, Intent, BannerAd(), bannerAdUnitRes(), Modifier, Bar, Catalog, detailsRoute() (+27 more)

### Community 8 - "BarViewModel"
Cohesion: 0.09
Nodes (24): BarEffect, BarIntent, BarUiState, ClosePicker, filterBarTerms(), Load, OpenDrink, OpenPicker (+16 more)

### Community 10 - "MixologyDatabase"
Cohesion: 0.07
Nodes (13): BarDao, Flow, BarIngredientEntity, create(), importLegacySavedDrinks(), Context, MixologyDatabase, Flow (+5 more)

### Community 11 - "CatalogViewModel"
Cohesion: 0.08
Nodes (24): CatalogEffect, CatalogIntent, CatalogUiState, filterCatalogTerms(), Load, OpenFilter, OpenTerm, QueryChanged (+16 more)

### Community 12 - "HotViewModelTest.kt"
Cohesion: 0.39
Nodes (7): awaitItemUntil(), createViewModel(), T, load_fallsBackToPresetDrinkTypesWhenCatalogEmpty(), load_usesDrinkTypeCatalogTerms(), save_addsRecentlyViewedRowFirst(), setUp()

### Community 19 - "ShoppingViewModel"
Cohesion: 0.12
Nodes (14): ClearChecked, Remove, ShoppingEffect, ShoppingIntent, ShoppingUiState, Toggle, ShoppingRoute(), ShoppingScreen() (+6 more)

### Community 20 - "Overlay"
Cohesion: 0.18
Nodes (9): Activity, Animator, Overlay, AnimatorListenerAdapter, SaveConfetti, Spark, Canvas, MotionEvent (+1 more)

### Community 21 - "MainViewModel"
Cohesion: 0.12
Nodes (13): DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails, OpenMenu (+5 more)

### Community 22 - "ThemeMode"
Cohesion: 0.17
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 23 - "DrinkGridViewModelTest.kt"
Cohesion: 0.27
Nodes (7): awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, loadedDrink(), openDrink_emitsEffect(), setUp(), toggleSaved_savesDrinkWithoutMessage()

### Community 24 - "DrinkGridViewModel"
Cohesion: 0.16
Nodes (12): Bind, DrinkGridEffect, DrinkGridIntent, DrinkGridUiState, OpenDrink, ShowMessage, ShowMessageRes, ToggleListView (+4 more)

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.16
Nodes (23): datetime, Path, blank(), connect(), drink_payload(), main(), parse_args(), Namespace (+15 more)

### Community 26 - "RandomixerViewModel"
Cohesion: 0.29
Nodes (5): Discarded, Job, RandomixerViewModel, Saved, UndoAction

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "IngredientMeasure"
Cohesion: 0.24
Nodes (3): IngredientListConverter, IngredientMeasure, IngredientMeasureTest

### Community 29 - "CocktailDbResponse"
Cohesion: 0.21
Nodes (3): CocktailService, CatalogListResponse, CocktailDbResponse

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.32
Nodes (11): Exception, cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main(), parse_args() (+3 more)

### Community 31 - "FakeCocktailService"
Cohesion: 0.10
Nodes (5): CatalogListItem, catalog(), FakeCocktailService, setUp(), setUp()

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 33 - "DrinkRepository"
Cohesion: 0.06
Nodes (12): ShoppingItemEntity, WidgetEntryPoint, DrinkRepository, FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT (+4 more)

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.29
Nodes (7): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), reloadSameDrink_logsViewOnce(), share_logsShareEvent(), toggleSaved_logsWishlistEvents()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 36 - "BarViewModelTest.kt"
Cohesion: 0.33
Nodes (8): awaitItemUntil(), T, load_emptyBarHasNoRecommendations(), martini(), negroni(), toggleIngredient_addsAndRemovesBarItem(), toggleSaved_logsWishlistEvents(), togglingIngredients_splitsMakeableAndAlmost()

### Community 39 - "BarMatcher"
Cohesion: 0.38
Nodes (3): BarAlmostDrink, BarMatcher, BarRecommendations

### Community 40 - "NetworkModule"
Cohesion: 0.40
Nodes (3): Context, NetworkModule, OkHttpClient

### Community 41 - "MainDispatcherRule"
Cohesion: 0.40
Nodes (3): MainDispatcherRule, Description, TestWatcher

### Community 42 - "CocktailDbDrink"
Cohesion: 0.12
Nodes (3): CocktailDbDrink, DrinkFilterTest, CocktailDbDrinkTest

### Community 44 - "Play Store listing"
Cohesion: 0.40
Nodes (4): English, Español, Play Store listing, Screenshot checklist

### Community 45 - "RandomixerViewModelTest.kt"
Cohesion: 0.40
Nodes (8): awaitItemUntil(), createViewModel(), T, load_usesLocalRecipesWithoutHittingEndpoint(), seedRecipes(), setUp(), swipeDiscard_logsSkipEvent(), swipeSave_logsWishlistEvent()

### Community 46 - "DrinkDetailsIntent"
Cohesion: 0.14
Nodes (13): AddToShoppingList, Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, OpenUrl, OpenVideo (+5 more)

### Community 47 - "MviViewModel"
Cohesion: 0.26
Nodes (11): CollectMviEffects(), Flow, StateFlow, MviStore, MviViewModel, SnackbarHostState, RandomixerRoute(), E (+3 more)

### Community 50 - "SwipeableDrinkCard"
Cohesion: 0.44
Nodes (8): ActionCircleButton(), DrinkSwipeDetails(), DrinkSwipePhoto(), Modifier, OverlayActionButtons(), SwipeableDrinkCard(), SwipeStamp(), Color

### Community 53 - "RandomixerIntent"
Cohesion: 0.20
Nodes (9): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, ShowUndo, SwipeDiscard, SwipeSave, ToggleHideSaved (+1 more)

### Community 56 - "DrinkRecipeBody"
Cohesion: 0.29
Nodes (9): DrinkImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock(), Modifier, RecipeChip(), RecipeSectionTitle() (+1 more)

## Knowledge Gaps
- **88 isolated node(s):** `CocktailURLs`, `CatalogSeedDrink`, `ALCOHOL`, `GLASS`, `INGREDIENT` (+83 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRepository`, `DrinkDetailsViewModelTest.kt`, `HotViewModel`, `openDrinkIntent`, `SearchViewModel`, `BarMatcher`, `BarViewModel`, `MixologyApp`, `CocktailDbDrink`, `BarMatcherTest`, `BarViewModelTest.kt`, `ActivityMainEspressoTest`, `SwipeableDrinkCard`, `.sendEffect`, `MainViewModel`, `DrinkGridViewModel`, `IntentExtrasTest`, `RandomixerViewModel`?**
  _High betweenness centrality (0.318) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `AnalyticsTracker`, `Drink`, `HotViewModel`, `SearchViewModel`, `BarViewModel`, `CatalogViewModel`, `MviViewModel`, `ShoppingViewModel`, `MainViewModel`, `ThemeMode`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Why does `DrinkRepository` connect `DrinkRepository` to `MixologyDatabase`, `CatalogViewModel`, `HotViewModelTest.kt`, `RandomixerViewModelTest.kt`, `ShoppingViewModel`, `DrinkGridViewModelTest.kt`, `FakeCocktailService`?**
  _High betweenness centrality (0.103) - this node is a cross-community bridge._
- **Are the 34 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.drinkClick_reportsMakeableCocktail()`) actually correct?**
  _`Drink` has 34 INFERRED edges - model-reasoned connections that need verification._
- **Are the 62 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 62 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `DrinkRepository` (e.g. with `setUp()` and `queryChanged_filtersTermsAcrossMultipleSearches()`) actually correct?**
  _`DrinkRepository` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 19 inferred relationships involving `CocktailDbResponse` (e.g. with `.fetchAndCache_observesFilterResultsAndSkipsBadThumbs()` and `.fetchAndCache_replacesPreviousFilterMemberships()`) actually correct?**
  _`CocktailDbResponse` has 19 INFERRED edges - model-reasoned connections that need verification._