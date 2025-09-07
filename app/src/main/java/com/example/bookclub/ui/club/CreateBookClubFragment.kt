package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bookclub.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.Instant

class CreateBookClubFragment : Fragment(R.layout.fragment_create_book_club) {

    private val args: CreateBookClubFragmentArgs by navArgs()
    private val vm: CreateClubViewModel by viewModels()

    private lateinit var etTitle: TextInputEditText
    private lateinit var etAuthor: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnCreate: Button

    // TODO: ia userId din SessionManager
    private val currentUserId: Long = 1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle = view.findViewById(R.id.etTitle)
        etAuthor = view.findViewById(R.id.etAuthor)
        etDescription = view.findViewById(R.id.etDescription)
        btnCreate = view.findViewById(R.id.btnCreate)

        // pre-fill din args (vin din BookDetailFragment)
        etTitle.setText(args.title)
        etAuthor.setText(args.author)

        btnCreate.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            if (title.isBlank()) {
                etTitle.error = getString(R.string.hint_title)
                return@setOnClickListener
            }

            val author = etAuthor.text?.toString()?.trim().orEmpty()
            val description = etDescription.text?.toString()?.trim()
            val startAt = Instant.now().plusSeconds(60 * 60) // default: peste 1h

            vm.createClub(
                adminId = currentUserId,
                workId = args.workId,
                title = title,
                author = author,
                description = description,
                coverUrl = args.coverUrl,
                startAt = startAt
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.state.collect { st ->
                when (st) {
                    CreateState.Idle -> Unit
                    CreateState.Loading -> btnCreate.isEnabled = false
                    is CreateState.Success -> {
                        btnCreate.isEnabled = true
                        Snackbar.make(
                            requireView(),
                            getString(R.string.club_created_fmt, st.clubId),
                            Snackbar.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()
                        vm.reset()
                    }
                    is CreateState.Error -> {
                        btnCreate.isEnabled = true
                        val msg = if (st.t is IllegalStateException) {
                            getString(R.string.err_club_duplicate)
                        } else st.t.message ?: getString(R.string.error_unknown)
                        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
