package com.example.bookclub.ui.books

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.bookclub.R
import com.example.bookclub.data.ServiceLocator
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
    private lateinit var btnCreateClub: Button
    private lateinit var btnOpenWeb: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgCover = view.findViewById(R.id.imgCover)
        txtTitle = view.findViewById(R.id.txtTitle)
        txtSubjects = view.findViewById(R.id.txtSubjects)
        txtDescription = view.findViewById(R.id.txtDescription)
        btnFollow = view.findViewById(R.id.btnFollow)
        btnCreateClub = view.findViewById(R.id.btnCreateClub)
        btnOpenWeb = view.findViewById(R.id.btnOpenWeb)

        val workId = args.workId

        // ✅ Session & role – inside onViewCreated
        val session = ServiceLocator.sessionManager(requireContext()).get()
        val currentUserId = session?.userId ?: 0L
        val isAdmin = session?.role?.trim()?.equals("admin", ignoreCase = true) == true

        // Show Create Club only to admins
        btnCreateClub.isVisible = isAdmin

        // Load details + follow state
        viewModel.loadWorkDetails(workId)
        viewModel.loadFollowState(currentUserId, workId)

        // Observe details state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.detailsState.collect { state ->
                when (state) {
                    UiState.Idle -> Unit

                    is UiState.Loading -> {
                        btnFollow.isEnabled = false
                    }

                    is UiState.Success -> {
                        btnFollow.isEnabled = true
                        val d = state.data

                        txtTitle.text = d.title.ifBlank { args.title }
                        txtSubjects.isVisible = d.subjects.isNotEmpty()
                        txtSubjects.text = d.subjects.joinToString(" • ")
                        txtDescription.text =
                            d.description ?: getString(R.string.no_description)

                        imgCover.load(d.coverUrl ?: args.coverUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_book_placeholder)
                            error(R.drawable.ic_book_placeholder)
                        }

                        // Create Club (admins only)
                        btnCreateClub.setOnClickListener {
                            val action =
                                BookDetailFragmentDirections
                                    .actionBookDetailFragmentToCreateBookClubFragment(
                                        workId = workId,
                                        title = d.title.ifBlank { args.title },
                                        author = args.author.ifBlank { "Unknown" },
                                        coverUrl = d.coverUrl ?: args.coverUrl
                                    )
                            findNavController().navigate(action)
                        }

                        // Open book page in browser (OpenLibrary)
                        btnOpenWeb.setOnClickListener {
                            val url = "https://openlibrary.org/works/${args.workId}"
                            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        }
                    }

                    is UiState.Error -> {
                        val msg = state.throwable.message ?: getString(R.string.error_unknown)
                        txtDescription.text = getString(R.string.error_fmt, msg)
                        btnFollow.isEnabled = false
                        btnCreateClub.isVisible = false
                    }
                }
            }
        }

        // Follow / Unfollow observe
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFollowed.collect { followed ->
                btnFollow.text =
                    if (followed) getString(R.string.unfollow) else getString(R.string.follow)
                btnFollow.isEnabled = true
            }
        }

        btnFollow.setOnClickListener {
            if (viewModel.isFollowed.value) {
                viewModel.unfollowBook(currentUserId, workId)
            } else {
                viewModel.followBook(currentUserId, workId)
            }
            btnFollow.isEnabled = false
        }
    }
}
