package com.moviedb.view.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.moviedb.activities.DashboardActivity
import com.moviedb.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Register button listener, show Toasts with exceptions
         * */
        binding.registerButton.setOnClickListener{
            when{
                TextUtils.isEmpty(binding.registerEmail.text.toString().trim(){
                    it <= ' '
                }) -> {
                    binding.registerEmail.requestFocus()
                    Toast.makeText(context, "Enter email", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(binding.registerPassword.text.toString().trim(){
                    it <= ' '
                }) -> {
                    binding.registerPassword.requestFocus()
                    Toast.makeText(context, "Enter password", Toast.LENGTH_SHORT).show()
                }

                binding.registerPassword.text.toString().trim(){ it <= ' ' } != binding.registerConfirmPassword.text.toString().trim(){ it <= ' ' }
                -> {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    binding.progressBar.visibility = View.VISIBLE
                    val email = binding.registerEmail.text.toString().trim(){ it <= ' ' }
                    val password = binding.registerPassword.text.toString().trim(){ it <= ' ' }
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener{ task ->
                            if(task.isSuccessful) {
                                binding.progressBar.visibility = View.INVISIBLE

                                Toast.makeText(context, "Registered successfully", Toast.LENGTH_LONG).show()
                                Log.d("lol", FirebaseAuth.getInstance().currentUser!!.toString())

                                val intent = Intent(requireContext(), DashboardActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                requireActivity().finish()

                            }
                            else {
                                binding.progressBar.visibility = View.INVISIBLE
                                Toast.makeText(context, task.exception!!.message.toString().replace(".", ""), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}