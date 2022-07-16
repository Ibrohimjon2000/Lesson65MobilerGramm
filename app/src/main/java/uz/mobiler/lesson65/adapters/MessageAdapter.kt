package uz.mobiler.lesson65.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uz.mobiler.lesson65.R
import uz.mobiler.lesson65.databinding.ItemFromBinding
import uz.mobiler.lesson65.databinding.ItemToBinding
import uz.mobiler.lesson65.model.Message
import uz.mobiler.lesson65.model.User

class MessageAdapter(
    val context: Context,
    val list: List<Message>,
    val account: User,
    val user: User
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private val TO = 0
    private val FROM = 1

    inner class ToVh(val itemToBinding: ItemToBinding) :
        RecyclerView.ViewHolder(itemToBinding.root) {
        fun onBind(message: Message) {
            itemToBinding.apply {
                firebaseDatabase = FirebaseDatabase.getInstance()
                reference = firebaseDatabase.getReference("users")
                val m = Message(
                    message.text,
                    message.toUserUid,
                    message.fromUserUid,
                    message.date,
                    true,
                    message.key
                )
                reference.child(account.uid ?: "").child("message").child(user.uid.toString())
                    .child(message.key ?: "").setValue(m)
                reference.child(user.uid ?: "").child("message").child(account.uid.toString())
                    .child(message.key ?: "").setValue(m)
                userMsg.text = message.text
                date.text = message.date
                Glide.with(context)
                    .load(user.photoUrl)
                    .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                    .into(img)
                if (user.isOnline == true) {
                    isOnline.setImageResource(R.drawable.online)
                } else {
                    isOnline.setImageResource(R.drawable.oflinee)
                }
            }
        }
    }

    inner class FromVh(val itemFromBinding: ItemFromBinding) :
        RecyclerView.ViewHolder(itemFromBinding.root) {
        fun onBind(message: Message) {
            itemFromBinding.apply {
                if (message.isChecked == true) {
                    isCheck.setImageResource(R.drawable.ic_check2)
                } else {
                    isCheck.setImageResource(R.drawable.ic_check1)
                }
                userMsg.text = message.text
                date.text = message.date
                Glide.with(context)
                    .load(account.photoUrl)
                    .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                    .into(img)
                if (account.isOnline == true) {
                    isOnline.setImageResource(R.drawable.online)
                } else {
                    isOnline.setImageResource(R.drawable.oflinee)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return ToVh(ItemToBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
            holder.onBind(list[position])
        } else if (holder is FromVh) {
            holder.onBind(list[position])
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
}