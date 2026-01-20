package com.yusdesign.notejoke

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yusdesign.notejoke.data.LoveMeterRepository
import com.yusdesign.notejoke.databinding.ActivityMainBinding
import com.yusdesign.notejoke.ui.LoveMeterAdapter
import com.yusdesign.notejoke.worker.ContentCheckWorker
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: LoveMeterAdapter
    private val repository by lazy { LoveMeterRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadData()

        binding.fabRefresh.setOnClickListener {
            loadData()
        }

        // Schedule the background worker to check for updates
        ContentCheckWorker.scheduleWork(this)
    }

    private fun setupRecyclerView() {
        adapter = LoveMeterAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadData() {
        lifecycleScope.launch {
            val entries = repository.fetchLatestEntries()
            adapter.updateData(entries)
        }
    }
}
