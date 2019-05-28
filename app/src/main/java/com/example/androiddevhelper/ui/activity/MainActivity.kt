package com.example.androiddevhelper.ui.activity

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
import com.example.androiddevhelper.R
import com.example.androiddevhelper.data.local.PostData
import com.example.androiddevhelper.injection.App.Companion.applicationComponent
import com.example.androiddevhelper.injection.injectViewModel
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val injector = applicationComponent
    private val viewModel by injectViewModel { injector.mainViewModel }
    private val myAdapter = injector.myAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRecyclerView()
        addSwipeToDelete()


        viewModel.getNewPostDataList()
            .observe(this, Observer { newPostDataList -> updateNewRedditPost(newPostDataList) })

        startServiceButton.setOnClickListener { viewModel.startService() }

        serviceResetButton.setOnClickListener { viewModel.resetService() }
    }

    private fun initRecyclerView() {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = myAdapter
        }
    }

    private fun addSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // only can swipe right or left
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            //Will delete the item swiped from the local db if not empty
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                myAdapter.fetchItem(viewHolder.adapterPosition)?.let { clickedItem ->
                    viewModel.deleteItem(clickedItem.title)
                }

            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun updateNewRedditPost(newPostDataList: List<PostData>) =
        myAdapter.updateList(newPostDataList)


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.my_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        Intent(this, SettingsActivity::class.java).also { settingsIntent ->
            startActivity(settingsIntent)
        }
        return true
    }
}