package com.akashsoam.kaltak.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.akashsoam.kaltak.R
import com.akashsoam.kaltak.databinding.FragmentArticleBinding
import com.akashsoam.kaltak.ui.NewsActivity
import com.akashsoam.kaltak.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment(R.layout.fragment_article) {

    lateinit var newsViewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()

    lateinit var binding: FragmentArticleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val article = args.article

        // Debugging: Log the URL
        Log.d("ArticleFragment", "Loading URL: ${article.url}")


        binding.webView.apply {
            webViewClient = WebViewClient()
//            article.url?.let { loadUrl(it) }
            settings.javaScriptEnabled = true
            loadUrl(article.url)
        }

        binding.fab.setOnClickListener {
            newsViewModel.addToFavourites(article)
            Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
//            Toast.makeText(context, "Article saved successfully", Toast.LENGTH_SHORT).show()

        }

    }


}