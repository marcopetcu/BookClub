package com.example.bookclub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookclub.R
import com.example.bookclub.data.ServiceLocator

class ProfileFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?  =
        inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val session = ServiceLocator.sessionManager(requireContext())
        view.findViewById<Button>(R.id.btn_logout)?.setOnClickListener {
            session.clear()
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
