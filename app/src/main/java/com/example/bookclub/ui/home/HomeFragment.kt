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
import com.example.bookclub.data.db.BookClubEntity
import com.example.bookclub.ui.club.ClubsViewModel
import kotlinx.coroutines.launch

// dacă ai mutat adapterul în alt pachet, ajustează importul
import com.example.bookclub.ui.club.ClubsAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerClubs)
        val empty = view.findViewById<TextView>(R.id.txtEmpty)

        val adapter = ClubsAdapter(
            onClick = { club: BookClubEntity ->
                // navigare cu Safe Args — PASĂM ARGUMENTELE DIRECT ÎN FUNCȚIE
                val action = HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(
                    clubId = club.id,
                    title = club.title,
                    coverUrl = club.coverUrl ?: ""
                )
                findNavController().navigate(action)
            },
            onJoinClick = { club: BookClubEntity ->
                val userId = 1L // TODO: din SessionManager
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        viewModel.joinClub(userId, club.id)
                        Toast.makeText(requireContext(), getString(R.string.joined_club), Toast.LENGTH_SHORT).show()

                        val action = HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(
                            clubId = club.id,
                            title = club.title,
                            coverUrl = club.coverUrl ?: ""
                        )
                        findNavController().navigate(action)
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
            viewModel.clubs.collect { list ->
                adapter.submitList(list)
                empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
