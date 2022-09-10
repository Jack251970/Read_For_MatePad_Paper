package com.jack.bookshelf.view.popupwindow

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.SearchView

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.jack.bookshelf.R
import com.jack.bookshelf.bean.BookChapterBean
import com.jack.bookshelf.bean.BookShelfBean
import com.jack.bookshelf.databinding.PopReadChapterBookmarkBinding
import com.jack.bookshelf.view.fragment.BookmarkFragment
import com.jack.bookshelf.view.fragment.ChapterListFragment
import com.jack.bookshelf.widget.viewpager.PaperViewPager

/**
 * Read Chapter & Bookmark Menu
 * Adapt to Huawei MatePad Paper
 * Edited by Jack251970
 */

class ReadChapterBookmarkPop : FrameLayout {

    private val binding = PopReadChapterBookmarkBinding.inflate(LayoutInflater.from(context), this, true)
    private var mSearchAutoComplete: SearchView.SearchAutoComplete? = null
    private var bookShelf: BookShelfBean? = null
    private var chapterBeanList: List<BookChapterBean>? = null
    private var callback: Callback? = null

    private val mVp: PaperViewPager = binding.tabVp
    private var tabFragmentPageAdapter: TabFragmentPageAdapter? = null
    private var mFragmentList: List<Fragment>? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        binding.vwBg.setOnClickListener(null)
    }

    fun setData(bookShelf: BookShelfBean, chapterBeanList: List<BookChapterBean>): ReadChapterBookmarkPop {
        this.bookShelf = bookShelf
        this.chapterBeanList = chapterBeanList
        upView()
        return this
    }

    fun setCallback(callback: Callback): ReadChapterBookmarkPop {
        this.callback = callback
        initView()
        return this
    }

    private fun initView() {
        initSearchView()
        mVp.offscreenPageLimit = 2
        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.tvChapterList.setOnClickListener {
            setCurrentItem(0)
            binding.ivChapterListIndicator.visibility = VISIBLE
            binding.ivBookmarkIndicator.visibility = INVISIBLE
        }
        binding.tvBookmark.setOnClickListener {
            setCurrentItem(1)
            binding.ivChapterListIndicator.visibility = INVISIBLE
            binding.ivBookmarkIndicator.visibility = VISIBLE
        }
        binding.ivSearch.setOnClickListener {
            binding.tvBookTitleChapterList.visibility = GONE
            binding.ivSearch.visibility = GONE
            binding.searchView.visibility = VISIBLE
        }
    }

    private fun upView() {
        binding.tvBookTitleChapterList.text = bookShelf?.bookInfoBean?.name
        binding.ivChapterListIndicator.visibility = VISIBLE
        binding.ivBookmarkIndicator.visibility = INVISIBLE
        mFragmentList = createTabFragments()
        tabFragmentPageAdapter = TabFragmentPageAdapter(callback?.getSupportFragmentManager())
        mVp.adapter = tabFragmentPageAdapter
    }

    private fun getCurrentItem(): Int {
        return mVp.currentItem
    }

    private fun setCurrentItem(item: Int) {
        if (getCurrentItem() != item) {
            mVp.currentItem = item
        }
    }

    private fun createTabFragments(): List<Fragment> {
        val chapterListFragment = ChapterListFragment(bookShelf,chapterBeanList)
        val bookmarkFragment = BookmarkFragment(bookShelf)
        return listOf<Fragment>(chapterListFragment, bookmarkFragment)
    }

    private fun initSearchView() {
        mSearchAutoComplete = binding.searchView.findViewById(R.id.search_src_text)
        mSearchAutoComplete!!.textSize = 16f
        val closeButton: ImageView = binding.searchView.findViewById(R.id.search_close_btn)
        closeButton.setBackgroundColor(Color.TRANSPARENT)
        closeButton.setImageResource(R.drawable.ic_close)
        closeButton.setOnClickListener { searchViewCollapsed() }
        binding.searchView.onActionViewExpanded()
        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (getCurrentItem() == 1) {
                    (mFragmentList?.get(1) as BookmarkFragment).startSearch(newText)
                } else {
                    (mFragmentList?.get(0) as ChapterListFragment).startSearch(newText)
                }
                return false
            }
        })
    }

    private fun onBackPressed() {
        if (binding.searchView.visibility == VISIBLE) {
            searchViewCollapsed()
            return
        }
        callback?.back()
    }

    fun searchViewCollapsed() {
        mSearchAutoComplete!!.setText("")
        binding.tvBookTitleChapterList.visibility = VISIBLE
        binding.ivSearch.visibility = VISIBLE
        binding.searchView.visibility = GONE
    }

    inner class TabFragmentPageAdapter internal constructor(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            return mFragmentList!![position]
        }

        override fun getCount(): Int {
            return mFragmentList!!.size
        }
    }

    fun finish() {
        callback?.back()
    }

    interface Callback {
        fun back()

        fun getSupportFragmentManager(): FragmentManager
    }
}