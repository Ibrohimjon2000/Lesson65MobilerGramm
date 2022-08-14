package uz.mobiler.lesson65.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.databinding.FragmentSignInBinding
import uz.mobiler.lesson65.model.User


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SignInFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private val TAG = "MainActivity"
    private lateinit var binding: FragmentSignInBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.apply {
            auth = Firebase.auth
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")

            gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            signIn.setOnClickListener {
                val intent = mGoogleSignInClient.signInIntent
                startActivityForResult(intent, 1)
            }

            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(
                            TAG,
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                        return@OnCompleteListener
                    }

                    val token = task.result
                    this@SignInFragment.token = token
                    Log.d(TAG, "onDataChange: $token")
                })
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            val account: GoogleSignInAccount? = task?.getResult(ApiException::class.java)

            val user = User(
                account?.displayName,
                account?.id,
                account?.email,
                account?.photoUrl.toString(),
                true,
                token
            )
            reference
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var isHase = false
                        val children = snapshot.children
                        children.forEach {
                            val value = it.getValue(User::class.java)
                            if (value != null && user.uid == value.uid) {
                                isHase = true
                            }
                        }
                        if (isHase) {
                            val bundle = Bundle()
                            bundle.putSerializable("user", user)
                            bundle.putString("id", user.uid)
                            Navigation.findNavController(binding.root)
                                .navigate(R.id.editProfileFragment, bundle)
                        } else {
                            setNewUser(user)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setNewUser(user: User) {
        reference.child(user.uid ?: "").setValue(user)
            .addOnSuccessListener {
                val bundle = Bundle()
                bundle.putSerializable("user", user)
                bundle.putString("id", user.uid)
                Navigation.findNavController(binding.root)
                    .navigate(R.id.editProfileFragment, bundle)
            }
    }

    override fun onStart() {
        super.onStart()
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (lastSignedInAccount != null) {
            val user = User(
                lastSignedInAccount.displayName,
                lastSignedInAccount.id,
                lastSignedInAccount.email,
                lastSignedInAccount.photoUrl.toString(),
                true,
                lastSignedInAccount.idToken.toString()
            )
            val bundle = Bundle()
            bundle.putSerializable("user", user)
            bundle.putString("id", user.uid)
            Navigation.findNavController(binding.root)
                .navigate(R.id.messageFragment, bundle)
        }
    }
}