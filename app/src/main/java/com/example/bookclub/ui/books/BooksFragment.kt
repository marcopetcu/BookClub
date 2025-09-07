package com.example.bookclub.ui.books

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import com.example.bookclub.data.model.BookSearchItem
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.launch

class BooksFragment : Fragment(R.layout.fragment_books) {

    private val viewModel: BooksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch = view.findViewById<EditText>(R.id.et_search)
        val btnSearch = view.findViewById<Button>(R.id.btn_search)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler_books)

        val adapter = BooksAdapter { book: BookSearchItem ->
            val workId = book.key.substringAfterLast("/")

            val bundle = BookDetailFragmentArgs(
                workId  = workId,
                title   = book.title,
                author  = book.author.orEmpty(),
                coverUrl = book.coverUrl.orEmpty()
            ).toBundle()

            findNavController().navigate(R.id.bookDetailFragment, bundle)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        fun triggerSearch() {
            val q = etSearch.text?.toString()?.trim().orEmpty()
            if (q.isNotEmpty()) viewModel.searchBooks(q)
        }

        btnSearch.setOnClickListener { triggerSearch() }
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                triggerSearch(); true
            } else false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchState.collect { state ->
                    when (state) {
                        UiState.Idle -> Unit
                        is UiState.Loading -> btnSearch.isEnabled = false
                        is UiState.Success -> {
                            btnSearch.isEnabled = true
                            adapter.submitList(state.data) // List<BookSearchItem>
                        }
                        is UiState.Error -> btnSearch.isEnabled = true
                    }
                }
            }
        }
    }
}
