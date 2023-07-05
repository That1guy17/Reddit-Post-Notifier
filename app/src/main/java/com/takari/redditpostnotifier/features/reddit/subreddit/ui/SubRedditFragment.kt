package com.takari.redditpostnotifier.features.reddit.subreddit.ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.databinding.FragmentPostDataBinding
import com.takari.redditpostnotifier.utils.injectViewModel
import com.takari.redditpostnotifier.features.reddit.SharedViewModel

class SubRedditFragment : Fragment() {

    private val viewModel: SharedViewModel by injectViewModel { App.applicationComponent().sharedViewModel }
    private val validationDialog = SubRedditValidationDialog()
    private val handler = Handler()

    //espresso kept giving a null error with this specific view for whatever reason unless I used findViewById
    private lateinit var queuedSubRedditRecyclerView: RecyclerView

    private val queuedSubredditsAdapter = QueuedSubredditsAdapter { clickedSubData ->
        viewModel.deleteDbSubRedditData(clickedSubData)
    }

    private val binding by lazy { FragmentPostDataBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.observeButton.setOnClickListener {
            viewModel.switchContainers(SharedViewModel.FragmentName.NewPostFragment)
        }

        binding.addSubRedditFab.setOnClickListener {
            if (!validationDialog.isAdded)
                validationDialog.show(
                    activity?.supportFragmentManager!!,
                    "SubRedditValidationDialog"
                )
        }

        queuedSubRedditRecyclerView = view.findViewById(R.id.queuedSubRedditRecyclerView)

        queuedSubRedditRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = queuedSubredditsAdapter
            queuedSubredditsAdapter.attachOnSwipe(this)
        }

        viewModel.dbSubRedditData.observe(viewLifecycleOwner, Observer { subRedditDataList ->

            queuedSubredditsAdapter.submitList(subRedditDataList.reversed())

            //without a delay the recycler view wouldn't scroll to the new item for whatever reason
            handler.postDelayed({
                queuedSubRedditRecyclerView
                    .smoothScrollToPosition(0)
            }, 500)

            //disabled if there's no data in the db
            binding.observeButton.isEnabled = subRedditDataList.isNotEmpty()

            //will be received by NewPostFragment
            viewModel.subRedditDataList = subRedditDataList
        })
    }
}