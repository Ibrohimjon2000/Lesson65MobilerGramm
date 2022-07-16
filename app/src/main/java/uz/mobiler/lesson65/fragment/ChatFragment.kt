package uz.mobiler.lesson65.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.adapters.MessageAdapter
import uz.mobiler.lesson65.databinding.FragmentChatBinding
import uz.mobiler.lesson65.model.Message
import uz.mobiler.lesson65.model.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "user"
private const val ARG_PARAM2 = "account"

class ChatFragment : Fragment() {
    private var param1: User? = null
    private var param2: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as User?
            param2 = it.getSerializable(ARG_PARAM2) as User?
        }
    }

    private lateinit var binding: FragmentChatBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var uid: String
    private lateinit var user: User
    private lateinit var account: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.apply {
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")
            name.text = param1?.displayName
            Glide.with(requireContext())
                .load(param1?.photoUrl)
                .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                .into(img)
            close.setOnClickListener {
                Navigation.findNavController(root).popBackStack()
            }
            if (param2 != null && param1 != null) {
                uid = param2?.uid!!
                user = param1!!
                account = param2!!
            }
            if (user.isOnline == true) {
                isOnline.setImageResource(R.drawable.online)
                textOnline.text = "online"
            } else {
                isOnline.setImageResource(R.drawable.oflinee)
                textOnline.text = "last seen recently"
            }
            messageList = ArrayList()
            if (messageList.isNotEmpty()) {
                binding.lottie.visibility = View.INVISIBLE
            } else {
                binding.lottie.visibility = View.VISIBLE
            }
            messageAdapter = MessageAdapter(requireContext(), messageList, account, user)
            rv.adapter = messageAdapter

            sendBtn.setOnClickListener {
                if (edtMsg.text.toString().isNotEmpty()) {
                    val text = edtMsg.text.toString()
                    val key = reference.push().key
                    val message = Message(text, param1?.uid, uid, getDate(), false, key)
                    reference.child(param1?.uid ?: "").child("message").child(uid)
                        .child(key ?: "").setValue(message)
                    reference.child(uid).child("message").child(param1?.uid ?: "")
                        .child(key ?: "").setValue(message)
                    edtMsg.setText("")
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please enter your message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            reference.child(uid).child("message").child(param1?.uid ?: "")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messageList.clear()
                        val children = snapshot.children
                        children.forEach {
                            val value = it.getValue(Message::class.java)
                            if (value != null) {
                                messageList.add(value)
                            }
                        }
                        messageAdapter.notifyDataSetChanged()
                        if (messageList.isNotEmpty()) {
                            binding.lottie.visibility = View.INVISIBLE
                        } else {
                            binding.lottie.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
        return binding.root
    }

    private fun getDate(): String {
        val date = Date()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        return simpleDateFormat.format(date)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: User, param2: User) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putSerializable(ARG_PARAM2, param2)
                }
            }
    }
}