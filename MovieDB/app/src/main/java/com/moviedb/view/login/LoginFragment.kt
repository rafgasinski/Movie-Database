package com.moviedb.view.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.moviedb.R
import com.moviedb.activities.DashboardActivity
import com.moviedb.databinding.DialogForgotPasswordBinding
import com.moviedb.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.forgotPassword.setOnClickListener {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.background.level = 4000

        /**
         * Login button listener, show Toasts with exceptions
         * */
        binding.loginButton.setOnClickListener {
            when {
                TextUtils.isEmpty(binding.loginEmail.text.toString().trim() {
                    it <= ' '
                }) -> {
                    binding.loginEmail.requestFocus()
                    Toast.makeText(context, "Enter email", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(binding.loginPassword.text.toString().trim() {
                    it <= ' '
                }) -> {
                    binding.loginPassword.requestFocus()
                    Toast.makeText(context, "Enter password", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                    val email = binding.loginEmail.text.toString().trim() { it <= ' ' }
                    val password = binding.loginPassword.text.toString().trim() { it <= ' ' }
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            binding.progressBar.visibility = View.INVISIBLE

                            val intent = Intent(requireContext(), DashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            requireActivity().finish()
                        } else {
                            binding.progressBar.visibility = View.INVISIBLE
                            Toast.makeText(context, task.exception!!.message.toString().replace(".", ""), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        /**
         * Build forgotPassword dialog
         * */
        binding.forgotPassword.setOnClickListener {

            val bindingDialog = DialogForgotPasswordBinding.inflate(LayoutInflater.from(context))
            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            builder.setView(bindingDialog.root)

            val mAlertDialog = builder.show()

            bindingDialog.resetPassword.setOnClickListener {
                when {
                    TextUtils.isEmpty(bindingDialog.forgotPasswordEmail.text.toString().trim() {
                        it <= ' '
                    }) -> {
                        bindingDialog.forgotPasswordEmail.requestFocus()
                        Toast.makeText(context, "No email provided", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        bindingDialog.progressBar.visibility = View.VISIBLE
                        val email = bindingDialog.forgotPasswordEmail.text.toString().trim() { it <= ' ' }
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Check your email to reset password", Toast.LENGTH_LONG).show()
                                        bindingDialog.progressBar.visibility = View.INVISIBLE
                                        mAlertDialog.dismiss()
                                    } else {
                                        Toast.makeText(context, task.exception!!.message.toString().replace(".", ""), Toast.LENGTH_SHORT).show()
                                        bindingDialog.progressBar.visibility = View.INVISIBLE
                                    }
                                }
                    }
                }
            }

            bindingDialog.cancel.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
    }
}