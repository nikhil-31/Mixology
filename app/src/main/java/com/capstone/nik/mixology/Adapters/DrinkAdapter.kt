package com.capstone.nik.mixology.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.DrinkListItem
import com.squareup.picasso.Picasso

class DrinkAdapter(
    private val onItemSelected: (Cocktail) -> Unit,
    private val onToggleSaved: (DrinkListItem) -> Unit,
) : ListAdapter<DrinkListItem, DrinkAdapter.ViewHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_main, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.cocktail_image)
        private val textView: TextView = itemView.findViewById(R.id.cocktail_text)
        private val favorite: ImageView = itemView.findViewById(R.id.cocktail_button)

        fun bind(item: DrinkListItem) {
            textView.text = item.name
            Picasso.get().load(item.thumb).error(R.drawable.empty_glass).into(image)
            favorite.setImageResource(
                if (item.saved) R.drawable.ic_fav_filled else R.drawable.ic_fav_unfilled_black,
            )
            favorite.setOnClickListener { onToggleSaved(item) }
            itemView.setOnClickListener { onItemSelected(item.toCocktail()) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<DrinkListItem>() {
        override fun areItemsTheSame(oldItem: DrinkListItem, newItem: DrinkListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DrinkListItem, newItem: DrinkListItem): Boolean {
            return oldItem == newItem
        }
    }
}
