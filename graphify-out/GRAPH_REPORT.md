# Graph Report - Mixology  (2026-08-18)

## Corpus Check
- 123 files · ~75,534 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 847 nodes · 1524 edges · 58 communities (47 shown, 11 thin omitted)
- Extraction: 79% EXTRACTED · 21% INFERRED · 0% AMBIGUOUS · INFERRED: 313 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `75a05132`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DrinkRecipeBody
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- WidgetContent
- DrinkDetailsIntent
- MixologyApp
- BarViewModel
- cocktailDrink
- MixologyDatabase
- DrinkGridViewModel
- HotViewModelTest.kt
- CocktailURLs.java
- ShoppingViewModel
- NetworkMonitor
- MainIntent
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
- DrawerDestination
- DrinkDetailsViewModelTest.kt
- custom.md
- DrinkRepository
- CatalogViewModelTest.kt
- NetworkModule
- MainDispatcherRule
- BarMatcher
- Flow
- Play Store listing
- ShoppingItemEntity
- .fetchAndCache
- IntentExtrasTest
- MixologyDatabaseMigrationTest
- ActivityMainEspressoTest
- MainViewModel
- CatalogListItem
- CocktailDbDrink
- MviViewModel
- .sendEffect
- CatalogViewModel
- CatalogUiState
- CatalogScreen

## God Nodes (most connected - your core abstractions)
1. `Drink` - 65 edges
2. `MixologyTheme()` - 42 edges
3. `DrinkRepository` - 39 edges
4. `DrinkDao` - 32 edges
5. `FakeCocktailService` - 25 edges
6. `CocktailDbResponse` - 24 edges
7. `MixologyApp()` - 23 edges
8. `cocktailDrink()` - 21 edges
9. `MviViewModel` - 19 edges
10. `IngredientMeasure` - 18 edges

## Surprising Connections (you probably didn't know these)
- `setUp()` --calls--> `FakeCocktailService`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/hot/HotViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `openDrink_emitsEffect()` --calls--> `cocktailDrink()`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `catalog()` --calls--> `CatalogListResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt
- `catalog()` --calls--> `CatalogListItem`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt
- `addToShoppingList_insertsIngredients()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt

## Import Cycles
- None detected.

## Communities (58 total, 11 thin omitted)

### Community 0 - "DrinkRecipeBody"
Cohesion: 0.10
Nodes (19): DrinkHeroImage(), DrinkRecipeBody(), InstructionBlock(), instructionSteps(), Modifier, RecipeChip(), RecipeSectionTitle(), FavoriteButton() (+11 more)

### Community 1 - "DrinkDao"
Cohesion: 0.09
Nodes (11): CatalogSeed, CatalogSeedDrink, CatalogSeedPayload, Context, CatalogTermEntity, DrinkDao, Flow, DrinkEntity (+3 more)

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.09
Nodes (16): DrinkCard(), Dp, Modifier, DrinkGridRoute(), DrinkGridScreen(), DrinkFilter, SnackbarHostState, RandomixerUiState (+8 more)

### Community 4 - "HotViewModel"
Cohesion: 0.11
Nodes (20): recordCrash(), HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter (+12 more)

### Community 5 - "WidgetContent"
Cohesion: 0.16
Nodes (15): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+7 more)

### Community 6 - "DrinkDetailsIntent"
Cohesion: 0.14
Nodes (13): AddToShoppingList, Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, OpenUrl, OpenVideo (+5 more)

### Community 7 - "MixologyApp"
Cohesion: 0.05
Nodes (41): ActivityMain, Intent, FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, detailsRoute() (+33 more)

### Community 8 - "BarViewModel"
Cohesion: 0.07
Nodes (28): BarEffect, BarIntent, BarUiState, ClosePicker, filterBarTerms(), Load, OpenDrink, OpenPicker (+20 more)

### Community 9 - "cocktailDrink"
Cohesion: 0.15
Nodes (3): cocktailDrink(), DrinkRepositoryTest, SearchViewModelTest

### Community 10 - "MixologyDatabase"
Cohesion: 0.07
Nodes (13): BarDao, Flow, BarIngredientEntity, create(), importLegacySavedDrinks(), Context, MixologyDatabase, Flow (+5 more)

### Community 11 - "DrinkGridViewModel"
Cohesion: 0.16
Nodes (12): Bind, DrinkGridEffect, DrinkGridIntent, DrinkGridUiState, OpenDrink, ShowMessage, ShowMessageRes, ToggleSaved (+4 more)

### Community 12 - "HotViewModelTest.kt"
Cohesion: 0.39
Nodes (7): awaitItemUntil(), createViewModel(), T, load_fallsBackToPresetDrinkTypesWhenCatalogEmpty(), load_usesDrinkTypeCatalogTerms(), save_addsRecentlyViewedRowFirst(), setUp()

### Community 19 - "ShoppingViewModel"
Cohesion: 0.18
Nodes (10): ClearChecked, Remove, ShoppingEffect, ShoppingIntent, ShoppingUiState, Toggle, ShoppingRoute(), ShoppingScreen() (+2 more)

### Community 20 - "NetworkMonitor"
Cohesion: 0.08
Nodes (16): Activity, Animator, AppEntryPoint, MyApplication, forTests(), StateFlow, NetworkMonitor, Overlay (+8 more)

### Community 21 - "MainIntent"
Cohesion: 0.17
Nodes (11): DismissMenu, DrinkSelected, MainEffect, MainIntent, MainUiState, Navigate, OpenDetails, OpenMenu (+3 more)

