package uz.mobiler.lesson65.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.adapters.MessageAdapter
import uz.mobiler.lesson65.databinding.FragmentChatBinding
import uz.mobiler.lesson65.model.Message
import uz.mobiler.lesson65.model.User
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var storage: FirebaseStorage
    private lateinit var reference1: StorageReference
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var uid: String
    private lateinit var user: User
    private lateinit var account: User
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding.apply {
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")
            storage = FirebaseStorage.getInstance()
            reference1 = storage.getReference("photos")
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

            sendBtn.setOnClickListener {
                if (edtMsg.text.toString().isNotEmpty()) {
                    val text = edtMsg.text.toString()
                    val key = reference.push().key
                    val message = Message(text, param1?.uid, uid, getDate(), false, key, imageUrl)
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

            sendProfile.setOnClickListener {
                launcher.launch("image/*")
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

            messageAdapter =
                MessageAdapter(
                    requireContext(),
                    messageList,
                    account,
                    user,
                    object : MessageAdapter.OnItemClickListener {
                        override fun onItemTextClickListener(message: Message, position: Int) {
                            val clipboard = getSystemService(
                                requireContext(),
                                ClipboardManager::class.java
                            ) as ClipboardManager
                            val clip = ClipData.newPlainText("label", message.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(requireContext(), "copy text", Toast.LENGTH_SHORT).show()
                        }

                        override fun onItemImageClickListener(message: Message, position: Int) {
                            val bundle = Bundle()
                            bundle.putString("img", message.imageUrl)
                            Navigation.findNavController(root).navigate(R.id.imageFragment, bundle)
                        }
                    })
            rv.adapter = messageAdapter
        }
        return binding.root
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it == null) return@registerForActivityResult
        reference1
            .child("${System.currentTimeMillis()}.png")
            .putFile(it)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    imageUrl = uri.toString()
                    val key = reference.push().key
                    val message = Message("", param1?.uid, uid, getDate(), false, key, imageUrl)
                    reference.child(param1?.uid ?: "").child("message").child(uid)
                        .child(key ?: "").setValue(message)
                    reference.child(uid).child("message").child(param1?.uid ?: "")
                        .child(key ?: "").setValue(message)
                    imageUrl = ""
                    Toast.makeText(requireContext(), "Image uploaded", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(), it.message, Toast.LENGTH_SHORT
                ).show()
            }
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

    override fun onStart() {
        super.onStart()
        reference.child(param2?.uid.toString()).child("online").setValue(true)
    }
}