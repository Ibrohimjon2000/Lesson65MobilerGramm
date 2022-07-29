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
import uz.mobiler.lesson65.databinding.ItemToBinding
import uz.mobiler.lesson65.model.Group
import uz.mobiler.lesson65.model.GroupMessage
import uz.mobiler.lesson65.model.User

class GroupMessageAdapter(
    val context: Context,
    val list: List<GroupMessage>,
    val account: User,
    val group: Group?,
    val onItemClickListener: (message: GroupMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private val TO = 0
    private val FROM = 1

    inner class ToVh(val itemToBinding: ItemToBinding) :
        RecyclerView.ViewHolder(itemToBinding.root) {
        fun onBind(groupMessage: GroupMessage) {
            itemToBinding.apply {
                firebaseDatabase = FirebaseDatabase.getInstance()
                reference = firebaseDatabase.getReference("groups")
                val m = GroupMessage(
                    groupMessage.text,
                    groupMessage.user,
                    groupMessage.date,
                    true,
                    groupMessage.key,
                    groupMessage.imageUrl
                )
                reference.child(group?.groupKey ?: "").child("message")
                    .child(groupMessage.key ?: "")
                    .setValue(m)

                if (groupMessage.text.toString().isNotEmpty()) {
                    imageDate.visibility = View.GONE
                    sendImg.visibility = View.GONE
                    liner.visibility = View.VISIBLE
                    date.visibility = View.VISIBLE
                    userMsg.text = groupMessage.text
                    date.text = groupMessage.date
                } else if (groupMessage.imageUrl.toString().isNotEmpty()) {
                    imageDate.visibility = View.VISIBLE
                    sendImg.visibility = View.VISIBLE
                    liner.visibility = View.GONE
                    date.visibility = View.GONE
                    Glide.with(context)
                        .load(groupMessage.imageUrl)
                        .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                        .into(sendImg)
                    imageDate.text = groupMessage.date
                }

                Glide.with(context)
                    .load(groupMessage.user?.photoUrl)
                    .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                    .into(img)
            }
            itemView.setOnClickListener {
                onItemClickListener.invoke(groupMessage)
            }
        }
    }

    inner class FromVh(val itemFromBinding: ItemFromBinding) :
        RecyclerView.ViewHolder(itemFromBinding.root) {
        fun onBind(groupMessage: GroupMessage) {
            itemFromBinding.apply {
                if (groupMessage.text.toString().isNotEmpty()) {
                    if (groupMessage.isChecked == true) {
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
                    userMsg.text = groupMessage.text
                    date.text = groupMessage.date
                } else if (groupMessage.imageUrl.toString().isNotEmpty()) {
                    if (groupMessage.isChecked == true) {
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
                        .load(groupMessage.imageUrl)
                        .apply(RequestOptions().placeholder(R.drawable.profile).centerCrop())
                        .into(sendImg)
                    imageDate.text = groupMessage.date
                }
            }
            itemView.setOnClickListener {
                onItemClickListener.invoke(groupMessage)
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
        if (list[position].user?.uid == account.uid) {
            return FROM
        }
        return TO
    }
}