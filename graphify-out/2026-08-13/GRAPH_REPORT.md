# Graph Report - Mixology  (2026-08-13)

## Corpus Check
- 54 files · ~40,329 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 722 nodes · 1654 edges · 39 communities (35 shown, 4 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 96 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `52f23ff3`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkCursorAdapter
- ApplicationComponent.java
- RecyclerView
- FragmentRandomixer.java
- WidgetDataProvider
- AppCompatActivity
- ActivityLogin
- ActivityMain.java
- FragmentDetails.java
- FragmentAlcoholic
- FragmentDrink.java
- FragmentNonAlcoholic
- FragmentVodka
- FragmentCocktail
- FragmentCocktailGlass
- FragmentGin
- FragmentHighballGlass
- FragmentOrdinaryDrink
- FragmentSavedDrink
- Cocktail
- CursorRecyclerViewAdapter
- GlassTypeFilterJob.java
- DrinkTypeFilterJob.java
- IngredientFilterJob.java
- AlcoholFilterJob.java
- Mixology
- FragmentGrid
- Cocktails
- Measures
- gradlew
- DrinkDatabase
- ExampleUnitTest.java
- custom.md
- AlcoholicColumn.java

## God Nodes (most connected - your core abstractions)
1. `Drink` - 101 edges
2. `DrinkCursorAdapter` - 38 edges
3. `Cocktail` - 35 edges
4. `FragmentDetails` - 23 edges
5. `ActivityMain` - 22 edges
6. `FragmentAlcoholic` - 19 edges
7. `FragmentCocktail` - 19 edges
8. `FragmentCocktailGlass` - 19 edges
9. `FragmentGin` - 19 edges
10. `FragmentHighballGlass` - 19 edges

## Surprising Connections (you probably didn't know these)
- `ActivitySearch` --references--> `CocktailService`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Activities/ActivitySearch.java → app/src/main/java/com/capstone/nik/mixology/Network/CocktailService.java
- `DrinkCursorAdapter` --inherits--> `CursorRecyclerViewAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java → app/src/main/java/com/capstone/nik/mixology/utils/CursorRecyclerViewAdapter.java
- `FragmentAlcoholic` --references--> `DrinkCursorAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Fragments/FragmentAlcoholic.java → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java
- `FragmentCocktail` --references--> `DrinkCursorAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Fragments/FragmentCocktail.java → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java
- `FragmentCocktailGlass` --references--> `DrinkCursorAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Fragments/FragmentCocktailGlass.java → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java

## Import Cycles
- None detected.

## Communities (39 total, 4 thin omitted)

### Community 1 - "DrinkCursorAdapter"
Cohesion: 0.10
Nodes (29): DrinkCursorAdapter, Activity, Cocktail, Cursor, ImageView, LayoutInflater, Override, TextView (+21 more)

### Community 2 - "ApplicationComponent.java"
Cohesion: 0.11
Nodes (23): ApplicationComponent, Singleton, ApplicationModule, Application, Context, JobManager, Singleton, Override (+15 more)

### Community 4 - "RecyclerView"
Cohesion: 0.10
Nodes (18): ActivitySearch, Bundle, Menu, Override, TextView, OnAdapterItemSelectedListener, Activity, ImageView (+10 more)

### Community 5 - "FragmentRandomixer.java"
Cohesion: 0.12
Nodes (21): Adapter, IngredientsAdapter, Context, ImageView, LayoutInflater, Override, TextView, View (+13 more)

### Community 6 - "WidgetDataProvider"
Cohesion: 0.13
Nodes (16): DrinkWidgetProvider, Context, Intent, Override, DrinkWidgetService, Context, Cursor, Intent (+8 more)

### Community 7 - "AppCompatActivity"
Cohesion: 0.12
Nodes (20): ActivityDetails, Bundle, Menu, Override, ActivityPasswordChange, Bundle, ImageView, Override (+12 more)

### Community 8 - "ActivityLogin"
Cohesion: 0.14
Nodes (14): ActivityLogin, Bundle, Button, Intent, LinearLayout, Override, ProgressDialog, TextInputEditText (+6 more)

### Community 9 - "ActivityMain.java"
Cohesion: 0.15
Nodes (12): ActivityMain, Bundle, Menu, Override, TextView, Uri, View, CircleImageView (+4 more)

### Community 10 - "FragmentDetails.java"
Cohesion: 0.16
Nodes (14): FragmentDetails, Activity, Bundle, Context, ImageView, Intent, LayoutInflater, LinearLayout (+6 more)

### Community 11 - "FragmentAlcoholic"
Cohesion: 0.22
Nodes (11): FragmentAlcoholic, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, NonNull (+3 more)

### Community 12 - "FragmentDrink.java"
Cohesion: 0.22
Nodes (11): FragmentDrink, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, NonNull (+3 more)

### Community 13 - "FragmentNonAlcoholic"
Cohesion: 0.22
Nodes (11): FragmentNonAlcoholic, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, Override (+3 more)

### Community 14 - "FragmentVodka"
Cohesion: 0.22
Nodes (11): FragmentVodka, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, NonNull (+3 more)

### Community 15 - "FragmentCocktail"
Cohesion: 0.23
Nodes (10): FragmentCocktail, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, Override (+2 more)

### Community 16 - "FragmentCocktailGlass"
Cohesion: 0.23
Nodes (10): FragmentCocktailGlass, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, Override (+2 more)

### Community 17 - "FragmentGin"
Cohesion: 0.23
Nodes (10): FragmentGin, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, Override (+2 more)

### Community 18 - "FragmentHighballGlass"
Cohesion: 0.23
Nodes (10): FragmentHighballGlass, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, Override (+2 more)

### Community 19 - "FragmentOrdinaryDrink"
Cohesion: 0.23
Nodes (10): FragmentOrdinaryDrink, Activity, Bundle, Cursor, JobManager, LayoutInflater, Loader, Override (+2 more)

### Community 20 - "FragmentSavedDrink"
Cohesion: 0.25
Nodes (9): FragmentSavedDrink, Activity, Bundle, Cursor, LayoutInflater, Loader, Override, View (+1 more)

### Community 21 - "Cocktail"
Cohesion: 0.16
Nodes (4): Cocktail, Creator, Override, Parcel

### Community 22 - "CursorRecyclerViewAdapter"
Cohesion: 0.25
Nodes (6): CursorRecyclerViewAdapter, Context, Cursor, Override, NotifyingDataSetObserver, DataSetObserver

### Community 23 - "GlassTypeFilterJob.java"
Cohesion: 0.21
Nodes (7): Cocktail, GlassTypeFilterJob, Cocktail, Context, Override, RetryConstraint, Uri

### Community 24 - "DrinkTypeFilterJob.java"
Cohesion: 0.22
Nodes (7): DrinkTypeFilterJob, Cocktail, Context, Override, RetryConstraint, Uri, Job

### Community 25 - "IngredientFilterJob.java"
Cohesion: 0.22
Nodes (7): IngredientFilterJob, Cocktail, Context, Override, RetryConstraint, Uri, ContentValues

### Community 26 - "AlcoholFilterJob.java"
Cohesion: 0.23
Nodes (6): AlcoholFilterJob, Cocktail, Context, Override, RetryConstraint, Uri

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "FragmentGrid"
Cohesion: 0.18
Nodes (9): FragmentGrid, Bundle, Fragment, LayoutInflater, View, ViewGroup, newInstance(), FragmentGridViewModel (+1 more)

### Community 29 - "Cocktails"
Cohesion: 0.42
Nodes (4): CocktailService, Cocktails, Call, GET

### Community 30 - "Measures"
Cohesion: 0.23
Nodes (5): Creator, Override, Parcel, Measures, Parcelable

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

## Knowledge Gaps
- **15 isolated node(s):** `AlcoholicColumn`, `Path`, `Describe the change`, `Why is this helpful`, `Optional Implementation` (+10 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `.setUIData`, `RecyclerView`, `FragmentRandomixer.java`, `FragmentDetails.java`, `GlassTypeFilterJob.java`, `DrinkTypeFilterJob.java`, `IngredientFilterJob.java`, `AlcoholFilterJob.java`, `Cocktails`, `.shareRecipe`?**
  _High betweenness centrality (0.191) - this node is a cross-community bridge._
- **Why does `DrinkCursorAdapter` connect `DrinkCursorAdapter` to `RecyclerView`, `ActivityMain.java`, `FragmentAlcoholic`, `FragmentDrink.java`, `FragmentNonAlcoholic`, `FragmentVodka`, `FragmentCocktail`, `FragmentCocktailGlass`, `FragmentGin`, `FragmentHighballGlass`, `FragmentOrdinaryDrink`, `FragmentSavedDrink`, `CursorRecyclerViewAdapter`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `DrinkCursorAdapter`, `RecyclerView`, `WidgetDataProvider`, `ActivityMain.java`, `FragmentDetails.java`, `GlassTypeFilterJob.java`, `DrinkTypeFilterJob.java`, `IngredientFilterJob.java`, `AlcoholFilterJob.java`, `Cocktails`, `Measures`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **What connects `AlcoholicColumn`, `Path`, `Describe the change` to the rest of the system?**
  _15 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.04081632653061224 - nodes in this community are weakly interconnected._
- **Should `DrinkCursorAdapter` be split into smaller, more focused modules?**
  _Cohesion score 0.10299003322259136 - nodes in this community are weakly interconnected._
- **Should `ApplicationComponent.java` be split into smaller, more focused modules?**
  _Cohesion score 0.10952380952380952 - nodes in this community are weakly interconnected._