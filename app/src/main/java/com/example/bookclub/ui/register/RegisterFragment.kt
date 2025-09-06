package com.example.bookclub.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bookclub.R

class RegisterFragment: Fragment() {

    private val args: RegisterFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  = inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_register).setOnClickListener {
            goToHome()
        }

        view.findViewById<EditText>(R.id.edt_email).setText(args.email)
    }

    private fun goToHome() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToNavigationHome()
        findNavController().navigate(action)
    }

}