package com.varnittyagi.googlemaps.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.models.UserDetails

class UserAdapter(private var context: Context, private var listener:setonitemclclick) : RecyclerView.Adapter<UserAdapter.Myviewholder>() {
    private val items = ArrayList<UserDetails>()


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
        holder.userName.text = currentitem.name
        Glide.with(context).load(currentitem.profile).placeholder(R.drawable.userpng).into(holder.userPhoto)
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
        var userCardView:CardView = itemView.findViewById(R.id.userCardView)

    }

    interface setonitemclclick {
        fun onitemclicks(items: UserDetails,position: Int)

    }
}