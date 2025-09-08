package com.example.bookclub.ui.books

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
    private lateinit var btnCreateClub: Button
    private lateinit var btnOpenWeb: Button   // ✅ nou

    // TODO: ia userId/role reale din SessionManager
    private val userId = 1L
    private val isAdmin = true // TEMP

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgCover = view.findViewById(R.id.imgCover)
        txtTitle = view.findViewById(R.id.txtTitle)
        txtSubjects = view.findViewById(R.id.txtSubjects)
        txtDescription = view.findViewById(R.id.txtDescription)
        btnFollow = view.findViewById(R.id.btnFollow)
        btnCreateClub = view.findViewById(R.id.btnCreateClub)
        btnOpenWeb = view.findViewById(R.id.btnOpenWeb) // ✅ init

        val workId = args.workId

        // butonul Create Club vizibil doar pt ADMIN
        btnCreateClub.visibility = if (isAdmin) View.VISIBLE else View.GONE

        // încărcăm detalii + follow state
        viewModel.loadWorkDetails(workId)
        viewModel.loadFollowState(userId, workId)

        // observăm detaliile
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

                        // titlu / subjects / descriere / copertă
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

                        // Create Club → folosim Safe Args
                        btnCreateClub.setOnClickListener {
                            val action =
                                BookDetailFragmentDirections
                                    .actionBookDetailFragmentToCreateBookClubFragment(
                                        workId  = workId,
                                        title   = d.title.ifBlank { args.title },
                                        author  = args.author.ifBlank { "Unknown" },
                                        coverUrl = d.coverUrl ?: args.coverUrl
                                    )
                            findNavController().navigate(action)
                        }

                        // ✅ Open Book Page → browser
                        btnOpenWeb.setOnClickListener {
                            val url = "https://openlibrary.org/works/${args.workId}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }
                    }

                    is UiState.Error -> {
                        val msg = state.throwable.message ?: getString(R.string.error_unknown)
                        txtDescription.text = getString(R.string.error_fmt, msg)
                        btnFollow.isEnabled = false
                        btnCreateClub.visibility = View.GONE
                    }
                }
            }
        }

        // follow/unfollow – observăm starea
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isFollowed.collect { followed ->
                btnFollow.text =
                    if (followed) getString(R.string.unfollow) else getString(R.string.follow)
                btnFollow.isEnabled = true
            }
        }

        btnFollow.setOnClickListener {
            if (viewModel.isFollowed.value) viewModel.unfollowBook(userId, workId)
            else viewModel.followBook(userId, workId)
            btnFollow.isEnabled = false
        }
    }
}