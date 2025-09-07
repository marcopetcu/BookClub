package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.ui.home.ClubsAdapter
import kotlinx.coroutines.launch
import java.time.Instant

class ClubsFragment : Fragment(R.layout.fragment_clubs) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler: RecyclerView = view.findViewById(R.id.recyclerClubs)
        val placeholder: TextView  = view.findViewById(R.id.txtClubsPlaceholder)
        val btnCreate: Button?     = view.findViewById(R.id.btnCreateClub) // dacă există în layout

        val adapter = ClubsAdapter { club ->
            // TODO: navigate către detalii club dacă ai un ecran dedicat
            // findNavController().navigate(...)
            // sau doar un feedback rapid:
            // Toast.makeText(requireContext(), club.title, Toast.LENGTH_SHORT).show()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        // Observă lista de cluburi și afișeaz-o
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.clubs.collect { clubs ->
                    adapter.submitList(clubs)
                    placeholder.visibility = if (clubs.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // Creează un club de test (dev only). Poți elimina butonul din layout în producție.
        btnCreate?.setOnClickListener {
            viewModel.createClub(
                adminId    = 1L,
                workId     = "OL12345W",
                title      = "Club Test",
                author     = "Anonim",
                coverUrl   = null,
                description= "Primul club creat",
                startAt    = Instant.now().plusSeconds(3600) // începe peste o oră
            )
        }
    }
}
