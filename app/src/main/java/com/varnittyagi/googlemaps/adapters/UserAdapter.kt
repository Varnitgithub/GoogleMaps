package com.varnittyagi.googlemaps.adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.models.LastOnlineData
import com.varnittyagi.googlemaps.models.Messages
import com.varnittyagi.googlemaps.models.UserDetails

class UserAdapter(private var context: Context, private var listener:setonitemclclick) : RecyclerView.Adapter<UserAdapter.Myviewholder>() {
    private val items = ArrayList<UserDetails>()
private var databaseReference = FirebaseDatabase.getInstance().reference
    private var currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val handler = Handler(Looper.getMainLooper())
    var messageList  = ArrayList<Messages>()
    var recentMessage = HashMap<String,String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myviewholder {
        val itemview =
            Myviewholder(LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
            )
        itemview.userCardView.setOnClickListener {
            listener.onitemclicks(items[itemview.adapterPosition],itemview.adapterPosition)
        }
        return itemview
    }

    override fun onBindViewHolder(holder: Myviewholder, position: Int) {
        val currentitem = items[position]
        readMessageFromFirebase(currentitem.uid!!, object : ReadMessageCallback {
            override fun onReadMessageComplete(uid: String, message: String?) {
                getLastOnline(currentitem.uid!!,object :LastOnlineCallback{
                    override fun onLastOnlineReceived(uid: String, lastOnline: String?) {

                        holder.userName.text = currentitem.name
                        Glide.with(context).load(currentitem.profile).placeholder(R.drawable.userpng).into(holder.userPhoto)
                        holder.recentMessage.text = message.toString()
                        holder.lastOnline.text = "$lastOnline"
                    }

                })


            }
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateddata(updateddata: ArrayList<UserDetails>) {
        items.clear()
        items.addAll(updateddata)
        notifyDataSetChanged()
    }

    class Myviewholder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
         var userPhoto: ImageView = itemView.findViewById(R.id.userPhoto)
        var userName: TextView = itemView.findViewById(R.id.userName)
        var userCardView:LinearLayout = itemView.findViewById(R.id.userCardView)
        var recentMessage:TextView = itemView.findViewById(R.id.recentMessage)
        var lastOnline:TextView = itemView.findViewById(R.id.lastOnline)

    }

    interface LastOnlineCallback {
        fun onLastOnlineReceived(uid: String, lastOnline: String?)
    }
    fun getLastOnline(receiverUserUid: String, callback: LastOnlineCallback) {
        databaseReference.child("LastOnline").child(receiverUserUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val time = snapshot.getValue(LastOnlineData::class.java)

                // Pass the result to the callback
                callback.onLastOnlineReceived(receiverUserUid, time?.lastOnlineTime)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }

    interface setonitemclclick {
        fun onitemclicks(items: UserDetails,position: Int)

    }
    interface ReadMessageCallback {
        fun onReadMessageComplete(uid: String, message: String?)
    }

    private fun readMessageFromFirebase(receiverUserUid: String, callback: ReadMessageCallback) {
        messageList.clear()
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (messageSnapshot: DataSnapshot in dataSnapshot.children) {
                    val message = messageSnapshot.getValue(Messages::class.java)

                    if (message?.senderId == currentUserUid && message?.recieverId == receiverUserUid ||
                        message?.senderId == receiverUserUid && message.recieverId == currentUserUid
                    ) {
                        messageList.add(message)
                        recentMessage[receiverUserUid] = message.message
                    }
                }
                // Pass the result to the callback
                callback.onReadMessageComplete(receiverUserUid, recentMessage[receiverUserUid])
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
            }
        }
        databaseReference.child("messages").addValueEventListener(valueEventListener)
    }


}