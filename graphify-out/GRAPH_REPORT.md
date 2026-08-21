# Graph Report - Mixology  (2026-08-21)

## Corpus Check
- 126 files · ~84,839 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 889 nodes · 1629 edges · 47 communities (43 shown, 4 thin omitted)
- Extraction: 78% EXTRACTED · 22% INFERRED · 0% AMBIGUOUS · INFERRED: 359 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e50b8912`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DrinkDetailsContent
- DrinkDao
- Measures
- Drink
- HotViewModel
- WidgetContent
- MixologyApp
- DrawerDestination
- BarViewModel
- cocktailDrink
- MixologyDatabase
- MviViewModel
- HotViewModelTest.kt
- CocktailURLs.java
- ShoppingViewModel
- NetworkMonitor
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
- CocktailDbDrink
- BarMatcher
- NetworkModule
- MainDispatcherRule
- Play Store listing
- MixologyDatabaseMigrationTest
- CatalogListItem
- DrinkViewPreferences
- CatalogViewModelTest.kt

## God Nodes (most connected - your core abstractions)
1. `Drink` - 72 edges
2. `MixologyTheme()` - 51 edges
3. `DrinkRepository` - 41 edges
4. `DrinkDao` - 33 edges
5. `CocktailDbResponse` - 28 edges
6. `FakeCocktailService` - 26 edges
7. `cocktailDrink()` - 25 edges
8. `MixologyApp()` - 23 edges
9. `SearchScreen()` - 21 edges
10. `MviViewModel` - 19 edges

## Surprising Connections (you probably didn't know these)
- `setUp()` --calls--> `FakeCocktailService`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/hot/HotViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `setUp()` --calls--> `FakeCocktailService`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/randomixer/RandomixerViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `openDrink_emitsEffect()` --calls--> `cocktailDrink()`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `catalog()` --calls--> `CatalogListResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt
- `catalog()` --calls--> `CatalogListItem`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt

## Import Cycles
- None detected.

## Communities (47 total, 4 thin omitted)

### Community 0 - "DrinkDetailsContent"
Cohesion: 0.24
Nodes (9): DrinkDetailsUiState, DrinkDetailsContent(), DrinkDetailsRoute(), DrinkDetailsScaffold(), FullscreenDrinkImage(), HeroActionButton(), SnackbarHostState, DrinkDetailsScreenTest (+1 more)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (11): CatalogSeed, CatalogSeedDrink, CatalogSeedPayload, Context, CatalogTermEntity, DrinkDao, Flow, DrinkEntity (+3 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "Drink"
Cohesion: 0.05
Nodes (27): ActivityMainEspressoTest, Drink, DrinkCard(), Dp, Modifier, DrinkListItem(), DrinkViewToggle(), FavoriteButton() (+19 more)

### Community 4 - "HotViewModel"
Cohesion: 0.11
Nodes (20): recordCrash(), HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter (+12 more)

### Community 5 - "WidgetContent"
Cohesion: 0.16
Nodes (15): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+7 more)

### Community 6 - "MixologyApp"
Cohesion: 0.05
Nodes (42): ActivityMain, Intent, FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, detailsRoute() (+34 more)

### Community 7 - "DrawerDestination"
Cohesion: 0.15
Nodes (13): Bar, Catalog, DrawerDestination, Filter, Hot, Randomixer, Settings, Shopping (+5 more)

### Community 8 - "BarViewModel"
Cohesion: 0.07
Nodes (28): BarEffect, BarIntent, BarUiState, ClosePicker, filterBarTerms(), Load, OpenDrink, OpenPicker (+20 more)

### Community 9 - "cocktailDrink"
Cohesion: 0.13
Nodes (3): cocktailDrink(), DrinkRepositoryTest, SearchViewModelTest

### Community 10 - "MixologyDatabase"
Cohesion: 0.07
Nodes (13): BarDao, Flow, BarIngredientEntity, create(), importLegacySavedDrinks(), Context, MixologyDatabase, Flow (+5 more)

### Community 11 - "MviViewModel"
Cohesion: 0.05
Nodes (42): CatalogEffect, CatalogIntent, CatalogUiState, filterCatalogTerms(), Load, OpenFilter, OpenTerm, QueryChanged (+34 more)

### Community 12 - "HotViewModelTest.kt"
Cohesion: 0.39
Nodes (7): awaitItemUntil(), createViewModel(), T, load_fallsBackToPresetDrinkTypesWhenCatalogEmpty(), load_usesDrinkTypeCatalogTerms(), save_addsRecentlyViewedRowFirst(), setUp()

### Community 19 - "ShoppingViewModel"
Cohesion: 0.18
Nodes (10): ClearChecked, Remove, ShoppingEffect, ShoppingIntent, ShoppingUiState, Toggle, ShoppingRoute(), ShoppingScreen() (+2 more)

### Community 20 - "NetworkMonitor"
Cohesion: 0.08
Nodes (16): Activity, Animator, AppEntryPoint, MyApplication, forTests(), StateFlow, NetworkMonitor, Overlay (+8 more)

