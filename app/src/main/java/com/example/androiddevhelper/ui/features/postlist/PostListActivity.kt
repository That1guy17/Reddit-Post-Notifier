package com.example.androiddevhelper.ui.features.postlist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddevhelper.App.Companion.applicationComponent
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.ui.features.settings.SettingsActivity
import com.example.androiddevhelper.utils.injectViewModel
import kotlinx.android.synthetic.main.activity_main.*


class PostListActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context) =
            Intent(context, PostListActivity::class.java)
    }

    private val injector = applicationComponent
    private val viewModel by injectViewModel { injector.postListViewModel }

    //Deletes item when clicked
    private val postListAdapter: PostListAdapter by lazy {
        PostListAdapter(this) { position ->
            postListAdapter.fetchItem(position)?.let { item ->
                viewModel.deleteItem(item.title)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
        addSwipeToDelete()

        //Observes local db, when changed the recycler view updates.
        viewModel.getNewPostDataList()
            .observe(this, Observer { newPostDataList -> updateNewRedditPost(newPostDataList) })

        startServiceButton.setOnClickListener { viewModel.startService() }

        serviceResetButton.setOnClickListener { viewModel.resetService() }
    }

    private fun initRecyclerView() {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PostListActivity)
            adapter = postListAdapter
        }
    }

    private fun addSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT //Only can swipe right or left
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false //Don't need this callback

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                postListAdapter.fetchItem(viewHolder.adapterPosition)?.let { clickedItem ->
                    viewModel.deleteItem(clickedItem.title)
                }

            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun updateNewRedditPost(newPostDataList: List<PostData>) =
        postListAdapter.updateList(newPostDataList)


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.my_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        startActivity(SettingsActivity.createIntent(this))
        return true
    }
}