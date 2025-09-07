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

// Dacă ClubsAdapter este în ui.home, schimbă importul de mai jos:
// import com.example.bookclub.ui.home.ClubsAdapter
import com.example.bookclub.ui.club.ClubsAdapter

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerClubs)
        val empty = view.findViewById<TextView>(R.id.txtEmpty)

        val adapter = ClubsAdapter(
            onClick = { club: BookClubEntity ->
                // deschide detaliile clubului (opțional, fără join)
                // findNavController().navigate(
                //     HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(club.id)
                // )
            },
            onJoinClick = { club: BookClubEntity ->
                val userId = 1L // TODO: ia-l din SessionManager
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        viewModel.joinClub(userId, club.id)
                        Toast.makeText(requireContext(), getString(R.string.joined_club), Toast.LENGTH_SHORT).show()
                        // după join, mergi la ecranul de comentarii
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToClubDetailFragment(club.id)
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
            viewModel.clubs.collect { list ->
                adapter.submitList(list)
                empty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
