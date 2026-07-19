package com.example.recipe_book.data.remote

import com.example.recipe_book.data.remote.dto.RecipeDto
import com.example.recipe_book.util.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirestoreRecipeDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val recipesCollection get() = firestore.collection(Constants.FIRESTORE_RECIPES_COLLECTION)

    fun generateRecipeId(): String = recipesCollection.document().id

    suspend fun addRecipe(id: String, dto: RecipeDto) {
        recipesCollection.document(id).set(dto).await()
    }

    suspend fun updateRecipe(id: String, dto: RecipeDto) {
        recipesCollection.document(id).set(dto).await()
    }

    suspend fun deleteRecipe(id: String) {
        recipesCollection.document(id).delete().await()
    }

    suspend fun getRecipeById(id: String): Pair<String, RecipeDto>? {
        val snapshot = recipesCollection.document(id).get().await()
        return snapshot.toObject(RecipeDto::class.java)?.let { id to it }
    }

    fun observeAllRecipes(): Flow<List<Pair<String, RecipeDto>>> = observeQuery(
        recipesCollection.orderBy("createdAt", Query.Direction.DESCENDING)
    )

    fun observeRecipesByCategory(category: String): Flow<List<Pair<String, RecipeDto>>> = observeQuery(
        recipesCollection
            .whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
    )

    fun observeRecipesByAuthor(authorId: String): Flow<List<Pair<String, RecipeDto>>> = observeQuery(
        recipesCollection
            .whereEqualTo("authorId", authorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
    )

    private fun observeQuery(query: Query): Flow<List<Pair<String, RecipeDto>>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // propagates as a Flow exception, caught by the repository
                return@addSnapshotListener
            }
            val recipes = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(RecipeDto::class.java)?.let { doc.id to it }
            } ?: emptyList()
            trySend(recipes)
        }
        awaitClose { registration.remove() }
    }
}
