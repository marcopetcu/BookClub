package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
            onPrimaryClick = { ui ->
                if (ui.isMember) {
                    // Open -> mergi la ecranul de comentarii
                    val action = ClubsFragmentDirections.actionClubsFragmentToClubDetailFragment(
                        clubId = ui.club.id,
                        title = ui.club.title,
                        coverUrl = ui.club.coverUrl ?: ""
                    )
                    findNavController().navigate(action)
                } else {
                    // Join -> înscrie userul și (opțional) navighează
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            viewModel.joinClub(ui.club.id)
                            Toast.makeText(requireContext(), getString(R.string.joined_club), Toast.LENGTH_SHORT).show()

                            val action = ClubsFragmentDirections.actionClubsFragmentToClubDetailFragment(
                                clubId = ui.club.id,
                                title = ui.club.title,
                                coverUrl = ui.club.coverUrl ?: ""
                            )
                            findNavController().navigate(action)
                        } catch (t: Throwable) {
                            Toast.makeText(
                                requireContext(),
                                t.message ?: getString(R.string.join_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            },
            onLeaveClick = { ui ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        viewModel.leaveClub(ui.club.id)
                        Toast.makeText(requireContext(), getString(R.string.left_club), Toast.LENGTH_SHORT).show()
                    } catch (t: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            t.message ?: getString(R.string.leave_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onCardClick = { ui ->
                // dacă e membru, deschide comentariile și la tap pe card
                if (ui.isMember) {
                    val action = ClubsFragmentDirections.actionClubsFragmentToClubDetailFragment(
                        clubId = ui.club.id,
                        title = ui.club.title,
                        coverUrl = ui.club.coverUrl ?: ""
                    )
                    findNavController().navigate(action)
                }
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        // observăm lista UI (UiClub)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiClubs.collect { list ->
                adapter.submitList(list)
                placeholder.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }


    }
}
