package uz.mobiler.lesson65.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.databinding.ItemUserBinding
import uz.mobiler.lesson65.model.Message
import uz.mobiler.lesson65.model.User

class UsersAdapter(
    val context: Context,
    val list: List<User>,
    val account: User?,
    val onItemClickListener: (user: User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.Vh>() {
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var messageList: ArrayList<Message>

    inner class Vh(val itemUserBinding: ItemUserBinding) :
        RecyclerView.ViewHolder(itemUserBinding.root) {
        fun onBind(user: User) {
            itemUserBinding.apply {
                firebaseDatabase = FirebaseDatabase.getInstance()
                reference = firebaseDatabase.getReference("users")
                messageList = ArrayList()
                reference.child(account?.uid ?: "").child("message").child(user.uid ?: "")
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
                            if (messageList.isNotEmpty()) {
                                message.visibility = View.VISIBLE
                                date.visibility = View.VISIBLE

                                if (messageList[messageList.size - 1].fromUserUid == account?.uid) {
                                    isCheck.visibility = View.VISIBLE
                                    if (messageList[messageList.size - 1].isChecked == true) {
                                        isCheck.setImageResource(R.drawable.ic_check_black2)
                                    } else {
                                        isCheck.setImageResource(R.drawable.ic_check_black1)
                                    }
                                } else {
                                    if (messageList[messageList.size - 1].isChecked == false) {
                                        isCheck.visibility = View.VISIBLE
                                        isCheck.setImageResource(R.drawable.online)
                                    } else {
                                        isCheck.visibility = View.INVISIBLE
                                    }
                                }

                                if (messageList[messageList.size - 1].fromUserUid.toString() == account?.uid) {
                                    message.text =
                                        account.displayName + " " + messageList[messageList.size - 1].text.toString()
                                } else if (messageList[messageList.size - 1].fromUserUid.toString() == user.uid) {
                                    message.text =
                                        user.displayName + " " + messageList[messageList.size - 1].text.toString()
                                }
                                date.text = messageList[messageList.size - 1].date.toString()
                            } else {
                                message.visibility = View.INVISIBLE
                                date.visibility = View.INVISIBLE
                                isCheck.visibility = View.INVISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                Glide.with(context)
                    .load(user.photoUrl)
                    .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                    .into(img)
                name.text = user.displayName
                if (user.isOnline == true) {
                    isOnline.setImageResource(R.drawable.online)
                } else {
                    isOnline.setImageResource(R.drawable.oflinee)
                }
            }
            itemView.setOnClickListener {
                onItemClickListener.invoke(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}