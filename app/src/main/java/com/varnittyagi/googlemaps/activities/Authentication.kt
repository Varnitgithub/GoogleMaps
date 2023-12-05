package com.varnittyagi.googlemaps.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.varnittyagi.googlemaps.R
import com.varnittyagi.googlemaps.databinding.ActivityAuthenticationBinding
import com.varnittyagi.googlemaps.models.UserDetails
import java.util.Objects

class Authentication : AppCompatActivity() {
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var userMap: HashMap<String, Objects>? = null
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var binding: ActivityAuthenticationBinding


    companion object {
        private const val REQUEST_CODE_SIGN_IN = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseDatabase = Firebase.database.getReference("Users")
        if (firebaseAuth.currentUser!=null){
            startActivity(Intent(this,UsersActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
                val signInIntent = mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
            }



        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                val googleSignInAccount = task.result
                if (googleSignInAccount != null) {
                    val idToken = googleSignInAccount.idToken
                    if (idToken != null) {
                        signIn(idToken)
                        Log.d("TAGGGGGGG", "onActivityResult: id token $idToken")
                    }
                } else {
                    Log.d("TAGGGG", "Google Sign-In failed")
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun signIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TAGGGG", "saveUserOnFirebase: here 3")
                    saveUserOnFirebase()
                    startActivity(Intent(this, UsersActivity::class.java))
                    finishAffinity()
                } else {
                    Log.e("TAGGGG", "signInWithCredential: ${task.exception}")
                }
            }.addOnFailureListener {
                Log.d("TAGGGG", "signIn: error ${it.localizedMessage}")
            }
    }

    private fun saveUserOnFirebase() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            // Save user data to Firebase
            val userDetails = UserDetails(
                user.email!!, user.displayName!!,
                user.photoUrl.toString(), user.uid
            )

            firebaseDatabase.child(user.uid).setValue(userDetails)
        }
    }

    fun checkLoggedInUser() {
        firebaseDatabase.child("users").orderByChild("email").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.getValue(String::class.java)

                    if (email == firebaseAuth.currentUser?.email) {
                        Log.d("TAGGGGGGGG", "onDataChange: $email")

                    } else {
                        Log.d("TAGGGGGGGG", "onDataChange: user not found")

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}