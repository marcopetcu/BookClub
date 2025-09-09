package com.example.bookclub.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookclub.R
import com.example.bookclub.data.ServiceLocator

class ProfileFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = ServiceLocator.sessionManager(requireContext())
        val session = sessionManager.get()

        // 🔹 Completează nume și email
        val txtName: TextView = view.findViewById(R.id.txt_name)
        val txtEmail: TextView = view.findViewById(R.id.txt_email)

        txtName.text = session?.nickname ?: getString(R.string.default_nickname)
        txtEmail.text = session?.email ?: ""

        // 🔹 Buton logout
        view.findViewById<Button>(R.id.btn_logout)?.setOnClickListener {
            sessionManager.clear()
            findNavController().navigate(R.id.loginFragment)
        }
    }
}
