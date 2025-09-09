package com.example.bookclub.ui.inbox

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookclub.R
import kotlinx.coroutines.launch

class InboxFragment : Fragment(R.layout.fragment_inbox) {

    private val vm: InboxViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recycler: RecyclerView = view.findViewById(R.id.recyclerInbox)
        val btnMarkAll: Button = view.findViewById(R.id.btnMarkAll)

        val adapter = InboxAdapter { item, parsed ->
            vm.markRead(item.id)
            parsed.clubId?.let { clubId ->
                // navighează la detaliu club (ajustează direcția după graficul tău)
                val action = InboxFragmentDirections
                    .actionInboxFragmentToClubDetailFragment(
                        clubId = clubId,
                        title = parsed.title,
                        coverUrl = "" // dacă vrei, pune cover din payload
                    )
                findNavController().navigate(action)
            }
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnMarkAll.setOnClickListener { vm.markAll() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.items.collect { adapter.submitList(it) }
            }
        }
    }
}
