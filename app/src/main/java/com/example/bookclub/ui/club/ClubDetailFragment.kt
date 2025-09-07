package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R

class ClubDetailFragment : Fragment(R.layout.fragment_club_detail) {

    private val args: ClubDetailFragmentArgs by navArgs()

    private lateinit var commentsAdapter: CommentsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtHeader: TextView = view.findViewById(R.id.txtHeader)
        val etComment: EditText = view.findViewById(R.id.etComment)
        val btnSend: Button     = view.findViewById(R.id.btnSend)
        val recycler: RecyclerView = view.findViewById(R.id.recyclerComments)

        // Header cu ID-ul clubului
        txtHeader.text = getString(R.string.club_detail_header_fmt, args.clubId)

        // Setăm adapterul pentru comentarii
        commentsAdapter = CommentsAdapter()
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = commentsAdapter
        recycler.setHasFixedSize(true)

        // Trimite comentariul
        btnSend.setOnClickListener {
            val content = etComment.text?.toString()?.trim().orEmpty()
            if (content.isNotEmpty()) {
                commentsAdapter.addComment(content) // momentan doar local
                etComment.text?.clear()
            }
        }
    }
}
