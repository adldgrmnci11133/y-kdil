package com.yokdil.vocab.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.databinding.ItemWordBinding

class WordAdapter(
    private val onItemClick: (Word) -> Unit,
    private val onFavoriteToggle: (Word) -> Unit
) : ListAdapter<Word, WordAdapter.WordViewHolder>(WordDiffCallback()) {

    inner class WordViewHolder(private val binding: ItemWordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(word: Word) {
            binding.tvWord.text = word.word
            binding.tvMeaning.text = word.turkishMeaning
            binding.tvPartOfSpeech.text = word.partOfSpeech
            binding.btnFavorite.setImageResource(
                if (word.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            binding.root.setOnClickListener { onItemClick(word) }
            binding.btnFavorite.setOnClickListener { onFavoriteToggle(word) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
    override fun areItemsTheSame(oldItem: Word, newItem: Word) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Word, newItem: Word) = oldItem == newItem
}
