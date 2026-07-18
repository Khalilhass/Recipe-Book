package com.example.recipe_book.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.recipe_book.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Temporary Phase 0 destination.
 *
 * Purpose: confirm the whole stack boots cleanly — Hilt injection,
 * Firebase initialization (via google-services.json), Cloudinary
 * MediaManager initialization, and Navigation Component — before any
 * real feature work begins.
 *
 * Deleted/replaced in Phase 1 once LoginFragment becomes the graph's
 * start destination (see blueprint Section 9).
 */
@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.destination.collect { destination ->
                    when (destination) {
                        is SplashDestination.Home ->
                            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToHomeFragment())
                        is SplashDestination.Login ->
                            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLoginFragment())
                        null -> Unit // still checking session
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}