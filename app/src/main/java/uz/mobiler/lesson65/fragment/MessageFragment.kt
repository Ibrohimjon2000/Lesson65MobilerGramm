package uz.mobiler.lesson65.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.adapters.ViewPager2FragmentAdapter
import uz.mobiler.lesson65.databinding.CustomTabBinding
import uz.mobiler.lesson65.databinding.FragmentMessageBinding
import uz.mobiler.lesson65.model.User


private const val ARG_PARAM1 = "user"
private const val ARG_PARAM2 = "id"

class MessageFragment : Fragment() {
    private var param1: User? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as User?
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentMessageBinding
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var viewPager2FragmentAdapter: ViewPager2FragmentAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    val list = arrayOf(
        "Chats",
        "Groups"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        binding.apply {
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")
            reference.child(param1?.uid.toString()).child("online").setValue(true)
            gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            exit.setOnClickListener {
                mGoogleSignInClient.signOut()
                reference.child(param1?.uid.toString()).child("online").setValue(false)
                Navigation.findNavController(root).popBackStack()
                Navigation.findNavController(root).popBackStack()
            }

//            reference.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val children = snapshot.children
//                    children.forEach {
//                        val value = it.getValue(User::class.java)
//                        if (value != null && param1?.uid == value.uid) {
//                            name.text = value.displayName
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//            })

            name.text = param1?.displayName

            Glide.with(requireContext())
                .load(param1?.photoUrl)
                .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                .into(img)

            if (param1 != null) {
                viewPager2FragmentAdapter =
                    ViewPager2FragmentAdapter(this@MessageFragment, list.toList(), param1!!)
            }
            binding.viewPager2.adapter = viewPager2FragmentAdapter
            val tabLayoutMediator =
                TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
                    val customTabBinding =
                        CustomTabBinding.inflate(LayoutInflater.from(requireContext()), null, false)
                    customTabBinding.title.text = list[position]
                    if (position == 0) {
                        customTabBinding.liner.setBackgroundResource(R.drawable.tab_selector)
                        customTabBinding.title.setTextColor(Color.WHITE)
                    } else {
                        customTabBinding.liner.setBackgroundResource(R.drawable.tab_unselector)
                        customTabBinding.title.setTextColor(Color.parseColor("#848484"))
                    }
                    tab.customView = customTabBinding.root
                    tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab) {
                            val binding = tab.customView?.let { CustomTabBinding.bind(it) }
                            binding?.liner?.setBackgroundResource(R.drawable.tab_selector)
                            binding?.title?.setTextColor(Color.WHITE)
                        }

                        override fun onTabUnselected(tab: TabLayout.Tab) {
                            val binding = tab.customView?.let { CustomTabBinding.bind(it) }
                            binding?.liner?.setBackgroundResource(R.drawable.tab_unselector)
                            binding?.title?.setTextColor(Color.parseColor("#848484"))
                        }

                        override fun onTabReselected(tab: TabLayout.Tab?) {

                        }
                    })
                }
            tabLayoutMediator.attach()
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: User, param2: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        reference.child(param1?.uid.toString()).child("online").setValue(false)
    }

    override fun onStop() {
        super.onStop()
        reference.child(param1?.uid.toString()).child("online").setValue(false)
    }

    override fun onStart() {
        super.onStart()
        reference.child(param1?.uid.toString()).child("online").setValue(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    reference.child(param1?.uid.toString()).child("online").setValue(false)
                    activity?.finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }
}