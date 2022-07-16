package uz.mobiler.lesson65.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.adapters.GroupsAdapter
import uz.mobiler.lesson65.adapters.UsersAdapter
import uz.mobiler.lesson65.databinding.FragmentMessageViewPagerBinding
import uz.mobiler.lesson65.model.Group
import uz.mobiler.lesson65.model.User

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MessageViewPagerFragment : Fragment() {
    private var param1: String? = null
    private var param2: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getSerializable(ARG_PARAM2) as User?
        }
    }

    private lateinit var binding: FragmentMessageViewPagerBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var reference1: DatabaseReference
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var list: ArrayList<User>
    private lateinit var groupList: ArrayList<Group>
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageViewPagerBinding.inflate(inflater, container, false)
        binding.apply {
            auth = FirebaseAuth.getInstance()
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")
            reference1 = firebaseDatabase.getReference("groups")
            when (param1) {
                "Chats" -> {
                    add.visibility = View.INVISIBLE
                    list = ArrayList()
                    if (list.isNotEmpty()) {
                        binding.lottie.visibility = View.INVISIBLE
                    } else {
                        binding.lottie.visibility = View.VISIBLE
                    }
                    usersAdapter = UsersAdapter(requireContext(), list, param2) {
                        val bundle = Bundle()
                        bundle.putSerializable("user", it)
                        bundle.putSerializable("account", param2)
                        Navigation.findNavController(root).navigate(R.id.chatFragment, bundle)
                    }
                    rv.addItemDecoration(
                        DividerItemDecoration(
                            requireContext(),
                            LinearLayoutManager.VERTICAL
                        )
                    )
                    rv.adapter = usersAdapter
                    reference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            list.clear()
                            val children = snapshot.children
                            children.forEach {
                                val value = it.getValue(User::class.java)
                                if (value != null && param2?.uid != value.uid) {
                                    list.add(value)
                                }
                            }
                            usersAdapter.notifyDataSetChanged()
                            if (list.isNotEmpty()) {
                                binding.lottie.visibility = View.INVISIBLE
                            } else {
                                binding.lottie.visibility = View.VISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
                "Groups" -> {
                    groupList = ArrayList()
                    if (groupList.isNotEmpty()) {
                        binding.lottie.visibility = View.INVISIBLE
                    } else {
                        binding.lottie.visibility = View.VISIBLE
                    }
                    add.visibility = View.VISIBLE
                    groupsAdapter = GroupsAdapter(requireContext(), groupList, param2) {
                        val bundle = Bundle()
                        bundle.putSerializable("group", it)
                        bundle.putSerializable("account", param2)
                        Navigation.findNavController(root).navigate(R.id.groupChatFragment, bundle)
                    }
                    rv.addItemDecoration(
                        DividerItemDecoration(
                            requireContext(),
                            LinearLayoutManager.VERTICAL
                        )
                    )
                    rv.adapter = groupsAdapter
                    add.setOnClickListener {
                        val mDialogView =
                            LayoutInflater.from(requireContext())
                                .inflate(R.layout.add_group_dialog, null)
                        val mBuilder =
                            AlertDialog.Builder(requireContext()).setView(mDialogView)
                        mBuilder.setCancelable(false)
                        val mAlterDialog = mBuilder.show()
                        mAlterDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        mDialogView.findViewById<TextView>(R.id.create).setOnClickListener {
                            val name =
                                mDialogView.findViewById<TextView>(R.id.groupName).text.toString()
                            val about =
                                mDialogView.findViewById<TextView>(R.id.groupAbout).text.toString()
                            if (name.isNotEmpty() && about.isNotEmpty()) {
                                val key = reference.push().key
                                val group = Group(name, about, key)
                                reference1.child(key ?: "").setValue(group)
                                groupList.add(group)
                                if (groupList.isNotEmpty()) {
                                    binding.lottie.visibility = View.INVISIBLE
                                } else {
                                    binding.lottie.visibility = View.VISIBLE
                                }
                                groupsAdapter.notifyDataSetChanged()
                                mAlterDialog.dismiss()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Ma'lumotlar to'liq kiritilmagan",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        mDialogView.findViewById<TextView>(R.id.cancel).setOnClickListener {
                            mAlterDialog.dismiss()
                        }
                    }

                    reference1.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            groupList.clear()
                            val children = snapshot.children
                            children.forEach {
                                val group = it.getValue(Group::class.java)
                                if (group != null) {
                                    groupList.add(group)
                                }
                            }
                            groupsAdapter.notifyDataSetChanged()
                            if (groupList.isNotEmpty()) {
                                binding.lottie.visibility = View.INVISIBLE
                            } else {
                                binding.lottie.visibility = View.VISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                }
            }
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: User) =
            MessageViewPagerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putSerializable(ARG_PARAM2, param2)
                }
            }
    }
}