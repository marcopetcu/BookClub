// file: app/src/main/java/com/example/bookclub/ui/club/ClubDetailFragment.kt
package com.example.bookclub.ui.club

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
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
import com.example.bookclub.data.model.ClubComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClubDetailFragment : Fragment(R.layout.fragment_club_detail) {

    private val args: ClubDetailFragmentArgs by navArgs()
    private val vm: ClubDetailViewModel by viewModels()

    private var replyTo: ClubComment? = null

    private val expanded = mutableSetOf<Long>()
    private val repliesCache = mutableMapOf<Long, List<ClubComment>>()
    private val repliesCount = mutableMapOf<Long, Int>()

    private lateinit var adapter: ThreadedCommentsAdapter
    private var latestParents: List<ClubComment> = emptyList()

    private var canComment: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Header
        val imgCover: ImageView = view.findViewById(R.id.imgCover)
        val tvTitle: TextView   = view.findViewById(R.id.tvTitle)
        tvTitle.text = args.title
        imgCover.load(args.coverUrl) {
            placeholder(R.drawable.ic_book_placeholder)
            error(R.drawable.ic_book_placeholder)
            crossfade(true)
        }

        // Reply bar
        val replyBar: View         = view.findViewById(R.id.replyBar)
        val tvReplyingTo: TextView = view.findViewById(R.id.tvReplyingTo)
        val btnCancelReply: Button = view.findViewById(R.id.btnCancelReply)
        fun clearReply() { replyTo = null; replyBar.isVisible = false; tvReplyingTo.text = "" }
        btnCancelReply.setOnClickListener { clearReply() }

        // Recycler + adapter
        val recycler: RecyclerView = view.findViewById(R.id.recyclerComments)
        adapter = ThreadedCommentsAdapter(
            onReplyClick = { comment ->
                if (!canComment) return@ThreadedCommentsAdapter
                replyTo = comment
                tvReplyingTo.text = getString(
                    R.string.replying_to_fmt,
                    comment.authorName ?: getString(R.string.anonymous)
                )
                replyBar.isVisible = true
            },
            onToggleParent = { parentId, currentlyExpanded -> toggleParent(parentId, currentlyExpanded) },
            canReply = true // va fi actualizat din live-state mai jos
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Input & send
        val etComment: EditText = view.findViewById(R.id.etComment)
        val btnSend: Button     = view.findViewById(R.id.btnSend)
        val session = ServiceLocator.sessionManager(requireContext()).get()
        val currentUserId = session?.userId ?: 1L

        btnSend.setOnClickListener {
            if (!canComment) return@setOnClickListener
            val content = etComment.text?.toString()?.trim().orEmpty()
            if (content.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val parentId = replyTo?.id
                    vm.postComment(args.clubId, currentUserId, content, parentId)
                    if (parentId != null) refreshRepliesForParent(parentId)
                    etComment.text?.clear()
                    clearReply()
                }
            }
        }

        // ✅ LIVE / non-LIVE → controlează UI + Reply din adapter
        vm.observeLiveState(args.clubId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.isCommentingAllowed.collect { allowed ->
                    canComment = allowed
                    etComment.isEnabled = allowed
                    btnSend.isEnabled = allowed
                    if (!allowed) clearReply()
                    adapter.setCanReply(allowed)   // <<—— ascunde/arata „Reply” în listă
                }
            }
        }

        // Doar PĂRINȚII
        vm.observeComments(args.clubId)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.comments.collect { parents ->
                    latestParents = parents
                    buildAndShowThread()
                }
            }
        }
    }

    private fun toggleParent(parentId: Long, currentlyExpanded: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (currentlyExpanded) {
                expanded.remove(parentId)
                buildAndShowThread()
            } else {
                expanded.add(parentId)
                if (!repliesCache.containsKey(parentId)) {
                    val replies = withContext(Dispatchers.IO) { vm.fetchReplies(parentId) }
                    repliesCache[parentId] = replies
                    repliesCount[parentId] = replies.size
                }
                buildAndShowThread()
            }
        }
    }

    private suspend fun refreshRepliesForParent(parentId: Long) {
        val replies = withContext(Dispatchers.IO) { vm.fetchReplies(parentId) }
        repliesCache[parentId] = replies
        repliesCount[parentId] = replies.size
        buildAndShowThread()
    }

    private fun buildAndShowThread() {
        val items = mutableListOf<ThreadItem>()
        for (parent in latestParents) {
            val count = repliesCount[parent.id]
            items += ThreadItem.Parent(
                comment = parent,
                isExpanded = parent.id in expanded,
                repliesCount = count
            )
            if (parent.id in expanded) {
                repliesCache[parent.id].orEmpty().forEach { child ->
                    items += ThreadItem.Reply(child)
                }
            } else if (count == null) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val c = vm.countReplies(parent.id)
                    repliesCount[parent.id] = c
                    withContext(Dispatchers.Main) { buildAndShowThread() }
                }
            }
        }
        adapter.submitList(items)
    }
}
