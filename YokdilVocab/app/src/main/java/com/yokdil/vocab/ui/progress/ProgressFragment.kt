package com.yokdil.vocab.ui.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yokdil.vocab.data.db.AppDatabase
import com.yokdil.vocab.data.repository.WordRepository
import com.yokdil.vocab.databinding.FragmentProgressBinding

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels {
        val db = AppDatabase.getDatabase(requireContext())
        ProgressViewModelFactory(WordRepository(db.wordDao()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
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

        viewModel.totalCorrect.observe(viewLifecycleOwner) {
            binding.tvCorrectCount.text = (it ?: 0).toString()
            updatePieChart()
        }

        viewModel.totalWrong.observe(viewLifecycleOwner) {
            binding.tvWrongCount.text = (it ?: 0).toString()
            updatePieChart()
        }

        // Completion percentage
        viewModel.learnedCount.observe(viewLifecycleOwner) { learned ->
            viewModel.totalCount.value?.let { total ->
                val pct = if (total > 0) (learned ?: 0) * 100 / total else 0
                binding.progressCompletion.progress = pct
                binding.tvCompletionPercent.text = "$pct%"
            }
        }
        viewModel.totalCount.observe(viewLifecycleOwner) { total ->
            viewModel.learnedCount.value?.let { learned ->
                val pct = if (total > 0) (learned) * 100 / total else 0
                binding.progressCompletion.progress = pct
                binding.tvCompletionPercent.text = "$pct%"
            }
        }
    }

    private fun updatePieChart() {
        val correct = viewModel.totalCorrect.value ?: 0
        val wrong = viewModel.totalWrong.value ?: 0
        if (correct == 0 && wrong == 0) return

        val entries = listOf(
            PieEntry(correct.toFloat(), "Doğru"),
            PieEntry(wrong.toFloat(), "Yanlış")
        )
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                requireContext().getColor(com.yokdil.vocab.R.color.colorCorrect),
                requireContext().getColor(com.yokdil.vocab.R.color.colorWrong)
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }
        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
