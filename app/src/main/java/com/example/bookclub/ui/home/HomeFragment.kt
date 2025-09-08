package com.example.bookclub.ui.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.ui.club.ClubsViewModel
import com.example.bookclub.ui.club.UiClub
import com.example.bookclub.ui.club.ClubsAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler: RecyclerView = view.findViewById(R.id.recyclerClubs)
        val empty: TextView = view.findViewById(R.id.txtEmpty)

        val adapter = ClubsAdapter(
            onPrimaryClick = { ui ->
                if (ui.isMember) {
                    val action = HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(
                        clubId = ui.club.id,
                        title = ui.club.title,
                        coverUrl = ui.club.coverUrl ?: ""
                    )
                    findNavController().navigate(action)
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.joinClub(ui.club.id)
                        Toast.makeText(requireContext(), getString(R.string.joined_club), Toast.LENGTH_SHORT).show()
                        // poți naviga automat după join, dacă vrei:
                        val action = HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(
                            clubId = ui.club.id,
                            title = ui.club.title,
                            coverUrl = ui.club.coverUrl ?: ""
                        )
                        findNavController().navigate(action)
                    }
                }
            },
            onLeaveClick = { ui ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.leaveClub(ui.club.id)
                    Toast.makeText(requireContext(), getString(R.string.left_club), Toast.LENGTH_SHORT).show()
                }
            },
            onCardClick = { ui ->
                if (ui.isMember) {
                    val action = HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiClubs.collect { list ->
                adapter.submitList(list)
                empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
