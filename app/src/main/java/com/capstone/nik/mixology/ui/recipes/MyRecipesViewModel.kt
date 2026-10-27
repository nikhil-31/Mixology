package com.capstone.nik.mixology.ui.recipes

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.repository.RecipeRepository
import com.capstone.nik.mixology.ui.components.DrinkViewPreferences
import com.capstone.nik.mixology.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRecipesViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    @ApplicationContext private val context: Context,
) : MviViewModel<MyRecipesIntent, MyRecipesUiState, MyRecipesEffect>(MyRecipesUiState()) {

    init {
        setState { copy(listView = DrinkViewPreferences.listView(context)) }
        viewModelScope.launch {
            // Edits and deletes elsewhere update the repository's list, and so this screen.
            recipes.recipes.collect { list -> setState { copy(recipes = list) } }
        }
        load()
    }

    override fun onIntent(intent: MyRecipesIntent) {
        when (intent) {
            MyRecipesIntent.Retry -> load()
            is MyRecipesIntent.OpenRecipe -> sendEffect(MyRecipesEffect.OpenRecipe(intent.recipe))
            MyRecipesIntent.AddRecipe -> sendEffect(MyRecipesEffect.OpenEditor)
            MyRecipesIntent.ToggleListView -> toggleListView()
        }
    }

    private fun load() {
        if (currentState.loading) return
        setState { copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                recipes.load()
                setState { copy(loading = false, loaded = true) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                setState { copy(loading = false, error = error.message.orEmpty()) }
            }
        }
    }

    private fun toggleListView() {
        val listView = !currentState.listView
        DrinkViewPreferences.setListView(context, listView)
        setState { copy(listView = listView) }
    }
}
