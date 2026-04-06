package com.yokdil.vocab.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yokdil.vocab.data.preferences.UserPreferences
import com.yokdil.vocab.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: UserPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = UserPreferences(requireContext())

        lifecycleScope.launch {
            val hour = prefs.notificationHour.first()
            val minute = prefs.notificationMinute.first()
            binding.btnPickTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

            val enabled = prefs.notificationsEnabled.first()
            binding.switchNotification.isChecked = enabled

            val count = prefs.flashcardCount.first()
            binding.btnCardCount.text = count.toString()
        }

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { prefs.setNotificationsEnabled(isChecked) }
        }

        binding.btnPickTime.setOnClickListener {
            lifecycleScope.launch {
                val hour = prefs.notificationHour.first()
                val minute = prefs.notificationMinute.first()
                TimePickerDialog(requireContext(), { _, h, m ->
                    binding.btnPickTime.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                    lifecycleScope.launch { prefs.setNotificationTime(h, m) }
                }, hour, minute, true).show()
            }
        }

        binding.btnCardCount.setOnClickListener {
            val options = arrayOf("20", "30", "50")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Kart Sayısı")
                .setItems(options) { _, idx ->
                    val count = options[idx].toInt()
                    binding.btnCardCount.text = count.toString()
                    lifecycleScope.launch { prefs.setFlashcardCount(count) }
                }.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
