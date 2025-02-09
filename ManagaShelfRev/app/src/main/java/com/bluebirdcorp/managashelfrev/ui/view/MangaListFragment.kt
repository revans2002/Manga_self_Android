package com.bluebirdcorp.managashelfrev.ui.view

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluebirdcorp.managashelfrev.R
import com.bluebirdcorp.managashelfrev.databinding.FragmentMangaListBinding
import com.bluebirdcorp.managashelfrev.db.MangaDatabase
import com.bluebirdcorp.managashelfrev.model.Manga
import com.bluebirdcorp.managashelfrev.repository.MangaRepository
import com.bluebirdcorp.managashelfrev.ui.adapter.MangaAdapter
import com.bluebirdcorp.managashelfrev.ui.viewmodel.MangaViewModel
import com.bluebirdcorp.managashelfrev.ui.viewmodel.MangaViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MangaListFragment : Fragment() {
    private var _binding: FragmentMangaListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context
    private lateinit var mangAdapter: MangaAdapter
    private var mangaCollectionJob: Job? = null
    private lateinit var scrollListener: RecyclerView.OnScrollListener
    private var isTabSwitching = false
    private val viewModel: MangaViewModel by viewModels {
        MangaViewModelFactory(
            MangaRepository(
                MangaDatabase.getDatabase(requireContext()).mangaDao()
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMangaListBinding.inflate(inflater, container, false)
        mContext = inflater.context
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter with a click listener for showing details
        mangAdapter = MangaAdapter(viewModel) { manga ->
            showMangaDetails(manga)
        }

       setClickListeners()

        viewModel.loadAllMangas()

        // Setup RecyclerView with either Grid or Linear layout based on orientation
        binding.recyclerView.apply {
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    GridLayoutManager(requireContext(), 2)
                } else {
                    LinearLayoutManager(requireContext())
                }
            adapter = mangAdapter
        }

      setUpObservers()

       setUpNavListeners()

    }

    private fun setClickListeners(){
        binding.imgToggleChipGroup.setOnClickListener {
            val isVisible = binding.chipGroupSort.visibility == View.VISIBLE

            // Toggle visibility
            binding.chipGroupSort.visibility = if (isVisible) View.GONE else View.VISIBLE

            // Animate arrow rotation
            binding.imgToggleChipGroup.animate()
                .rotation(if (isVisible) 0f else 180f)
                .setDuration(200)
                .start()
        }

        // Pagination button listeners
        binding.btnNext.setOnClickListener {
            viewModel.loadNextPage()
        }
        binding.btnPrevious.setOnClickListener {
            viewModel.loadPreviousPage()
        }
    }

    private fun setUpObservers(){
        mangaCollectionJob = lifecycleScope.launch {
            viewModel.currentPage.collectLatest { page ->
                val totalPages = viewModel.totalPages.value
                binding.tvPageIndicator.text = "Page $page of $totalPages"
            }
        }

        // Observe manga list and update adapter when data changes
        mangaCollectionJob = lifecycleScope.launch {
            viewModel.items.collectLatest { mangas ->
                mangAdapter.setMangaList(mangas)
            }
        }

        // Setup search EditText filtering
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mangAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupSort.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.chipScoreAll -> viewModel.loadAllMangas()
                R.id.chipScoreAsc -> {
                    viewModel.sortByScoreAscending()
                    Log.d("TAG", "onViewCreated: sorting")
                }
                R.id.chipScoreDesc -> viewModel.sortByScoreDescending()
                R.id.chipPopularityAsc -> viewModel.sortByPopularityAscending()
                R.id.chipPopularityDesc -> viewModel.sortByPopularityDescending()
            }
        }

    }

    private fun setUpNavListeners(){
        // Handle year tab selection
        binding.yearTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    isTabSwitching = true
                    val year = it.text.toString().toInt()
                    scrollToYear(year)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(isTabSwitching) {
                    updateSelectedYearTab()
                }
            }
        }

        // Setup bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (binding.etSearch.isVisible) {
                        binding.etSearch.visibility = View.GONE
                    }
                    mangAdapter.unsetFavorites()
                    mangaCollectionJob?.cancel()
                    mangaCollectionJob = lifecycleScope.launch{
                        viewModel.items.collectLatest { mangas ->
                            mangAdapter.setMangaList(mangas)
                        }
                    }
                    binding.recyclerView.removeOnScrollListener(scrollListener)
                    mangAdapter.scrollToTop(binding.recyclerView)
                    binding.paginationLayout.visibility = View.VISIBLE
                    binding.yearTabs.visibility = View.GONE
                    binding.chipGroupSort.visibility = View.GONE
                    binding.imgToggleChipGroup.visibility = View.GONE
                    true
                }

                R.id.nav_tab -> {
                    if (binding.etSearch.isVisible) {
                        binding.etSearch.visibility = View.GONE
                    }
                    binding.paginationLayout.visibility = View.GONE
                    binding.yearTabs.visibility = View.VISIBLE
                    binding.chipGroupSort.visibility = View.GONE
                    binding.imgToggleChipGroup.visibility = View.GONE
                    // Load all data and sort by year
                    setAllDataIntoAdapter()
                    binding.recyclerView.addOnScrollListener(scrollListener)
                    mangAdapter.scrollToTop(binding.recyclerView)
                    mangaCollectionJob?.cancel()
                    viewModel.loadAllMangas()
                    mangaCollectionJob = lifecycleScope.launch {
                        viewModel.allMangas.collectLatest { mangas ->
                            setupYearTabs(mangas)
                            mangAdapter.setMangaList(mangas.sortedBy { it.publishedChapterDate })
                        }
                    }
                    true
                }

                R.id.nav_favorites -> {
                    if (binding.etSearch.isVisible) {
                        binding.etSearch.visibility = View.GONE
                    }
                    binding.paginationLayout.visibility = View.GONE
                    binding.yearTabs.visibility = View.GONE
                    mangaCollectionJob?.cancel()
                    mangAdapter.scrollToTop(binding.recyclerView)
                    binding.recyclerView.removeOnScrollListener(scrollListener)
                    mangaCollectionJob = lifecycleScope.launch {
                        viewModel.getLikedManga().observe(viewLifecycleOwner) { likedMangaList ->
                            mangAdapter.setFavorites(likedMangaList)
                        }
                    }
                    binding.chipGroupSort.visibility = View.GONE
                    binding.imgToggleChipGroup.visibility = View.GONE
                    true
                }

                R.id.action_search -> {
                    binding.etSearch.visibility = View.VISIBLE
                    mangAdapter.unsetFavorites()
                    binding.paginationLayout.visibility = View.GONE
                    binding.yearTabs.visibility = View.GONE
                    mangAdapter.scrollToTop(binding.recyclerView)
                    binding.recyclerView.removeOnScrollListener(scrollListener)
                    binding.chipGroupSort.visibility = View.GONE
                    binding.imgToggleChipGroup.visibility = View.GONE
                    viewModel.loadAllMangas()
                    setAllDataIntoAdapter()
                    true
                }

                R.id.action_filter -> {
                    if (binding.etSearch.isVisible) {
                        binding.etSearch.visibility = View.GONE
                    }
                    binding.paginationLayout.visibility = View.GONE
                    binding.yearTabs.visibility = View.GONE
                    mangAdapter.unsetFavorites()
                    mangAdapter.scrollToTop(binding.recyclerView)
                    binding.recyclerView.removeOnScrollListener(scrollListener)
                    binding.chipScoreAll.isChecked = true
                    binding.chipGroupSort.visibility = View.GONE
                    binding.imgToggleChipGroup.visibility = View.VISIBLE
                    setAllDataIntoAdapter()
                    true
                }

                else -> false
            }
        }

    }

    private fun setupYearTabs(mangas: List<Manga>) {
        val years =
            mangas.map { it.publishedChapterDate?.let { date -> getYearFromTimestamp(date) } }
                .distinct()

        binding.yearTabs.removeAllTabs()
        years.forEach { year ->
            if (year != null) {
                binding.yearTabs.addTab(binding.yearTabs.newTab().setText(year.toString()))
            }
        }

    }



    private fun scrollToYear(year: Int) {
        val position = mangAdapter.getPositionForYear(year)
        Log.d("MangaListFragment", "Scrolling to year: $year, position: $position")
        if (position != -1) {
            binding.recyclerView.scrollToPosition(position)
        } else {
            Log.d("MangaListFragment", "No manga found for year: $year")
        }
        isTabSwitching = false
    }

    private fun updateSelectedYearTab() {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        val year = mangAdapter.getYearForPosition(firstVisibleItemPosition)
        Log.d("MangaListFragment", "Updating selected year tab. First visible position: $firstVisibleItemPosition, year: $year")
        if (year != null) {
            for (i in 0 until binding.yearTabs.tabCount) {
                val tabYear = binding.yearTabs.getTabAt(i)?.text?.toString()?.toIntOrNull()
                if (tabYear == year) {
                    binding.yearTabs.getTabAt(i)?.select()
                    Log.d("MangaListFragment", "Selected tab for year: $year")
                    break
                }
            }
        }
    }

    private fun getYearFromTimestamp(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar.get(Calendar.YEAR)
    }

    private fun setAllDataIntoAdapter() {
        mangaCollectionJob?.cancel()
        mangaCollectionJob = lifecycleScope.launch{
            viewModel.allMangas.collectLatest { mangas ->
                mangAdapter.setMangaList(mangas)
            }
        }
    }

    private fun showMangaDetails(manga: Manga) {
        val builder = AlertDialog.Builder(mContext)
        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_manga_details, null)

        val mangaImage: ImageView = dialogView.findViewById(R.id.iv_manga_cover)
        val mangaTitle: TextView = dialogView.findViewById(R.id.tv_manga_title)
        val mangaScore: TextView = dialogView.findViewById(R.id.tv_manga_score)
        val mangaPopularity: TextView = dialogView.findViewById(R.id.tv_manga_popularity)
        val mangaPublishedDate: TextView = dialogView.findViewById(R.id.tv_manga_published_date)
        val mangaCategory: TextView = dialogView.findViewById(R.id.tv_manga_category)
        val likeButton: ImageButton = dialogView.findViewById(R.id.btn_like)
        val closeButton: Button = dialogView.findViewById(R.id.btn_close)

        // Populate the details from the Manga model
        mangaTitle.text = manga.title ?: "Unknown Title"
        mangaScore.text = "⭐ Score: ${manga.score ?: "N/A"}"
        mangaPopularity.text = "🔥 Popularity: ${manga.popularity ?: "N/A"}"
        mangaPublishedDate.text = "📅 Published: ${convertLongToDate(manga.publishedChapterDate) ?: "Unknown"}"
        mangaCategory.text = "📚 Category: ${manga.category ?: "Unknown"}"

        Glide.with(requireContext())
            .load(manga.image)
            .placeholder(R.drawable.burst)
            .into(mangaImage)

        // Set initial like state
        if (manga.isLiked) {
            likeButton.setImageResource(R.drawable.ic_heart_filled)
        } else {
            likeButton.setImageResource(R.drawable.ic_heart_unfilled)
        }

        // Toggle like state when clicked
        likeButton.setOnClickListener {
            manga.isLiked = !manga.isLiked
            if (manga.isLiked) {
                likeButton.setImageResource(R.drawable.ic_heart_filled)
            } else {
                likeButton.setImageResource(R.drawable.ic_heart_unfilled)
            }
            viewModel.updateManga(manga)
        }

        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.show()

        // Close the dialog when close button is clicked
        closeButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun convertLongToDate(timeInMillis: Long?, format: String = "dd/MM/yyyy"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        if(timeInMillis!=null){
            return sdf.format(Date(timeInMillis))
        }
        else{
            return "N/A"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}