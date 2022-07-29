package uz.mobiler.lesson65.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.databinding.ItemFromBinding
import uz.mobiler.lesson65.databinding.ItemToChatBinding
import uz.mobiler.lesson65.model.Message
import uz.mobiler.lesson65.model.User

class MessageAdapter(
    val context: Context,
    val list: List<Message>,
    val account: User,
    val user: User,
    val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private val TO = 0
    private val FROM = 1

    inner class ToVh(val itemToChatBinding: ItemToChatBinding) :
        RecyclerView.ViewHolder(itemToChatBinding.root) {
        fun onBind(message: Message, position: Int) {
            itemToChatBinding.apply {
                firebaseDatabase = FirebaseDatabase.getInstance()
                reference = firebaseDatabase.getReference("users")
                val m = Message(
                    message.text,
                    message.toUserUid,
                    message.fromUserUid,
                    message.date,
                    true,
                    message.key,
                    message.imageUrl
                )
                reference.child(account.uid ?: "").child("message").child(user.uid.toString())
                    .child(message.key ?: "").setValue(m)
                reference.child(user.uid ?: "").child("message").child(account.uid.toString())
                    .child(message.key ?: "").setValue(m)
                if (message.text.toString().isNotEmpty()) {
                    imageDate.visibility = View.GONE
                    sendImg.visibility = View.GONE
                    liner.visibility = View.VISIBLE
                    date.visibility = View.VISIBLE
                    userMsg.text = message.text
                    date.text = message.date
                } else if (message.imageUrl.toString().isNotEmpty()) {
                    imageDate.visibility = View.VISIBLE
                    sendImg.visibility = View.VISIBLE
                    liner.visibility = View.GONE
                    date.visibility = View.GONE
                    Glide.with(context)
                        .load(message.imageUrl)
                        .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                        .into(sendImg)
                    imageDate.text = message.date
                }
                liner.setOnClickListener {
                    listener.onItemTextClickListener(message, position)
                }
                sendImg.setOnClickListener {
                    listener.onItemImageClickListener(message, position)
                }
            }
        }
    }

    inner class FromVh(val itemFromBinding: ItemFromBinding) :
        RecyclerView.ViewHolder(itemFromBinding.root) {
        fun onBind(message: Message, position: Int) {
            itemFromBinding.apply {
                if (message.text.toString().isNotEmpty()) {
                    if (message.isChecked == true) {
                        isCheck.setImageResource(R.drawable.ic_check2)
                    } else {
                        isCheck.setImageResource(R.drawable.ic_check1)
                    }
                    imageDate.visibility = View.GONE
                    sendImg.visibility = View.GONE
                    liner.visibility = View.VISIBLE
                    date.visibility = View.VISIBLE
                    isCheck.visibility = View.VISIBLE
                    isCheckImage.visibility = View.GONE
                    userMsg.text = message.text
                    date.text = message.date
                } else if (message.imageUrl.toString().isNotEmpty()) {
                    if (message.isChecked == true) {
                        isCheckImage.setImageResource(R.drawable.ic_check2)
                    } else {
                        isCheckImage.setImageResource(R.drawable.ic_check1)
                    }
                    imageDate.visibility = View.VISIBLE
                    sendImg.visibility = View.VISIBLE
                    isCheck.visibility = View.GONE
                    isCheckImage.visibility = View.VISIBLE
                    liner.visibility = View.GONE
                    date.visibility = View.GONE
                    Glide.with(context)
                        .load(message.imageUrl)
                        .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                        .into(sendImg)
                    imageDate.text = message.date
                }
                liner.setOnClickListener {
                    listener.onItemTextClickListener(message, position)
                }
                sendImg.setOnClickListener {
                    listener.onItemImageClickListener(message, position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return ToVh(
                ItemToChatBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return FromVh(
                ItemFromBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ToVh) {
            holder.onBind(list[position], position)
        } else if (holder is FromVh) {
            holder.onBind(list[position], position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position].fromUserUid == account.uid) {
            return FROM
        }
        return TO
    }

    interface OnItemClickListener {
        fun onItemTextClickListener(message: Message, position: Int)
        fun onItemImageClickListener(message: Message, position: Int)
    }
}