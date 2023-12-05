import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.models.Messages


class MessageAdapter(private val context: Context,private var receiverPhoto:String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    fun updateList(newList: ArrayList<Messages>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()

        //   notifyItemRangeInserted(messages.size - newList.size, newList.size)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder.itemViewType) {
            VIEW_TYPE_SENT -> {
                val sentMessageHolder = (holder as SentMessageViewHolder)
                sentMessageHolder.sendText.text = message.message
                Glide.with(context).load(senderPhoto).into(holder.outgoingImage)
            }

            VIEW_TYPE_RECEIVED -> {
                val receivedMessageHolder = (holder as ReceivedMessageViewHolder)
                receivedMessageHolder.recieveText.text = message.message
                Glide.with(context).load((Uri.parse(receiverPhoto))).into(holder.incomingImage)

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
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var recieveText: TextView = itemView.findViewById(R.id.incomingMessage)
        var incomingImage: ImageView = itemView.findViewById(R.id.incomingImage)

    }
}


