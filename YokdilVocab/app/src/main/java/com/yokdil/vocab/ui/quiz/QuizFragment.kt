package com.yokdil.vocab.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.yokdil.vocab.R
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        QuizViewModelFactory(WordRepository(db.wordDao()))
    }

    private val optionButtons: List<MaterialButton> by lazy {
        listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadQuiz(10)

        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            question ?: return@observe
            resetOptions()

            val typeLabel = when (question.type) {
                QuizType.MEANING -> "ANLAM"
                QuizType.SYNONYM -> "EŞ ANLAMLI"
                QuizType.ANTONYM -> "ZIT ANLAMLI"
            }
            binding.tvQuestionType.text = typeLabel

            val questionText = when (question.type) {
                QuizType.MEANING -> "\"${question.word.word}\" kelimesinin Türkçe anlamı nedir?"
                QuizType.SYNONYM -> "\"${question.word.word}\" kelimesinin eş anlamlısı hangisidir?"
                QuizType.ANTONYM -> "\"${question.word.word}\" kelimesinin zıt anlamlısı hangisidir?"
            }
            binding.tvQuestion.text = questionText

            question.options.forEachIndexed { index, option ->
                optionButtons[index].text = option
                optionButtons[index].setOnClickListener {
                    viewModel.selectAnswer(option)
                }
            }

            binding.feedbackCard.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
        }

        viewModel.progress.observe(viewLifecycleOwner) { (current, total) ->
            binding.tvQuizProgress.text = "Soru $current / $total"
            binding.quizProgressBar.max = total
            binding.quizProgressBar.progress = current
        }

        viewModel.score.observe(viewLifecycleOwner) {
            binding.tvScore.text = "Puan: $it"
        }

        viewModel.selectedAnswer.observe(viewLifecycleOwner) { selected ->
            selected ?: return@observe
            val correct = viewModel.currentQuestion.value?.correctAnswer ?: return@observe
            showFeedback(selected, correct)
        }

        viewModel.quizFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                val result = viewModel.getResult()
                binding.tvFinalScore.text = "Sonuç: ${result.correctAnswers} / ${result.totalQuestions}"
                binding.resultOverlay.visibility = View.VISIBLE
            }
        }

        binding.btnNext.setOnClickListener { viewModel.nextQuestion() }

        binding.btnRetry.setOnClickListener {
            binding.resultOverlay.visibility = View.GONE
            viewModel.loadQuiz(10)
        }

        binding.btnResultHome.setOnClickListener { findNavController().navigateUp() }
    }

    private fun showFeedback(selected: String, correct: String) {
        val isCorrect = selected == correct
        binding.feedbackCard.setCardBackgroundColor(
            if (isCorrect) requireContext().getColor(R.color.colorCorrect)
            else requireContext().getColor(R.color.colorWrong)
        )
        binding.tvFeedback.text = if (isCorrect) "Doğru!" else "Yanlış!"
        binding.tvFeedback.setTextColor(Color.WHITE)
        binding.tvCorrectAnswer.text = if (!isCorrect) "Doğru Cevap: $correct" else ""
        binding.tvCorrectAnswer.setTextColor(Color.WHITE)
        binding.feedbackCard.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE

        // Highlight buttons
        optionButtons.forEach { btn ->
            when (btn.text.toString()) {
                correct -> {
                    btn.setBackgroundColor(requireContext().getColor(R.color.colorCorrect))
                    btn.setTextColor(Color.WHITE)
                }
                selected -> if (selected != correct) {
                    btn.setBackgroundColor(requireContext().getColor(R.color.colorWrong))
                    btn.setTextColor(Color.WHITE)
                }
            }
            btn.isEnabled = false
        }
    }

    private fun resetOptions() {
        val primaryColor = requireContext().getColor(R.color.colorPrimary)
        val textPrimary = requireContext().getColor(R.color.colorTextPrimary)
        optionButtons.forEach { btn ->
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(textPrimary)
            btn.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
