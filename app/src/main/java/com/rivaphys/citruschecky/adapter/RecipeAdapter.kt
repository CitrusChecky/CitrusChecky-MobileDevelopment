package com.rivaphys.citruschecky.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rivaphys.citruschecky.data.ClassRecipes
import com.rivaphys.citruschecky.data.Recipe
import com.rivaphys.citruschecky.databinding.ItemResepClassBinding
import com.rivaphys.citruschecky.databinding.ItemResepKegunaanBinding

class RecipeAdapter(
    private val classRecipes: List<ClassRecipes>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_RECIPE = 1
    }

    private data class RecipeItem(
        val type: Int,
        val className: String? = null,
        val recipe: Recipe? = null
    )

    private val items: List<RecipeItem> = buildList {
        classRecipes.forEach { classRecipe ->
            // Add header for class name
            add(RecipeItem(VIEW_TYPE_HEADER, classRecipe.className))

            // Add all recipes for this class
            classRecipe.recipes.forEach { recipe ->
                add(RecipeItem(VIEW_TYPE_RECIPE, recipe = recipe))
            }
        }
    }

    class HeaderViewHolder(private val binding: ItemResepClassBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(className: String) {
            // Format class name untuk display yang lebih baik berdasarkan hasil deteksi
            val formattedClassName = when (className.lowercase()) {
                "matang" -> "Jeruk Matang"
                "sedikit-busuk" -> "Jeruk Sedikit Busuk"
                "sangat-busuk" -> "Jeruk Sangat Busuk"
                else -> "Jeruk ${className.replaceFirstChar { it.uppercase() }}"
            }
            binding.tvClassName.text = formattedClassName
        }
    }

    class RecipeViewHolder(private val binding: ItemResepKegunaanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.tvRecipeTitle.text = recipe.title
            binding.tvRecipeContent.text = recipe.ingredients
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemResepClassBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_RECIPE -> {
                val binding = ItemResepKegunaanBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                RecipeViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> {
                item.className?.let { holder.bind(it) }
            }
            is RecipeViewHolder -> {
                item.recipe?.let { holder.bind(it) }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}