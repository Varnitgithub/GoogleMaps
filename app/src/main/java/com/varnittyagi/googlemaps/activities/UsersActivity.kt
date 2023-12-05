package com.varnittyagi.googlemaps.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.adapters.UserAdapter
import com.varnittyagi.googlemaps.databinding.ActivityUsersBinding
import com.varnittyagi.googlemaps.models.UserDetails

class UsersActivity : AppCompatActivity(), UserAdapter.setonitemclclick {
    private lateinit var userAdapter: UserAdapter
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityUsersBinding
    private lateinit var currentUserUid: String
    private var userName: String? = null
    private lateinit var userList: ArrayList<UserDetails>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_users)
        firebaseAuth = FirebaseAuth.getInstance()
        currentUserUid = firebaseAuth.currentUser?.uid!!
        firebaseDatabase = Firebase.database.getReference("Users")
        userList = arrayListOf()
        userAdapter = UserAdapter(this, this)
        binding.usersRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.usersRecyclerview.adapter = userAdapter
        getCurrentUserFromFirebase()
        getUsersFromFirebase()

        binding.searchBtn.setOnClickListener {
            val searchedUserList = arrayListOf<UserDetails>()
            val user = binding.searchUserEdit.text.toString().trim()

            val searchedUser = searchUser(user)
            if (searchedUser != null) {
                Log.d("TAGGGGGGGGG", "onCreate: searched user $searchedUser")
                for (i in searchedUser.indices) {
                    searchedUserList.add(searchedUser[i])

                }
                userAdapter.updateddata(searchedUserList)
            } else {
                Log.d("TAGGGGGGGGG", "onCreate: searched user is null")

            }

        }

    }

    private fun searchUser(name: String): List<UserDetails>? {
        var firstName = ""
        var middleName = ""
        var lastName = ""
        val nameInSmalls = name.trim().lowercase() ?: "user"
        val mName = nameInSmalls.split(" ")
        return userList.filter {
            val normalizedUserName = it.name?.lowercase()
            normalizedUserName!!.contains(nameInSmalls) ||
                    normalizedUserName.split(" ").any { part -> part.contains(nameInSmalls) }
            /* if (mName.size > 2) {
            firstName = mName[0]
             middleName = mName[1]
              lastName = mName[2]

        }else{
             firstName = mName[0]
             lastName = mName[1]
        }*/

            //return userList.find { it.name==trimmedInput }

        }
    }


    private fun getUsersFromFirebase() {
        firebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TAGGGGGGGGGG", "onDataChange: ${snapshot.childrenCount}")
                for (users in snapshot.children) {
                    val user = users.getValue<UserDetails>()
                    if (user?.uid != currentUserUid) {
                        userList.add(user ?: UserDetails("", "", "", ""))

                    }

                }
                Log.d("TAGGGGGGGGGG", "onDataChange: ${userList}")

                userAdapter.updateddata(userList)
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun getCurrentUserFromFirebase() {
        firebaseDatabase.child(currentUserUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<UserDetails>()// users.getValue<UserDetails>()
                binding.currentUserName.text = user?.name
                userName = user?.name
                Glide.with(this@UsersActivity).load(Uri.parse(user?.profile))
                    .placeholder(R.drawable.userpng).into(binding.currentUserPhoto)


            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onitemclicks(items: UserDetails, position: Int) {
        val intent = nextScreen(items)
        startActivity(intent)
    }

    private fun nextScreen(items: UserDetails): Intent {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", items)
        intent.putExtra("userName", userName)
        return intent
    }


}