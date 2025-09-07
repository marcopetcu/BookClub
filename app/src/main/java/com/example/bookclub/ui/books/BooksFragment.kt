package com.example.bookclub.ui.books

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.launch

class BooksFragment : Fragment(R.layout.fragment_books) {

    private val viewModel: BooksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Adapter cu callback de click → navigare la detalii
        val adapter = BooksAdapter { book ->
            val workId = book.key.substringAfterLast("/")   // "/works/OL82563W" -> "OL82563W"
            val action = BooksFragmentDirections
                .actionBooksFragmentToBookDetailFragment(workId)
            findNavController().navigate(action)
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

        // 2) Colectează state-ul de căutare corect pe lifecycle
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    when (state) {
                        is UiState.Loading -> { /* TODO: arată un loader */ }
                        is UiState.Success -> adapter.submitList(state.data)
                        is UiState.Error -> Toast.makeText(
                            requireContext(),
                            "Eroare la căutare: ${state.throwable?.message ?: "necunoscută"}",
                            Toast.LENGTH_SHORT
                        ).show()
                        UiState.Idle -> Unit
                    }
                }
            }
        }
    }
}
