package com.yokdil.vocab.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yokdil.vocab.R
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.databinding.FragmentHomeBinding
import com.yokdil.vocab.ui.flashcard.FlashcardViewModel
import com.yokdil.vocab.ui.flashcard.FlashcardViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        HomeViewModelFactory(WordRepository(db.wordDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.learnedCount.observe(viewLifecycleOwner) {
            binding.tvLearnedCount.text = (it ?: 0).toString()
        }
        viewModel.totalCount.observe(viewLifecycleOwner) {
            binding.tvTotalCount.text = (it ?: 0).toString()
        }

        binding.btnFlashcard.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_flashcard)
        }
        binding.btnQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_quiz)
        }
        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_favorites)
        }
        binding.btnProgress.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_progress)
        }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
