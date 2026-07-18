package com.example.recipe_book.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recipe_book.R
import com.example.recipe_book.databinding.ItemRecipeBinding
import com.example.recipe_book.domain.model.Recipe
import com.example.recipe_book.util.gone
import com.example.recipe_book.util.visible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy


/**
 * ListAdapter + DiffUtil per blueprint Section 10.3 — avoids full-list
 * notifyDataSetChanged() re-renders on every Firestore snapshot update.
 * Click lambdas are passed in rather than referencing the Fragment
 * directly, keeping the adapter unit-testable in isolation.
 *
 * currentUserId is read fresh on every bind (not baked into the diff key)
 * so edit/delete icon visibility is always correct even if the signed-in
 * user changes between submitList() calls — a full rebind is cheap here.
 */
class RecipeAdapter(
    private val currentUserId: () -> String?,
    private val onItemClick: (Recipe) -> Unit,
    private val onEditClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.textTitle.text = recipe.title
            binding.textAuthor.text = binding.root.context.getString(R.string.author_format, recipe.authorName)
            binding.chipCategory.text = recipe.category

            Glide.with(binding.imageRecipe)
                .load(recipe.imageUrl.ifBlank { com.example.recipe_book.util.Constants.DEFAULT_RECIPE_IMAGE })
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(binding.imageRecipe)

            val isOwner = recipe.authorId.isNotBlank() && recipe.authorId == currentUserId()
            if (isOwner) binding.layoutOwnerActions.visible() else binding.layoutOwnerActions.gone()

            binding.root.setOnClickListener { onItemClick(recipe) }
            binding.buttonEdit.setOnClickListener { onEditClick(recipe) }
            binding.buttonDelete.setOnClickListener { onDeleteClick(recipe) }
        }
    }

    private object RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe) = oldItem == newItem
    }
}
