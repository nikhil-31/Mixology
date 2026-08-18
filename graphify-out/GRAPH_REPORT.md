# Graph Report - Mixology  (2026-08-18)

## Corpus Check
- 124 files · ~75,976 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 861 nodes · 1560 edges · 53 communities (42 shown, 11 thin omitted)
- Extraction: 79% EXTRACTED · 21% INFERRED · 0% AMBIGUOUS · INFERRED: 322 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `d958337f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DrinkRecipeBody
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- WidgetContent
- SearchViewModel
- MixologyApp
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
- Drink
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
- FilterKind
- BarMatcher
- NetworkModule
- MainDispatcherRule
- SearchViewModelTest
- Flow
- Play Store listing
- ShoppingItemEntity
- IntentExtrasTest
- ActivityMainEspressoTest
- MixologyDatabaseMigrationTest
- WidgetEntryPoint
- catalog
- CocktailDbDrink
- CatalogViewModel

## God Nodes (most connected - your core abstractions)
1. `Drink` - 67 edges
2. `MixologyTheme()` - 44 edges
3. `DrinkRepository` - 41 edges
4. `DrinkDao` - 33 edges
5. `CocktailDbResponse` - 26 edges
6. `FakeCocktailService` - 26 edges
7. `MixologyApp()` - 23 edges
8. `cocktailDrink()` - 23 edges
9. `MviViewModel` - 19 edges
10. `IngredientMeasure` - 18 edges

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

## Communities (53 total, 11 thin omitted)

### Community 0 - "DrinkRecipeBody"
Cohesion: 0.12
Nodes (17): DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock(), instructionSteps(), Modifier, RecipeChip(), RecipeSectionTitle(), DrinkDetailsUiState (+9 more)

### Community 1 - "DrinkDao"
Cohesion: 0.08
Nodes (11): CatalogSeed, CatalogSeedDrink, CatalogSeedPayload, Context, CatalogTermEntity, DrinkDao, Flow, DrinkEntity (+3 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.14
Nodes (10): DrinkGridRoute(), DrinkGridScreen(), DrinkFilter, SnackbarHostState, RandomixerUiState, RandomixerScreen(), MixologyTheme(), DrinkGridScreenTest (+2 more)

### Community 4 - "HotViewModel"
Cohesion: 0.11
Nodes (20): recordCrash(), HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter (+12 more)

### Community 5 - "WidgetContent"
Cohesion: 0.16
Nodes (15): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+7 more)

### Community 6 - "SearchViewModel"
Cohesion: 0.10
Nodes (22): Back, NavigateBack, OpenDrink, Search, SearchEffect, SearchIntent, SearchMode, INGREDIENT (+14 more)

### Community 7 - "MixologyApp"
Cohesion: 0.07
Nodes (32): ActivityMain, Intent, Bar, Catalog, detailsRoute(), DrawerDestination, encodeRouteArg(), Filter (+24 more)

### Community 8 - "BarViewModel"
Cohesion: 0.07
Nodes (32): BarEffect, BarIntent, BarUiState, ClosePicker, filterBarTerms(), Load, OpenDrink, OpenPicker (+24 more)

### Community 10 - "MixologyDatabase"
Cohesion: 0.07
Nodes (13): BarDao, Flow, BarIngredientEntity, create(), importLegacySavedDrinks(), Context, MixologyDatabase, Flow (+5 more)

### Community 11 - "MviViewModel"
Cohesion: 0.06
Nodes (35): AddToShoppingList, Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, OpenUrl, OpenVideo (+27 more)

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
Cohesion: 0.13
Nodes (13): DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails, OpenMenu (+5 more)

### Community 22 - "ThemeMode"
Cohesion: 0.18
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 23 - "DrinkGridViewModelTest.kt"
Cohesion: 0.32
Nodes (6): awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, openDrink_emitsEffect(), setUp(), toggleSaved_savesDrinkWithoutMessage()

### Community 24 - "Drink"
Cohesion: 0.16
Nodes (5): Drink, drinkExtra(), DrinkIntents, Intent, putDrinkExtra()

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.16
Nodes (23): datetime, Path, blank(), connect(), drink_payload(), main(), parse_args(), Namespace (+15 more)

### Community 26 - "RandomixerViewModel"
Cohesion: 0.10
Nodes (19): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, ShowUndo, SwipeDiscard, SwipeSave, ToggleHideSaved (+11 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "IngredientMeasure"
Cohesion: 0.08
Nodes (22): IngredientListConverter, IngredientRow(), Modifier, ingredientImageUrl(), IngredientMeasure, ActionCircleButton(), DrinkSwipeDetails(), DrinkSwipePhoto() (+14 more)

### Community 29 - "CocktailDbResponse"
Cohesion: 0.21
Nodes (3): CocktailService, CatalogListResponse, CocktailDbResponse

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.32
Nodes (11): Exception, cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main(), parse_args() (+3 more)

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.32
Nodes (5): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), setUp()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 36 - "FilterKind"
Cohesion: 0.18
Nodes (7): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, hasUsableThumb(), DrinkFilter

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

### Community 51 - "catalog"
Cohesion: 0.29
Nodes (3): CatalogListItem, catalog(), setUp()

### Community 52 - "CocktailDbDrink"
Cohesion: 0.12
Nodes (3): CocktailDbDrink, DrinkFilterTest, CocktailDbDrinkTest

### Community 55 - "CatalogViewModel"
Cohesion: 0.08
Nodes (24): CatalogEffect, CatalogIntent, CatalogUiState, filterCatalogTerms(), Load, OpenFilter, OpenTerm, QueryChanged (+16 more)

## Knowledge Gaps
- **85 isolated node(s):** `CocktailURLs`, `CatalogSeedDrink`, `ALCOHOL`, `GLASS`, `INGREDIENT` (+80 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRecipeBody`, `DrinkDao`, `MixologyTheme`, `HotViewModel`, `WidgetContent`, `SearchViewModel`, `MixologyApp`, `BarViewModel`, `MviViewModel`, `MainViewModel`, `RandomixerViewModel`, `IngredientMeasure`, `DrinkRepository`, `DrinkDetailsViewModelTest.kt`, `FilterKind`, `BarMatcher`, `Flow`, `IntentExtrasTest`, `ActivityMainEspressoTest`, `CocktailDbDrink`?**
  _High betweenness centrality (0.300) - this node is a cross-community bridge._
