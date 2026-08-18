# Graph Report - Mixology  (2026-08-18)

## Corpus Check
- 107 files · ~47,966 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 725 nodes · 1259 edges · 43 communities (40 shown, 3 thin omitted)
- Extraction: 79% EXTRACTED · 21% INFERRED · 0% AMBIGUOUS · INFERRED: 259 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `6da06cb7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DrinkRecipeBody
- DrinkDao
- Measures
- MixologyTheme
- HotViewModel
- WidgetContent
- RandomixerViewModel
- MixologyApp
- MviViewModel
- cocktailDrink
- MixologyDatabase
- DrinkGridViewModel
- setUp
- CocktailURLs.java
- ShoppingViewModel
- NetworkMonitor
- MainViewModel
- ThemeMode
- DrinkGridViewModelTest.kt
- Drink
- fetch_random_drink.py
- DrawerDestination
- Mixology
- IngredientMeasure
- CocktailDbResponse
- fetch_randomixer_csv.py
- FakeCocktailService
- gradlew
- ActivityMain
- DrinkDetailsViewModelTest.kt
- custom.md
- CatalogViewModelTest.kt
- NetworkModule
- MainDispatcherRule
- Play Store listing
- MixologyDatabaseMigrationTest

## God Nodes (most connected - your core abstractions)
1. `Drink` - 53 edges
2. `MixologyTheme()` - 37 edges
3. `DrinkRepository` - 30 edges
4. `DrinkDao` - 23 edges
5. `FakeCocktailService` - 23 edges
6. `MixologyApp()` - 22 edges
7. `CocktailDbResponse` - 21 edges
8. `MviViewModel` - 18 edges
9. `RandomixerViewModel` - 17 edges
10. `cocktailDrink()` - 16 edges

## Surprising Connections (you probably didn't know these)
- `openDrink_emitsEffect()` --calls--> `cocktailDrink()`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt
- `catalog()` --calls--> `CatalogListResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/FakeCocktailService.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CatalogListResponse.kt
- `addToShoppingList_insertsIngredients()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt
- `load_usesLookupRecipe()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/details/DrinkDetailsViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt
- `bind_loadsCachedFilterDrinks()` --calls--> `CocktailDbResponse`  [INFERRED]
  app/src/test/java/com/capstone/nik/mixology/ui/grid/DrinkGridViewModelTest.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/CocktailDbDrink.kt

## Import Cycles
- None detected.

## Communities (43 total, 3 thin omitted)

### Community 0 - "DrinkRecipeBody"
Cohesion: 0.07
Nodes (28): DrinkCard(), Dp, Modifier, CircularDrinkImage(), DrinkImage(), IngredientImage(), Dp, Modifier (+20 more)

### Community 1 - "DrinkDao"
Cohesion: 0.13
Nodes (6): CatalogTermEntity, DrinkDao, Flow, DrinkEntity, toEntity(), DrinkFilterCrossRef

### Community 2 - "Measures"
Cohesion: 0.21
Nodes (5): Measures, Creator, Override, Parcel, Parcelable

### Community 3 - "MixologyTheme"
Cohesion: 0.11
Nodes (13): DrinkGridRoute(), DrinkGridScreen(), DrinkFilter, SnackbarHostState, RandomixerUiState, RandomixerScreen(), SearchUiState, SearchScreen() (+5 more)

### Community 4 - "HotViewModel"
Cohesion: 0.13
Nodes (17): HotCategory, HotEffect, HotIntent, HotUiState, Load, OpenDrink, OpenFilter, SeeAll (+9 more)

### Community 5 - "WidgetContent"
Cohesion: 0.16
Nodes (15): DrinkWidgetProvider, Context, GlanceAppWidget, Intent, DrinkRow(), Context, GlanceAppWidget, Intent (+7 more)

### Community 6 - "RandomixerViewModel"
Cohesion: 0.06
Nodes (33): recordCrash(), CatalogEffect, CatalogIntent, CatalogUiState, filterCatalogTerms(), Load, OpenFilter, OpenTerm (+25 more)

### Community 7 - "MixologyApp"
Cohesion: 0.07
Nodes (37): FilterKind, ALCOHOL, DRINK_TYPE, GLASS, INGREDIENT, detailsRoute(), encodeRouteArg(), filterKindFromRoute() (+29 more)

### Community 8 - "MviViewModel"
Cohesion: 0.08
Nodes (26): AddToShoppingList, Back, DrinkDetailsEffect, DrinkDetailsIntent, Load, NavigateBack, OpenUrl, OpenVideo (+18 more)

### Community 9 - "cocktailDrink"
Cohesion: 0.16
Nodes (3): cocktailDrink(), DrinkRepositoryTest, SearchViewModelTest

### Community 10 - "MixologyDatabase"
Cohesion: 0.09
Nodes (12): create(), Callback, importLegacySavedDrinks(), Context, MixologyDatabase, Flow, ShoppingDao, ShoppingItemEntity (+4 more)

### Community 11 - "DrinkGridViewModel"
Cohesion: 0.16
Nodes (12): Bind, DrinkGridEffect, DrinkGridIntent, DrinkGridUiState, OpenDrink, ShowMessage, ShowMessageRes, ToggleSaved (+4 more)

