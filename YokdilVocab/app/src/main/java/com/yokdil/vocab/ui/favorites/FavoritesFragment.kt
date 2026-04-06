package com.yokdil.vocab.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yokdil.vocab.R
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.databinding.FragmentFavoritesBinding

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoritesViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        FavoritesViewModelFactory(WordRepository(db.wordDao()))
    }

    private lateinit var adapter: WordAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WordAdapter(
            onItemClick = { word ->
                val bundle = Bundle().apply { putInt("wordId", word.id) }
                findNavController().navigate(R.id.action_favorites_to_detail, bundle)
            },
            onFavoriteToggle = { word ->
                viewModel.toggleFavorite(word)
            }
        )

        binding.recyclerFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFavorites.adapter = adapter

        viewModel.words.observe(viewLifecycleOwner) { words ->
            adapter.submitList(words)
            binding.tvEmpty.visibility = if (words.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.etSearch.doAfterTextChanged { text ->
            viewModel.search(text.toString())
        }

        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.showAll()
        }

        binding.chipUnlearned.setOnCheckedChangeListener { _, checked ->
            if (checked) viewModel.showUnlearned()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