- **Why does `DrinkRepository` connect `DrinkRepository` to `DrinkDetailsViewModelTest.kt`, `FilterKind`, `cocktailDrink`, `MixologyDatabase`, `Flow`, `HotViewModelTest.kt`, `ShoppingItemEntity`, `SearchViewModelTest`, `WidgetEntryPoint`, `catalog`, `DrinkGridViewModelTest.kt`, `CatalogViewModel`, `Drink`, `RandomixerViewModel`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `DrinkRecipeBody`, `MixologyTheme`, `HotViewModel`, `SearchViewModel`, `BarViewModel`, `MviViewModel`, `ShoppingViewModel`, `MainViewModel`, `ThemeMode`, `CatalogViewModel`, `Drink`?**
  _High betweenness centrality (0.086) - this node is a cross-community bridge._
- **Are the 24 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.drinkClick_reportsMakeableCocktail()`) actually correct?**
  _`Drink` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Are the 43 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 43 INFERRED edges - model-reasoned connections that need verification._
- **Are the 8 inferred relationships involving `DrinkRepository` (e.g. with `setUp()` and `queryChanged_filtersTermsAcrossMultipleSearches()`) actually correct?**
  _`DrinkRepository` has 8 INFERRED edges - model-reasoned connections that need verification._
- **Are the 14 inferred relationships involving `CocktailDbResponse` (e.g. with `.fetchAndCache_observesFilterResultsAndSkipsBadThumbs()` and `.fetchAndCache_replacesPreviousFilterMemberships()`) actually correct?**
  _`CocktailDbResponse` has 14 INFERRED edges - model-reasoned connections that need verification._