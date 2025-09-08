package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bookclub.R
import com.example.bookclub.data.ServiceLocator
import kotlinx.coroutines.launch

class ClubDetailFragment : Fragment(R.layout.fragment_club_detail) {

    private val args: ClubDetailFragmentArgs by navArgs()
    private val vm: ClubDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Header: titlu + copertă
        val imgCover: ImageView = view.findViewById(R.id.imgCover)
        val tvTitle: TextView   = view.findViewById(R.id.tvTitle)

        tvTitle.text = args.title
        imgCover.load(args.coverUrl) {
            placeholder(R.drawable.ic_book_placeholder)
            error(R.drawable.ic_book_placeholder)
            crossfade(true)
        }

        // RecyclerView pentru comentarii
        val recycler: RecyclerView = view.findViewById(R.id.recyclerComments)
        val adapter = CommentsAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Input + send
        val etComment: EditText = view.findViewById(R.id.etComment)
        val btnSend: Button     = view.findViewById(R.id.btnSend)

        val session = ServiceLocator.sessionManager(requireContext()).get()
        val currentUserId = session?.userId ?: 0L

        btnSend.setOnClickListener {
            val content = etComment.text?.toString()?.trim().orEmpty()
            if (content.isNotEmpty()) {
                vm.postComment(args.clubId, currentUserId, content)
                etComment.text?.clear()
            }
        }

        // pornește observarea comentariilor pentru acest club
        vm.observeComments(args.clubId)

        // colectează lista din StateFlow și o trimite în adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.comments.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
    }
}
