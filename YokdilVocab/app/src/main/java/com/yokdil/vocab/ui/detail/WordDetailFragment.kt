package com.yokdil.vocab.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.databinding.FragmentWordDetailBinding
import com.yokdil.vocab.utils.parseAntonyms
import com.yokdil.vocab.utils.parseSynonyms

class WordDetailFragment : Fragment() {

    private var _binding: FragmentWordDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WordDetailViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        WordDetailViewModelFactory(WordRepository(db.wordDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val wordId = arguments?.getInt("wordId") ?: return
        viewModel.loadWord(wordId)

        viewModel.word.observe(viewLifecycleOwner) { word ->
            word ?: return@observe
            binding.tvWord.text = word.word
            binding.tvPartOfSpeech.text = word.partOfSpeech
            binding.tvExamYear.text = if (word.examYear.isNotEmpty()) "YÖKDİL ${word.examYear}" else ""
            binding.tvMeaning.text = word.turkishMeaning
            binding.tvSynonyms.text = word.parseSynonyms().joinToString(" • ")
            binding.tvAntonyms.text = word.parseAntonyms().joinToString(" • ")
            binding.tvExample.text = word.exampleSentence
            binding.tvExampleTranslation.text = word.exampleTranslation

            val favIcon = if (word.isFavorite) android.R.drawable.btn_star_big_on
                          else android.R.drawable.btn_star_big_off
            binding.btnFavorite.setImageResource(favIcon)

            binding.btnFavorite.setOnClickListener {
                viewModel.toggleFavorite(word)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
