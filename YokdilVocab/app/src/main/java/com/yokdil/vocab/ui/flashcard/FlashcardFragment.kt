package com.yokdil.vocab.ui.flashcard

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.yokdil.vocab.R
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.db.entities.Word
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.databinding.FragmentFlashcardBinding
import com.yokdil.vocab.utils.parseAntonyms
import com.yokdil.vocab.utils.parseSynonyms

class FlashcardFragment : Fragment() {

    private var _binding: FragmentFlashcardBinding? = null
    private val binding get() = _binding!!
    private var isFlipped = false

    private val viewModel: FlashcardViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        FlashcardViewModelFactory(WordRepository(db.wordDao()))
    }

    private lateinit var frontAnim: AnimatorSet
    private lateinit var backAnim: AnimatorSet
    private val scale by lazy { requireContext().resources.displayMetrics.density }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFlashcardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFlipAnimation()
        viewModel.loadWords()

        viewModel.currentWord.observe(viewLifecycleOwner) { word ->
            word?.let { bindWord(it) }
        }

        viewModel.progress.observe(viewLifecycleOwner) { (current, total) ->
            binding.tvProgress.text = "$current / $total"
            if (total > 0) {
                binding.progressBar.max = total
                binding.progressBar.progress = current
            }
        }

        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                binding.finishOverlay.visibility = View.VISIBLE
            }
        }

        // Touch on card to flip
        binding.cardContainer.setOnClickListener {
            if (viewModel.isFinished.value == true) return@setOnClickListener
            flipCard()
            viewModel.flip()
        }

        binding.btnKnown.setOnClickListener {
            resetFlip()
            viewModel.markKnown()
        }

        binding.btnUnknown.setOnClickListener {
            resetFlip()
            viewModel.markUnknown()
        }

        binding.fabFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        viewModel.currentWord.observe(viewLifecycleOwner) { word ->
            if (word != null) {
                val favIcon = if (word.isFavorite)
                    android.R.drawable.btn_star_big_on
                else
                    android.R.drawable.btn_star_big_off
                binding.fabFavorite.setImageResource(favIcon)
            }
        }

        binding.btnRestart.setOnClickListener {
            binding.finishOverlay.visibility = View.GONE
            viewModel.loadWords()
        }

        binding.btnGoHome.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun bindWord(word: Word) {
        binding.tvWord.text = word.word
        binding.tvPartOfSpeech.text = word.partOfSpeech
        binding.tvDifficulty.text = if (word.difficulty == 3) "C1 - İleri" else "B2 - Orta"
        binding.tvWordBack.text = word.word
        binding.tvMeaning.text = word.turkishMeaning
        binding.tvSynonyms.text = word.parseSynonyms().joinToString(", ")
        binding.tvAntonyms.text = word.parseAntonyms().joinToString(", ")
        binding.tvExample.text = word.exampleSentence
        binding.tvExampleTranslation.text = word.exampleTranslation
    }

    private fun Word.parseAntonyms(): List<String> {
        return try {
            com.google.gson.Gson().fromJson(antonyms, Array<String>::class.java).toList()
        } catch (e: Exception) { listOf(antonyms) }
    }

    private fun setupFlipAnimation() {
        val scale = requireContext().resources.displayMetrics.density
        binding.cardContainer.cameraDistance = 8000 * scale

        frontAnim = AnimatorInflater.loadAnimator(requireContext(), R.anim.front_animator) as AnimatorSet
        backAnim = AnimatorInflater.loadAnimator(requireContext(), R.anim.back_animator) as AnimatorSet
    }

    private fun flipCard() {
        if (!isFlipped) {
            frontAnim.setTarget(binding.cardFront)
            backAnim.setTarget(binding.cardBack)
            frontAnim.start()
            backAnim.start()
            binding.cardBack.visibility = View.VISIBLE
            binding.cardFront.visibility = View.GONE
            isFlipped = true
        } else {
            frontAnim.setTarget(binding.cardBack)
            backAnim.setTarget(binding.cardFront)
            frontAnim.start()
            backAnim.start()
            binding.cardFront.visibility = View.VISIBLE
            binding.cardBack.visibility = View.GONE
            isFlipped = false
        }
    }

    private fun resetFlip() {
        isFlipped = false
        binding.cardFront.visibility = View.VISIBLE
        binding.cardBack.visibility = View.GONE
        binding.cardFront.rotationY = 0f
        binding.cardBack.rotationY = 0f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
