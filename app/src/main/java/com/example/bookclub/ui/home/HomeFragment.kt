package com.example.bookclub.ui.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.ui.club.ClubsViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: ClubsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerClubs)
        val empty = view.findViewById<TextView>(R.id.txtEmpty)
        val adapter = ClubsAdapter { club ->
            // TODO: navigate la detaliul clubului (când vei avea ecranul)
            // findNavController().navigate(...)
        }
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
