import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.models.Messages
import com.varnittyagi.googlemaps.models.TypingStatus


class MessageAdapter(private val context: Context,private var receiverPhoto:String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
var databaseReference = FirebaseDatabase.getInstance().getReference()
    var currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        var senderPhoto = FirebaseAuth.getInstance().currentUser?.photoUrl
    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    private val messages = ArrayList<Messages>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.outgoing_messages, parent, false)
                SentMessageViewHolder(view)
            }

            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.incoming_messages, parent, false)
                ReceivedMessageViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    fun updateList(newList: List<Messages>) {
        val diffCallback = MessageDiffCallback(messages, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        messages.clear()
        messages.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                val sentMessageHolder = (holder as SentMessageViewHolder)
                sentMessageHolder.sendText.text = message.message
                Glide.with(context).load(senderPhoto).into(holder.outgoingImage)
                sentMessageHolder.outgoingTime.text = message.time
            }

            VIEW_TYPE_RECEIVED -> {
                val receivedMessageHolder = (holder as ReceivedMessageViewHolder)

                getTypingStatus(object : TypingStatusCallback {
                    override fun onTypingStatusReceived(isTyping: Boolean) {
                        if (isTyping){
                            Log.d("TAGGGGGGGGG", "onTypingStatusReceived: he is typing")
                            holder.typingIndicatorInc.visibility = View.VISIBLE
                        }else{
                            Log.d("TAGGGGGGGGG", "onTypingStatusReceived: he not typing")

                            holder.typingIndicatorInc.visibility = View.GONE
                            holder.msgconstraint.visibility = View.VISIBLE
                            receivedMessageHolder.recieveText.text = message.message
                            Glide.with(context).load((Uri.parse(receiverPhoto))).into(holder.incomingImage)
                            receivedMessageHolder.incomingTime.text = message.time
                        }
                    }
                })


            }
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        return if (messages[position].senderId == currentUserUid) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    // ViewHolders
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sendText: TextView = itemView.findViewById(R.id.outgoingMessage)
        var outgoingImage: ImageView = itemView.findViewById(R.id.outgoingImage)
        var outgoingTime :TextView = itemView.findViewById(R.id.Out_msg_Time)
        var typingIndicatorOut:LottieAnimationView = itemView.findViewById(R.id.typingAnimation_out)

    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recieveText: TextView = itemView.findViewById(R.id.incomingMessage)
        var incomingImage: ImageView = itemView.findViewById(R.id.incomingImage)
        var incomingTime:TextView = itemView.findViewById(R.id.inc_msg_time)
        var typingIndicatorInc:LottieAnimationView = itemView.findViewById(R.id.typingAnimation_inc)
var msgconstraint:ConstraintLayout = itemView.findViewById(R.id.msg_CL)
    }

    interface TypingStatusCallback {
        fun onTypingStatusReceived(isTyping: Boolean)
    }

    private fun getTypingStatus(callback: TypingStatusCallback) {
        databaseReference.child("TypingStatus").child(currentUserUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val typingStatus = snapshot.getValue(TypingStatus::class.java)
                    typingStatus?.let {
                        callback.onTypingStatusReceived(it.typingstatus)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }


    private class MessageDiffCallback(private val oldList: List<Messages>, private val newList: List<Messages>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].senderId == newList[newItemPosition].senderId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

}