### Community 12 - "setUp"
Cohesion: 0.40
Nodes (4): awaitItemUntil(), T, load_populatesIngredientRailsInOrder(), setUp()

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
Cohesion: 0.38
Nodes (5): awaitItemUntil(), bind_loadsCachedFilterDrinks(), T, openDrink_emitsEffect(), toggleSaved_savesDrinkWithoutMessage()

### Community 24 - "Drink"
Cohesion: 0.06
Nodes (12): ActivityMainEspressoTest, Drink, WidgetEntryPoint, DrinkRepository, hasUsableThumb(), DrinkFilter, Flow, drinkExtra() (+4 more)

### Community 25 - "fetch_random_drink.py"
Cohesion: 0.26
Nodes (16): datetime, blank_to_none(), connect(), drink_row(), ensure_schema(), fetch_random_drink(), ingredient_image_url(), ingredient_rows() (+8 more)

### Community 26 - "DrawerDestination"
Cohesion: 0.16
Nodes (12): Catalog, DrawerDestination, Filter, Hot, Randomixer, Settings, Shopping, BottomNavItem (+4 more)

### Community 27 - "Mixology"
Cohesion: 0.25
Nodes (7): Credits, License, Mixology, Overview, Play Store, Setup, Stack

### Community 28 - "IngredientMeasure"
Cohesion: 0.07
Nodes (17): IngredientListConverter, CocktailDbDrink, IngredientRow(), Modifier, ingredientImageUrl(), IngredientMeasure, ActionCircleButton(), DrinkSwipeDetails() (+9 more)

### Community 29 - "CocktailDbResponse"
Cohesion: 0.21
Nodes (3): CocktailService, CatalogListResponse, CocktailDbResponse

### Community 30 - "fetch_randomixer_csv.py"
Cohesion: 0.28
Nodes (12): Exception, Path, cell(), drink_row(), fetch_random_drink(), FetchError, load_existing_ids(), main() (+4 more)

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 33 - "ActivityMain"
Cohesion: 0.29
Nodes (4): ActivityMain, Intent, AppCompatActivity, Bundle

### Community 34 - "DrinkDetailsViewModelTest.kt"
Cohesion: 0.32
Nodes (5): addToShoppingList_insertsIngredients(), awaitItemUntil(), T, load_usesLookupRecipe(), setUp()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

### Community 39 - "CatalogViewModelTest.kt"
Cohesion: 0.17
Nodes (8): CatalogListItem, catalog(), awaitItemUntil(), T, load_populatesCategoryTerms(), queryChanged_filtersTermsAcrossMultipleSearches(), selectSameKind_doesNotClearQuery(), setUp()

### Community 40 - "NetworkModule"
Cohesion: 0.40
Nodes (3): Context, NetworkModule, OkHttpClient

### Community 41 - "MainDispatcherRule"
Cohesion: 0.40
Nodes (3): MainDispatcherRule, Description, TestWatcher

### Community 44 - "Play Store listing"
Cohesion: 0.40
Nodes (4): English, Español, Play Store listing, Screenshot checklist

## Knowledge Gaps
- **77 isolated node(s):** `CocktailURLs`, `ALCOHOL`, `GLASS`, `INGREDIENT`, `DRINK_TYPE` (+72 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `DrinkRecipeBody`, `DrinkDao`, `DrinkDetailsViewModelTest.kt`, `MixologyTheme`, `HotViewModel`, `WidgetContent`, `RandomixerViewModel`, `MixologyApp`, `MviViewModel`, `DrinkGridViewModel`, `MainViewModel`, `IngredientMeasure`?**
  _High betweenness centrality (0.286) - this node is a cross-community bridge._
- **Why does `MixologyApp()` connect `MixologyApp` to `DrinkRecipeBody`, `ActivityMain`, `MixologyTheme`, `HotViewModel`, `RandomixerViewModel`, `MviViewModel`, `ShoppingViewModel`, `MainViewModel`, `ThemeMode`, `Drink`, `DrawerDestination`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Why does `MixologyTheme()` connect `MixologyTheme` to `DrinkRecipeBody`, `ActivityMain`, `HotViewModel`, `RandomixerViewModel`, `ShoppingViewModel`, `ThemeMode`, `DrawerDestination`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `Drink` (e.g. with `.drinkExtras_showsDrinkName()` and `.content_numberedInstructions_showsSteps()`) actually correct?**
  _`Drink` has 20 INFERRED edges - model-reasoned connections that need verification._
- **Are the 36 inferred relationships involving `MixologyTheme()` (e.g. with `.onCreate()` and `rememberThemeMode()`) actually correct?**
  _`MixologyTheme()` has 36 INFERRED edges - model-reasoned connections that need verification._
- **Are the 6 inferred relationships involving `DrinkRepository` (e.g. with `queryChanged_filtersTermsAcrossMultipleSearches()` and `setUp()`) actually correct?**
  _`DrinkRepository` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 5 inferred relationships involving `FakeCocktailService` (e.g. with `queryChanged_filtersTermsAcrossMultipleSearches()` and `setUp()`) actually correct?**
  _`FakeCocktailService` has 5 INFERRED edges - model-reasoned connections that need verification._