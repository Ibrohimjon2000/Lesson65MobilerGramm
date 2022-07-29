package uz.mobiler.lesson65.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.databinding.FragmentEditProfileBinding
import uz.mobiler.lesson65.model.User

private const val ARG_PARAM1 = "user"
private const val ARG_PARAM2 = "id"

class EditProfileFragment : Fragment() {
    private var param1: User? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as User?
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var reference1: StorageReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        binding.apply {
            firebaseDatabase = FirebaseDatabase.getInstance()
            reference = firebaseDatabase.getReference("users")
            name.setText(param1?.displayName.toString())
            email.setText(param1?.email.toString())
            imageUrl = param1?.photoUrl.toString()
            Glide.with(requireContext())
                .load(param1?.photoUrl)
                .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                .into(img)

            storage = FirebaseStorage.getInstance()
            reference1 = storage.getReference("photos")

            selectImg.setOnClickListener {
                launcher.launch("image/*")
            }
            edit.setOnClickListener {
                if (name.text.toString().isNotEmpty() && email.text.toString()
                        .isNotEmpty() && imageUrl.isNotEmpty()
                ) {
                    reference.child(param2.toString()).child("displayName")
                        .setValue(name.text.toString())
                    reference.child(param2.toString()).child("email")
                        .setValue(email.text.toString())
                    reference.child(param2.toString()).child("photoUrl").setValue(imageUrl)
                    val user = User(
                        name.text.toString(),
                        param1?.uid,
                        email.text.toString(),
                        imageUrl,
                        param1?.isOnline
                    )
                    val bundle = Bundle()
                    bundle.putSerializable("user", user)
                    bundle.putString("id", user.uid)
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.messageFragment, bundle)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ma'lumotlar to'liq kiritilmagan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                    Glide.with(requireContext())
                        .load(uri)
                        .apply(
                            RequestOptions().placeholder(
                                R.drawable.profile
                            ).centerCrop()
                        )
                        .into(binding.img)
                }
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(), it.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: User, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}