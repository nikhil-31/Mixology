# Graph Report - Mixology  (2026-08-13)

## Corpus Check
- 40 files · ~36,677 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 499 nodes · 992 edges · 23 communities (19 shown, 4 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 94 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ada9cd34`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Drink
- DrinkCursorAdapter.java
- ApplicationModule.java
- Cocktail
- FragmentRandomixer.java
- WidgetDataProvider
- AppCompatActivity
- ActivityLogin
- FragmentDetails.java
- ActivitySignUp
- DrinkFilterTest
- CursorRecyclerViewAdapter
- Mixology
- FragmentGrid
- CocktailService
- Measures
- gradlew
- DrinkDatabase
- custom.md
- AlcoholicColumn.java

## God Nodes (most connected - your core abstractions)
1. `Drink` - 96 edges
2. `Cocktail` - 27 edges
3. `FragmentDetails` - 23 edges
4. `ActivityMain` - 22 edges
5. `CocktailService` - 19 edges
6. `IngredientsAdapter` - 17 edges
7. `FragmentRandomixer` - 17 edges
8. `Measures` - 17 edges
9. `ActivityLogin` - 16 edges
10. `SearchAdapter` - 16 edges

## Surprising Connections (you probably didn't know these)
- `hasUsableThumb()` --references--> `Drink`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/repository/DrinkRepository.kt → app/src/main/java/com/capstone/nik/mixology/Network/remoteModel/Drink.java
- `ActivitySearch` --references--> `SearchAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Activities/ActivitySearch.java → app/src/main/java/com/capstone/nik/mixology/Adapters/SearchAdapter.java
- `DrinkCursorAdapter` --inherits--> `CursorRecyclerViewAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java → app/src/main/java/com/capstone/nik/mixology/utils/CursorRecyclerViewAdapter.java
- `FragmentGrid` --references--> `DrinkCursorAdapter`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Fragments/FragmentGrid.kt → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java
- `MyViewHolder` --inherits--> `ViewHolder`  [EXTRACTED]
  app/src/main/java/com/capstone/nik/mixology/Adapters/IngredientsAdapter.java → app/src/main/java/com/capstone/nik/mixology/Adapters/DrinkCursorAdapter.java

## Import Cycles
- None detected.

## Communities (23 total, 4 thin omitted)

### Community 1 - "DrinkCursorAdapter.java"
Cohesion: 0.10
Nodes (31): DrinkCursorAdapter, Activity, Cocktail, Cursor, ImageView, LayoutInflater, Override, TextView (+23 more)

### Community 2 - "ApplicationModule.java"
Cohesion: 0.09
Nodes (21): ApplicationComponent, Singleton, ApplicationModule, Application, Context, Singleton, CocktailURLs, Override (+13 more)

### Community 4 - "Cocktail"
Cohesion: 0.08
Nodes (17): Adapter, OnAdapterItemSelectedListener, Activity, ImageView, LayoutInflater, Override, TextView, View (+9 more)

### Community 5 - "FragmentRandomixer.java"
Cohesion: 0.12
Nodes (20): IngredientsAdapter, Context, ImageView, LayoutInflater, Override, TextView, View, ViewGroup (+12 more)

### Community 6 - "WidgetDataProvider"
Cohesion: 0.19
Nodes (10): DrinkWidgetService, Context, Cursor, Intent, Override, WidgetDataProvider, RemoteViews, RemoteViewsFactory (+2 more)

### Community 7 - "AppCompatActivity"
Cohesion: 0.13
Nodes (17): ActivityDetails, Bundle, Menu, Override, ActivityPasswordChange, Bundle, ImageView, Override (+9 more)

### Community 8 - "ActivityLogin"
Cohesion: 0.14
Nodes (14): ActivityLogin, Bundle, Button, Intent, LinearLayout, Override, ProgressDialog, TextInputEditText (+6 more)

### Community 9 - "FragmentDetails.java"
Cohesion: 0.08
Nodes (26): ActivityMain, Bundle, Menu, Override, TextView, Uri, View, FragmentDetails (+18 more)

### Community 10 - "ActivitySignUp"
Cohesion: 0.30
Nodes (9): ActivitySignUp, Bundle, Button, ImageView, LinearLayout, Override, ProgressDialog, TextView (+1 more)

### Community 22 - "CursorRecyclerViewAdapter"
Cohesion: 0.25
Nodes (6): CursorRecyclerViewAdapter, Context, Cursor, Override, NotifyingDataSetObserver, DataSetObserver

### Community 27 - "Mixology"
Cohesion: 0.14
Nodes (13): Credits, Details Screen, Libraries used, License, Main Screen, Mixology, Overview, Phone (+5 more)

### Community 28 - "FragmentGrid"
Cohesion: 0.07
Nodes (27): AndroidViewModel, DrinkFilter, ALCOHOLIC, COCKTAIL, COCKTAIL_GLASS, GIN, HIGHBALL_GLASS, NON_ALCOHOLIC (+19 more)

### Community 29 - "CocktailService"
Cohesion: 0.17
Nodes (10): ActivitySearch, Bundle, Menu, Override, RecyclerView, TextView, CocktailService, Cocktails (+2 more)

### Community 30 - "Measures"
Cohesion: 0.16
Nodes (4): Creator, Override, Parcel, Measures

### Community 32 - "gradlew"
Cohesion: 0.60
Nodes (3): gradlew script, die(), warn()

### Community 35 - "custom.md"
Cohesion: 0.50
Nodes (3): Describe the change, Optional Implementation, Why is this helpful

## Knowledge Gaps
- **28 isolated node(s):** `AlcoholicColumn`, `ALCOHOLIC`, `NON_ALCOHOLIC`, `COCKTAIL`, `ORDINARY_DRINK` (+23 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Drink` connect `Drink` to `ApplicationModule.java`, `.setUIData`, `Cocktail`, `FragmentRandomixer.java`, `FragmentDetails.java`, `DrinkFilterTest`, `CocktailService`, `Measures`?**
  _High betweenness centrality (0.282) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `Cocktail` to `FragmentDetails.java`, `CocktailService`, `DrinkCursorAdapter.java`, `WidgetDataProvider`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Why does `Cocktail` connect `DrinkCursorAdapter.java` to `FragmentDetails.java`, `Cocktail`, `CocktailService`, `WidgetDataProvider`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **What connects `AlcoholicColumn`, `ALCOHOLIC`, `NON_ALCOHOLIC` to the rest of the system?**
  _28 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Drink` be split into smaller, more focused modules?**
  _Cohesion score 0.043478260869565216 - nodes in this community are weakly interconnected._
- **Should `DrinkCursorAdapter.java` be split into smaller, more focused modules?**
  _Cohesion score 0.10303030303030303 - nodes in this community are weakly interconnected._
- **Should `ApplicationModule.java` be split into smaller, more focused modules?**
  _Cohesion score 0.09246088193456614 - nodes in this community are weakly interconnected._