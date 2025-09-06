package com.example.bookclub.ui.books

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.launch

class BooksFragment : Fragment(R.layout.fragment_books) {

    private val viewModel: BooksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BooksAdapter { book ->
            viewModel.loadWorkDetails(book.key)
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_books)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val searchBtn = view.findViewById<Button>(R.id.btn_search)
        val searchEt = view.findViewById<EditText>(R.id.et_search)

        searchBtn.setOnClickListener {
            val query = searchEt.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(requireContext(), "Scrie un termen de căutare", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.search(query)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchState.collect { state ->
                when (state) {
                    is UiState.Loading -> { /* TO DO PROGRESS BAR*/ }
                    is UiState.Success -> adapter.submitList(state.data)
                    is UiState.Error -> Toast.makeText(requireContext(), "Eroare la căutare", Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }
        }
    }
}
