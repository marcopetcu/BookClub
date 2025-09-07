package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bookclub.R
import kotlinx.coroutines.launch
import java.time.Instant

class ClubsFragment : Fragment(R.layout.fragment_clubs) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnCreate: Button = view.findViewById(R.id.btnCreateClub)

        // Observă lista de cluburi
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.clubs.collect { clubs ->
                // TODO: le legi la un RecyclerView sau Compose
            }
        }

        // Creează un club de test
        btnCreate.setOnClickListener {
            viewModel.createClub(
                adminId = 1L,
                workId = "OL12345W",
                title = "Club Test",
                author = "Anonim",
                coverUrl = null,
                description = "Primul club creat",
                startAt = Instant.now()
            )
        }
    }
}
