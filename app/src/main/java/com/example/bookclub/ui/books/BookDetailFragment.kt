package com.example.bookclub.ui.books

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.bookclub.R
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment(R.layout.fragment_book_detail) {

    private val args: BookDetailFragmentArgs by navArgs()
    private val viewModel: BooksViewModel by viewModels()

    private lateinit var imgCover: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtSubjects: TextView
    private lateinit var txtDescription: TextView
    private lateinit var btnFollow: Button

    // TODO: ia userId real din SessionManager când M3 e gata
    private val userId = 1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgCover = view.findViewById(R.id.imgCover)
        txtTitle = view.findViewById(R.id.txtTitle)
        txtSubjects = view.findViewById(R.id.txtSubjects)
        txtDescription = view.findViewById(R.id.txtDescription)
        btnFollow = view.findViewById(R.id.btnFollow)

        val workId = args.workId

        // 1) Detalii + starea de follow pentru cartea curentă
        viewModel.loadWorkDetails(workId)
        viewModel.loadFollowState(userId, workId)

        // 2) Observă detaliile
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detailsState.collect { state ->
                when (state) {
                    UiState.Idle -> Unit
                    is UiState.Loading -> btnFollow.isEnabled = false
                    is UiState.Success -> {
                        btnFollow.isEnabled = true
                        val d = state.data
                        txtTitle.text = d.title
                        txtSubjects.isVisible = d.subjects.isNotEmpty()
                        txtSubjects.text = d.subjects.joinToString(" • ")
                        txtDescription.text = d.description ?: getString(R.string.no_description)

                        imgCover.load(d.coverUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_book_placeholder)
                            error(R.drawable.ic_book_placeholder)
                        }
                    }
                    is UiState.Error -> {
                        val msg = state.throwable.message ?: getString(R.string.error_unknown)
                        txtDescription.text = getString(R.string.error_fmt, msg)
                        btnFollow.isEnabled = false
                    }
                }
            }
        }

        // 3) Observă starea de follow din ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFollowed.collect { followed ->
                btnFollow.text =
                    if (followed) getString(R.string.unfollow) else getString(R.string.follow)
                btnFollow.isEnabled = true
            }
        }

        // 4) Click Follow/Unfollow (Varianta A: repo fără title/cover)
        btnFollow.setOnClickListener {
            if (viewModel.isFollowed.value) {
                viewModel.unfollowBook(userId, workId)
            } else {
                viewModel.followBook(userId, workId)
            }
            btnFollow.isEnabled = false // se reactivează după ce VM emite noua stare
        }
    }
}
