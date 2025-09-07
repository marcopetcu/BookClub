package com.example.bookclub.ui.club

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import kotlinx.coroutines.launch
import java.time.Instant

class ClubsFragment : Fragment(R.layout.fragment_clubs) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler: RecyclerView = view.findViewById(R.id.recyclerClubs)
        val placeholder: TextView  = view.findViewById(R.id.txtClubsPlaceholder)
        val btnCreate: Button?     = view.findViewById(R.id.btnCreateClub)

        val adapter = ClubsAdapter(
            onClick = { club ->
                // navigare la detalii fără join (opțional)
                findNavController().navigate(
                    ClubsFragmentDirections.actionClubsFragmentToClubDetailFragment(clubId = club.id)
                )
            },
            onJoinClick = { club ->
                // TODO: userId real din SessionManager
                val userId = 1L
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        viewModel.joinClub(userId, club.id)
                        Toast.makeText(requireContext(), getString(R.string.joined_club), Toast.LENGTH_SHORT).show()
                        // după join → direct în ecranul de comentarii
                        findNavController().navigate(
                            ClubsFragmentDirections.actionClubsFragmentToClubDetailFragment(clubId = club.id)
                        )
                    } catch (t: Throwable) {
                        Toast.makeText(requireContext(), t.message ?: "Join failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.clubs.collect { clubs ->
                    adapter.submitList(clubs)
                    placeholder.visibility = if (clubs.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // buton dev: creează un club rapid
        btnCreate?.setOnClickListener {
            viewModel.createClub(
                adminId    = 1L,
                workId     = "OL12345W",
                title      = "Club Test",
                author     = "Anonim",
                coverUrl   = null,
                description= "Primul club creat",
                startAt    = Instant.now().plusSeconds(3600)
            )
        }
    }
}
