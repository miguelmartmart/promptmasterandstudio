package com.promptmaster.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.promptmaster.databinding.FragmentFavoritesBinding;
import com.promptmaster.ui.PromptViewModel; // Assuming a shared ViewModel or a specific FavoritesViewModel
import com.promptmaster.ui.home.PromptAdapter;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    private PromptAdapter favoritesAdapter; // Assuming a similar adapter for favorites
    private PromptViewModel promptViewModel; // Using the same ViewModel for simplicity, or create a new one

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("PromptMasterDebug", "FavoritesFragment: onCreateView called.");
        promptViewModel = new ViewModelProvider(this).get(PromptViewModel.class); // Get PromptViewModel
        Log.d("PromptMasterDebug", "FavoritesFragment: PromptViewModel obtained.");

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerViewPrompts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoritesAdapter = new PromptAdapter(); // Reusing PromptAdapter, or create FavoritesAdapter
        recyclerView.setAdapter(favoritesAdapter);

        // Observe favorite prompts from PromptViewModel (assuming a method like getFavoritePromptsLiveData)
        // For now, observing all prompts, you might need to filter or have a separate LiveData for favorites
        promptViewModel.getPromptsLiveData().observe(getViewLifecycleOwner(), prompts -> {
            Log.d("PromptMasterDebug", "FavoritesFragment: Observing favorite prompts. Received " + (prompts != null ? prompts.size() : 0) + " prompts.");
            // You would typically filter these prompts for favorites here or in the ViewModel
            favoritesAdapter.setPrompts(prompts); // This will need to be updated to show only favorites
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