### Community 21 - "MainViewModel"
Cohesion: 0.12
Nodes (13): DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails, OpenMenu (+5 more)

### Community 22 - "ThemeMode"
Cohesion: 0.18
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 23 - "DrinkGridViewModelTest.kt"
Cohesion: 0.28
Nodes (6): awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, openDrink_emitsEffect(), setUp(), toggleSaved_savesDrinkWithoutMessage()

### Community 24 - "DrinkGridViewModel"
Cohesion: 0.16
Nodes (12): Bind, DrinkGridEffect, DrinkGridIntent, DrinkGridUiState, OpenDrink, ShowMessage, ShowMessageRes, ToggleListView (+4 more)

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.16
Nodes (23): datetime, Path, blank(), connect(), drink_payload(), main(), parse_args(), Namespace (+15 more)

### Community 26 - "RandomixerViewModel"
Cohesion: 0.09
Nodes (21): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, ShowUndo, SwipeDiscard, SwipeSave, ToggleHideSaved (+13 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "IngredientMeasure"
Cohesion: 0.06
Nodes (32): IngredientListConverter, DrinkImage(), Modifier, DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock(), instructionSteps(), Modifier (+24 more)

### Community 29 - "CocktailDbResponse"
Cohesion: 0.21
Nodes (3): CocktailService, CatalogListResponse, CocktailDbResponse

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.32
Nodes (11): Exception, cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main(), parse_args() (+3 more)

### Community 31 - "FakeCocktailService"
Cohesion: 0.12
Nodes (4): catalog(), FakeCocktailService, setUp(), setUp()

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 33 - "DrinkRepository"
Cohesion: 0.08
Nodes (6): ShoppingItemEntity, WidgetEntryPoint, DrinkRepository, hasUsableThumb(), DrinkFilter, Flow

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.32
Nodes (5): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), setUp()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 36 - "CocktailDbDrink"
Cohesion: 0.12
Nodes (3): CocktailDbDrink, DrinkFilterTest, CocktailDbDrinkTest

### Community 39 - "BarMatcher"
Cohesion: 0.38
Nodes (3): BarAlmostDrink, BarMatcher, BarRecommendations

### Community 40 - "NetworkModule"
Cohesion: 0.40
Nodes (3): Context, NetworkModule, OkHttpClient

### Community 41 - "MainDispatcherRule"
Cohesion: 0.40
Nodes (3): MainDispatcherRule, Description, TestWatcher

### Community 44 - "Play Store listing"
Cohesion: 0.40
Nodes (4): English, Español, Play Store listing, Screenshot checklist

### Community 55 - "CatalogViewModelTest.kt"
Cohesion: 0.36
Nodes (5): awaitItemUntil(), T, load_populatesIngredientTerms(), queryChanged_filtersTermsAcrossMultipleSearches(), selectSameKind_doesNotClearQuery()

## Knowledge Gaps
- **88 isolated node(s):** `CocktailURLs`, `CatalogSeedDrink`, `ALCOHOL`, `GLASS`, `INGREDIENT` (+83 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkDetailsContent`, `DrinkRepository`, `DrinkDetailsViewModelTest.kt`, `CocktailDbDrink`, `HotViewModel`, `MixologyApp`, `BarMatcher`, `BarViewModel`, `WidgetContent`, `MviViewModel`, `MainViewModel`, `DrinkGridViewModel`, `RandomixerViewModel`, `IngredientMeasure`?**
  _High betweenness centrality (0.302) - this node is a cross-community bridge._
- **Why does `DrinkRepository` connect `DrinkRepository` to `DrinkDetailsViewModelTest.kt`, `MixologyApp`, `cocktailDrink`, `MixologyDatabase`, `HotViewModelTest.kt`, `DrinkGridViewModelTest.kt`, `CatalogViewModelTest.kt`, `RandomixerViewModel`, `FakeCocktailService`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `Drink` to `DrinkDetailsContent`, `HotViewModel`, `MixologyApp`, `DrawerDestination`, `BarViewModel`, `MviViewModel`, `ShoppingViewModel`, `ThemeMode`?**
  _High betweenness centrality (0.083) - this node is a cross-community bridge._
- **Are the 29 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.drinkClick_reportsMakeableCocktail()`) actually correct?**
  _`Drink` has 29 INFERRED edges - model-reasoned connections that need verification._
- **Are the 50 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 50 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `DrinkRepository` (e.g. with `setUp()` and `queryChanged_filtersTermsAcrossMultipleSearches()`) actually correct?**
  _`DrinkRepository` has 8 INFERRED edges - model-reasoned connections that need verification._
- **Are the 16 inferred relationships involving `CocktailDbResponse` (e.g. with `.fetchAndCache_observesFilterResultsAndSkipsBadThumbs()` and `.fetchAndCache_replacesPreviousFilterMemberships()`) actually correct?**
  _`CocktailDbResponse` has 16 INFERRED edges - model-reasoned connections that need verification._