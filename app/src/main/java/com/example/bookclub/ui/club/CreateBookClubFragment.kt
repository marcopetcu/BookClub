package com.example.bookclub.ui.club

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bookclub.R
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CreateBookClubFragment : Fragment(R.layout.fragment_create_book_club) {

    private val args: CreateBookClubFragmentArgs by navArgs()
    private val vm: CreateClubViewModel by viewModels()

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var txtStartDate: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnCreate: Button

    private var pickedDate: LocalDate? = null
    private var pickedTime: LocalTime? = null
    private var startAtInstant: Instant = Instant.now()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle       = view.findViewById(R.id.etTitle)
        etAuthor      = view.findViewById(R.id.etAuthor)
        etDescription = view.findViewById(R.id.etDescription)
        txtStartDate  = view.findViewById(R.id.txtStartDate)
        btnPickDate   = view.findViewById(R.id.btnPickDate)
        btnCreate     = view.findViewById(R.id.btnCreate)

        // Prefill din Safe Args
        etTitle.setText(args.title)
        etAuthor.setText(args.author)

        // Implicit: mâine la aceeași oră
        val zdtDefault = ZonedDateTime.now().plusDays(1)
        applyPicked(zdtDefault.toLocalDate(), zdtDefault.toLocalTime())

        btnPickDate.setOnClickListener { openPickers() }

        btnCreate.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val author = etAuthor.text?.toString()?.trim().orEmpty().ifEmpty { "Unknown" }
            val description = etDescription.text?.toString()?.trim()

            if (title.isBlank()) {
                etTitle.error = getString(R.string.hint_title)
                return@setOnClickListener
            }

            // TODO: ia adminId din SessionManager; fallback pt. dev:
            val adminId = 1L

            vm.createClub(
                adminId = adminId,
                workId = args.workId,
                title = title,
                author = author,
                description = description,
                coverUrl = args.coverUrl.ifEmpty { null },
                startAt = startAtInstant
            )
        }

        // Observă starea și navighează la Home la succes (back stack curat)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collectLatest { st ->
                    when (st) {
                        is CreateState.Idle -> Unit
                        is CreateState.Loading -> btnCreate.isEnabled = false
                        is CreateState.Success -> {
                            btnCreate.isEnabled = true
                            Toast.makeText(requireContext(),
                                "Club created (id=${st.clubId})", Toast.LENGTH_SHORT).show()
                            vm.reset()

                            val opts = NavOptions.Builder()
                                .setPopUpTo(R.id.navigation_home, /*inclusive=*/false)
                                .setLaunchSingleTop(true)
                                .build()
                            findNavController().navigate(R.id.homeFragment, null, opts)
                        }
                        is CreateState.Error -> {
                            btnCreate.isEnabled = true
                            Toast.makeText(requireContext(),
                                st.t.message ?: "Create failed", Toast.LENGTH_LONG).show()
                            vm.reset()
                        }
                    }
                }
            }
        }
    }

    private fun openPickers() {
        val now = ZonedDateTime.now()
        val d = pickedDate ?: now.toLocalDate()
        val t = pickedTime ?: now.toLocalTime()

        DatePickerDialog(requireContext(), { _, y, m, day ->
            val date = LocalDate.of(y, m + 1, day)
            TimePickerDialog(requireContext(), { _, hh, mm ->
                val time = LocalTime.of(hh, mm)
                applyPicked(date, time)
            }, t.hour, t.minute, true).show()
        }, d.year, d.monthValue - 1, d.dayOfMonth).apply {
            datePicker.minDate = System.currentTimeMillis() // blochează trecutul
        }.show()
    }

    private fun applyPicked(date: LocalDate, time: LocalTime) {
        pickedDate = date
        pickedTime = time
        val zdt = ZonedDateTime.of(date, time, ZoneId.systemDefault())
        startAtInstant = zdt.toInstant()
        txtStartDate.text = zdt.toString() // pune formatter dacă vrei alt format
    }
}
