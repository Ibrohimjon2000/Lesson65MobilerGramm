package uz.mobiler.lesson65.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.databinding.ItemGroupBinding
import uz.mobiler.lesson65.model.Group
import uz.mobiler.lesson65.model.GroupMessage
import uz.mobiler.lesson65.model.User

class GroupsAdapter(
    val context: Context,
    val list: List<Group>,
    val account: User?,
    val onItemClickListener: (group: Group) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.Vh>() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var messageList: ArrayList<GroupMessage>
    private var count = 0

    inner class Vh(val itemGroupBinding: ItemGroupBinding) :
        RecyclerView.ViewHolder(itemGroupBinding.root) {
        fun onBind(group: Group) {
            itemGroupBinding.apply {
                firebaseDatabase = FirebaseDatabase.getInstance()
                reference = firebaseDatabase.getReference("groups")
                messageList = ArrayList()
                groupName.text = group.groupName

                reference.child(group.groupKey ?: "").child("message")
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
                            if (messageList.isNotEmpty()) {
                                message.visibility = View.VISIBLE
                                date.visibility = View.VISIBLE

                                if (messageList[messageList.size - 1].user?.uid == account?.uid) {
                                    isCheck.visibility = View.VISIBLE
                                    if (messageList[messageList.size - 1].isChecked == true) {
                                        isCheck.setImageResource(R.drawable.ic_check_black2)
                                    } else {
                                        isCheck.setImageResource(R.drawable.ic_check_black1)
                                    }
                                } else {
                                    if (messageList[messageList.size - 1].isChecked == false) {
                                        counter.visibility = View.VISIBLE
                                        count = 0
                                        messageList.forEach {
                                            if (it.isChecked == false) {
                                                count++
                                            }
                                        }
                                        numberCount.text = count.toString()
                                    } else {
                                        isCheck.visibility = View.INVISIBLE
                                    }
                                }

                                if (messageList[messageList.size - 1].user?.uid.toString() == account?.uid) {
                                    if (messageList[messageList.size - 1].text.toString()
                                            .isNotEmpty()
                                    ) {
                                        message.text =
                                            "you: " + messageList[messageList.size - 1].text.toString()
                                    } else if (messageList[messageList.size - 1].imageUrl.toString()
                                            .isNotEmpty()
                                    ) {
                                        message.text =
                                            "you: photo"
                                    }
                                } else {
                                    if (messageList[messageList.size - 1].text.toString()
                                            .isNotEmpty()
                                    ) {
                                        message.text =
                                            messageList[messageList.size - 1].user?.displayName + ": " + messageList[messageList.size - 1].text.toString()
                                    } else if (messageList[messageList.size - 1].imageUrl.toString()
                                            .isNotEmpty()
                                    ) {
                                        message.text =
                                            messageList[messageList.size - 1].user?.displayName + ": photo"
                                    }
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
            }
            itemView.setOnClickListener {
                onItemClickListener.invoke(group)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}