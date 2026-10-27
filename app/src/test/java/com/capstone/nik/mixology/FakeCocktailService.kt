package com.capstone.nik.mixology

import com.capstone.nik.mixology.Network.CocktailService
import com.capstone.nik.mixology.Network.remoteModel.ApiBarAlmost
import com.capstone.nik.mixology.Network.remoteModel.ApiBarRecommendations
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
import com.capstone.nik.mixology.Network.remoteModel.CatalogListItem
import com.capstone.nik.mixology.Network.remoteModel.CatalogListResponse
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbResponse
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarMatcher

class FakeCocktailService : CocktailService {
    var alcohol = CocktailDbResponse()
    var glass = CocktailDbResponse()
    var ingredient = CocktailDbResponse()
    var drinkType = CocktailDbResponse()
    var alcoholByQuery: Map<String, CocktailDbResponse> = emptyMap()
    var glassByQuery: Map<String, CocktailDbResponse> = emptyMap()
    var ingredientsByQuery: Map<String, CocktailDbResponse> = emptyMap()
    var drinkTypesByQuery: Map<String, CocktailDbResponse> = emptyMap()
    var search = CocktailDbResponse()
    var lookup = CocktailDbResponse()
    var lookupsById: Map<String, CocktailDbResponse> = emptyMap()
    var letter = CocktailDbResponse()
    var categories = CatalogListResponse()
    var glasses = CatalogListResponse()
    var ingredients = CatalogListResponse()
    var alcoholic = CatalogListResponse()
    var recommendationDrinks: List<Drink> = emptyList()
    var failRecommendations = false
    var failLookup = false
    val lookedUp = mutableListOf<String>()

    override suspend fun getAlcoholFilter(filter: String) = alcoholByQuery[filter] ?: alcohol
    override suspend fun getGlassFilter(filter: String) = glassByQuery[filter] ?: glass
    override suspend fun getIngredientFilter(filter: String) = ingredientsByQuery[filter] ?: ingredient
    override suspend fun getDrinkTypeFilter(filter: String) = drinkTypesByQuery[filter] ?: drinkType
    override suspend fun getSearchResults(search: String) = this.search
    override suspend fun getDrinkById(id: String): CocktailDbResponse {
        lookedUp += id
        if (failLookup) error("lookup failed")
        return lookupsById[id] ?: lookup
    }
    override suspend fun getSearchByLetter(letter: String) = this.letter
    override suspend fun listCategories(list: String) = categories
    override suspend fun listGlasses(list: String) = glasses
    override suspend fun listIngredients(list: String) = ingredients
    override suspend fun listAlcoholic(list: String) = alcoholic
    override suspend fun getBarRecommendations(ingredients: List<String>): ApiBarRecommendations {
        if (failRecommendations) error("recommendations failed")
        val result = BarMatcher.recommend(recommendationDrinks, ingredients)
        return ApiBarRecommendations(
            ingredients = ingredients,
            makeable = result.makeable.map { it.toApiDrink() },
            almost = result.almost.map { ApiBarAlmost(it.drink.toApiDrink(), it.missing) },
        )
    }
}

fun cocktailDrink(
    id: String,
    name: String,
    thumb: String = "https://example.com/$id.jpg",
    instructions: String? = "Shake.",
    ingredient: String? = "Gin",
): CocktailDbDrink = CocktailDbDrink(
    idDrink = id,
    strDrink = name,
    strDrinkThumb = thumb,
    strInstructions = instructions,
    strIngredient1 = ingredient,
    strMeasure1 = "1 oz",
)

fun catalog(vararg names: String) = CatalogListResponse(
    drinks = names.map { CatalogListItem(strIngredient1 = it) },
)

private fun Drink.toApiDrink() = ApiDrink(
    id = id,
    name = name,
    thumb = thumb,
    alcoholic = alcoholic,
    glass = glass,
    category = category,
    iba = iba,
    instructions = instructions,
    video = video,
    ingredients = ingredients,
)