### Community 22 - "ThemeMode"
Cohesion: 0.18
Nodes (12): SettingsRoute(), SettingsScreen(), fromStorage(), Context, rememberThemeMode(), ThemeMode, DARK, LIGHT (+4 more)

### Community 23 - "DrinkGridViewModelTest.kt"
Cohesion: 0.38
Nodes (5): awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, openDrink_emitsEffect(), toggleSaved_savesDrinkWithoutMessage()

### Community 24 - "Drink"
Cohesion: 0.15
Nodes (5): Drink, drinkExtra(), DrinkIntents, Intent, putDrinkExtra()

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.16
Nodes (23): datetime, Path, blank(), connect(), drink_payload(), main(), parse_args(), Namespace (+15 more)

### Community 26 - "RandomixerViewModel"
Cohesion: 0.14
Nodes (14): RandomixerEffect, RandomixerIntent, Refresh, ShowMessageRes, ShowUndo, SwipeDiscard, SwipeSave, ToggleHideSaved (+6 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "IngredientMeasure"
Cohesion: 0.10
Nodes (20): IngredientListConverter, IngredientRow(), Modifier, IngredientMeasure, ActionCircleButton(), DrinkSwipeDetails(), DrinkSwipePhoto(), Modifier (+12 more)

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

### Community 33 - "DrawerDestination"
Cohesion: 0.15
Nodes (13): Bar, Catalog, DrawerDestination, Filter, Hot, Randomixer, Settings, Shopping (+5 more)

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.32
Nodes (5): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), setUp()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 39 - "CatalogViewModelTest.kt"
Cohesion: 0.36
Nodes (5): awaitItemUntil(), T, load_populatesIngredientTerms(), queryChanged_filtersTermsAcrossMultipleSearches(), selectSameKind_doesNotClearQuery()

### Community 40 - "NetworkModule"
Cohesion: 0.40
Nodes (3): Context, NetworkModule, OkHttpClient

### Community 41 - "MainDispatcherRule"
Cohesion: 0.40
Nodes (3): MainDispatcherRule, Description, TestWatcher

### Community 42 - "BarMatcher"
Cohesion: 0.39
Nodes (3): BarAlmostDrink, BarMatcher, BarRecommendations

### Community 44 - "Play Store listing"
Cohesion: 0.40
Nodes (4): English, Español, Play Store listing, Screenshot checklist

### Community 52 - "CocktailDbDrink"
Cohesion: 0.10
Nodes (4): CocktailDbDrink, DrinkFilterTest, CocktailDbDrinkTest, IngredientMeasureTest

### Community 53 - "MviViewModel"
Cohesion: 0.26
Nodes (11): CollectMviEffects(), Flow, StateFlow, MviStore, MviViewModel, SnackbarHostState, RandomixerRoute(), E (+3 more)

### Community 55 - "CatalogViewModel"
Cohesion: 0.24
Nodes (7): CatalogIntent, Load, OpenTerm, QueryChanged, SelectKind, CatalogViewModel, Job

### Community 56 - "CatalogUiState"
Cohesion: 0.29
Nodes (7): CatalogEffect, CatalogUiState, filterCatalogTerms(), OpenFilter, withCatalogTerms(), withQuery(), CatalogContractTest

### Community 57 - "CatalogScreen"
Cohesion: 0.36
Nodes (4): CatalogRoute(), CatalogScreen(), CatalogTermRow(), CatalogScreenTest

## Knowledge Gaps
- **85 isolated node(s):** `CocktailURLs`, `CatalogSeedDrink`, `ALCOHOL`, `GLASS`, `INGREDIENT` (+80 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRecipeBody`, `MixologyTheme`, `HotViewModel`, `WidgetContent`, `MixologyApp`, `BarViewModel`, `DrinkGridViewModel`, `RandomixerViewModel`, `IngredientMeasure`, `DrinkDetailsViewModelTest.kt`, `DrinkRepository`, `BarMatcher`, `Flow`, `.fetchAndCache`, `IntentExtrasTest`, `ActivityMainEspressoTest`, `MainViewModel`, `CocktailDbDrink`, `.sendEffect`?**
  _High betweenness centrality (0.300) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `DrinkRecipeBody`, `DrawerDestination`, `MixologyTheme`, `HotViewModel`, `BarViewModel`, `MainViewModel`, `ShoppingViewModel`, `MviViewModel`, `ThemeMode`, `Drink`, `CatalogScreen`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Why does `DrinkRepository` connect `DrinkRepository` to `DrinkDetailsViewModelTest.kt`, `MixologyApp`, `CatalogViewModelTest.kt`, `cocktailDrink`, `MixologyDatabase`, `Flow`, `DrinkGridViewModel`, `ShoppingItemEntity`, `.fetchAndCache`, `HotViewModelTest.kt`, `Drink`, `FakeCocktailService`?**
  _High betweenness centrality (0.084) - this node is a cross-community bridge._
- **Are the 24 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.drinkClick_reportsMakeableCocktail()`) actually correct?**
  _`Drink` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Are the 41 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 41 INFERRED edges - model-reasoned connections that need verification._
- **Are the 7 inferred relationships involving `DrinkRepository` (e.g. with `setUp()` and `queryChanged_filtersTermsAcrossMultipleSearches()`) actually correct?**
  _`DrinkRepository` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Are the 6 inferred relationships involving `FakeCocktailService` (e.g. with `setUp()` and `queryChanged_filtersTermsAcrossMultipleSearches()`) actually correct?**
  _`FakeCocktailService` has 6 INFERRED edges - model-reasoned connections that need verification._