package uz.mobiler.lesson65.fragment

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.adapters.GroupMessageAdapter
import uz.mobiler.lesson65.databinding.FragmentGroupChatBinding
import uz.mobiler.lesson65.model.*
import uz.mobiler.lesson65.networking.ApiClient
import uz.mobiler.lesson65.networking.ApiService
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_PARAM1 = "group"
private const val ARG_PARAM2 = "account"

class GroupChatFragment : Fragment() {
    private var param1: Group? = null
    private var param2: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as Group?
            param2 = it.getSerializable(ARG_PARAM2) as User?
        }
    }

    private lateinit var binding: FragmentGroupChatBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var reference1: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var reference2: StorageReference
    private lateinit var groupMessageAdapter: GroupMessageAdapter
    private lateinit var messageList: ArrayList<GroupMessage>
    private lateinit var userList: ArrayList<User>
    private lateinit var uid: String
    private lateinit var account: User
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        binding.apply {
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")
            reference1 = firebaseDatabase.getReference("groups")
            storage = FirebaseStorage.getInstance()
            reference2 = storage.getReference("photos")
            if (param2 != null) {
                uid = param2?.uid!!
                account = param2!!
            }
            name.text = param1?.groupName
            info.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("About group")
                builder.setCancelable(false)
                builder.setMessage(param1?.groupAbout)
                builder.setPositiveButton(
                    "Close"
                ) { dialog, id ->
                    dialog.dismiss()
                }
                builder.show()
            }
            close.setOnClickListener {
                Navigation.findNavController(root).popBackStack()
            }
            messageList = ArrayList()
            userList = ArrayList()
            if (messageList.isNotEmpty()) {
                binding.lottie.visibility = View.INVISIBLE
            } else {
                binding.lottie.visibility = View.VISIBLE
            }
            groupMessageAdapter =
                GroupMessageAdapter(requireContext(), messageList, account, param1) { message ->
                    if (message.text.toString().isNotEmpty()) {
                        val clipboard = ContextCompat.getSystemService(
                            requireContext(),
                            ClipboardManager::class.java
                        ) as ClipboardManager
                        val clip = ClipData.newPlainText("label", message.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(requireContext(), "copy text", Toast.LENGTH_SHORT).show()
                    } else if (message.imageUrl.toString().isNotEmpty()) {
                        val bundle = Bundle()
                        bundle.putString("img", message.imageUrl)
                        Navigation.findNavController(root).navigate(R.id.imageFragment, bundle)
                    }
                }
            rv.adapter = groupMessageAdapter

            sendBtn.setOnClickListener {
                if (edtMsg.text.toString().isNotEmpty()) {
                    val text = edtMsg.text.toString()
                    val key = reference.push().key
                    val message = GroupMessage(text, account, getDate(), false, key, imageUrl)
                    if (userList.isNotEmpty()) {
                        userList.forEach { user ->
                            val notificationData = NotificationData(
                                Data(
                                    text,
                                    "key1",
                                    "key2",
                                    param1?.groupName + ": " + account.displayName.toString()
                                ), user.token.toString()
                            )

                            ApiClient.getRetrofit().create(ApiService::class.java)
                                .sendMessage(notificationData)
                                .enqueue(object : Callback<NotificationResponse> {
                                    override fun onResponse(
                                        call: Call<NotificationResponse>,
                                        response: Response<NotificationResponse>
                                    ) {
                                        if (response.isSuccessful) {
                                            reference1.child(param1?.groupKey ?: "")
                                                .child("message")
                                                .child(key ?: "").setValue(message)
                                            edtMsg.setText("")
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<NotificationResponse>,
                                        t: Throwable
                                    ) {

                                    }
                                })
                        }
                    } else {
                        reference1.child(param1?.groupKey ?: "").child("message")
                            .child(key ?: "").setValue(message)
                        edtMsg.setText("")
                    }
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

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    val children = snapshot.children
                    children.forEach {
                        val value = it.getValue(User::class.java)
                        if (value != null && uid != value.uid) {
                            userList.add(value)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            reference1.child(param1?.groupKey ?: "").child("message")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messageList.clear()
                        val children = snapshot.children
                        children.forEach {
                            val value = it.getValue(GroupMessage::class.java)
                            if (value != null) {
                                messageList.add(value)
                            }
                        }
                        groupMessageAdapter.notifyDataSetChanged()
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

    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        Toast.makeText(requireContext(), "Loading...", Toast.LENGTH_SHORT).show()
        if (it == null) return@registerForActivityResult
        reference2
            .child("${System.currentTimeMillis()}.png")
            .putFile(it)
            .addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    imageUrl = uri.toString()
                    val key = reference.push().key
                    val message = GroupMessage("", account, getDate(), false, key, imageUrl)
                    if (userList.isNotEmpty()) {
                        userList.forEach { user ->
                            val notificationData = NotificationData(
                                Data(
                                    "Photo",
                                    "key1",
                                    "key2",
                                    param1?.groupName + ": " + account.displayName.toString()
                                ), user.token.toString()
                            )

                            ApiClient.getRetrofit().create(ApiService::class.java)
                                .sendMessage(notificationData)
                                .enqueue(object : Callback<NotificationResponse> {
                                    override fun onResponse(
                                        call: Call<NotificationResponse>,
                                        response: Response<NotificationResponse>
                                    ) {
                                        if (response.isSuccessful) {
                                            reference1.child(param1?.groupKey ?: "")
                                                .child("message")
                                                .child(key ?: "").setValue(message)
                                            imageUrl = ""
                                            Toast.makeText(
                                                requireContext(),
                                                "Image uploaded",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onFailure(
                                        call: Call<NotificationResponse>,
                                        t: Throwable
                                    ) {

                                    }
                                })
                        }
                    } else {
                        reference1.child(param1?.groupKey ?: "")
                            .child("message")
                            .child(key ?: "").setValue(message)
                        imageUrl = ""
                        Toast.makeText(
                            requireContext(),
                            "Image uploaded",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(), it.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Group, param2: User) =
            GroupChatFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putSerializable(ARG_PARAM2, param2)
                }
            }
    }

    private fun getDate(): String {
        val date = Date()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        return simpleDateFormat.format(date)
    }

    override fun onStart() {
        super.onStart()
        reference.child(param2?.uid.toString()).child("online").setValue(true)
    }
}