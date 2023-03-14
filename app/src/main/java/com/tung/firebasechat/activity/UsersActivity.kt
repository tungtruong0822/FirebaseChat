package com.tung.firebasechat.activity


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tung.firebasechat.R
import com.tung.firebasechat.adapter.UserAdapter
import com.tung.firebasechat.databinding.ActivityUsersBinding
import com.tung.firebasechat.firebase.FirebaseService
import com.tung.firebasechat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("DEPRECATION")
class UsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersBinding
    private val userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseService.token = token
        }


        binding.userRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgProfile.setOnClickListener {
            val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        getUsersList()
    }

    private fun getUsersList() {
        val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val userid = firebaseUser.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUser = snapshot.child(firebaseUser.uid).getValue(User::class.java)
                if (currentUser!!.profileImage.isEmpty()) {
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@UsersActivity).load(currentUser.profileImage).into(binding.imgProfile)
                }

                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)

                    if (user!!.userId != firebaseUser.uid) {
                        userList.add(user)
                    }
                }

                val userAdapter = UserAdapter(this@UsersActivity, userList)

                binding.userRecyclerView.adapter = userAdapter
            }

        })
    }
}
