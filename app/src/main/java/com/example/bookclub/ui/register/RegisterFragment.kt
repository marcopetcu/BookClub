package com.example.bookclub.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bookclub.R
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.session.Session
import kotlinx.coroutines.flow.collectLatest

class RegisterFragment: Fragment() {

    private val args: RegisterFragmentArgs by navArgs()
    private val viewModel: RegisterViewModel by viewModels()
    private val session by lazy { ServiceLocator.sessionManager(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (session.isLoggedIn()) {
            goToHome()
            return
        }

        val email = view.findViewById<EditText>(R.id.edt_email)
        val nickname = view.findViewById<EditText>(R.id.edt_nickname)
        val password = view.findViewById<EditText>(R.id.edt_password)
        val confirm = view.findViewById<EditText>(R.id.edt_confirm)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)
        val tvError = view.findViewById<TextView>(R.id.tv_error)
        val progress = view.findViewById<ProgressBar>(R.id.progress)

        email.setText(args.email)

        btnRegister.setOnClickListener {
            tvError.text = ""
            viewModel.register(
                email.text?.toString().orEmpty(),
                nickname.text?.toString().orEmpty(),
                password.text?.toString().orEmpty(),
                confirm.text?.toString().orEmpty()
            )
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is com.example.bookclub.ui.common.UiState.Idle -> {
                        progress.visibility = View.GONE
                    }
                    is com.example.bookclub.ui.common.UiState.Loading -> {
                        progress.visibility = View.VISIBLE
                        tvError.text = ""
                    }
                    is com.example.bookclub.ui.common.UiState.Success -> {
                        progress.visibility = View.GONE
                        val u = state.data
                        session.save(
                            Session(
                                userId = u.id,
                                email = u.email,
                                nickname = u.nickname,
                                role = u.role,
                                createdAtEpochMs = u.createdAt.toEpochMilli()
                            )
                        )
                        goToHome()
                    }
                    is com.example.bookclub.ui.common.UiState.Error -> {
                        progress.visibility = View.GONE
                        tvError.text = state.throwable.message ?: "Registration error"
                    }
                }
            }
        }
    }

    private fun goToHome() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToNavigationHome()
        findNavController().navigate(action)
    }
}
