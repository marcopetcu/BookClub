package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch

class ClubDetailFragment : Fragment(R.layout.fragment_club_detail) {

    private val args: ClubDetailFragmentArgs by navArgs()
    private val viewModel: ClubDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtHeader: TextView = view.findViewById(R.id.txtHeader)
        val recycler: RecyclerView = view.findViewById(R.id.recyclerComments)
        val etComment: EditText = view.findViewById(R.id.etComment)
        val btnSend: Button = view.findViewById(R.id.btnSend)

        txtHeader.text = getString(R.string.club_detail_header_fmt, args.clubId)

        val adapter = CommentsAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.adapter = adapter

        // observă comentariile
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.comments.collect { list ->
                        adapter.submitList(list)
                        // opțional: scroll la ultimul
                        if (list.isNotEmpty()) recycler.scrollToPosition(list.lastIndex)
                    }
                }
            }
        }

        // inițial, „încarcă” (mock)
        viewModel.loadComments(args.clubId)

        btnSend.setOnClickListener {
            val content = etComment.text?.toString()?.trim().orEmpty()
            if (content.isEmpty()) return@setOnClickListener
            viewModel.postComment(args.clubId, content)
            etComment.text?.clear()
        }
    }
}
