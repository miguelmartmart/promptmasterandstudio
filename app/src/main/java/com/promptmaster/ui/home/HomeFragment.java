package com.promptmaster.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log; // Explicitly import Log
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.promptmaster.databinding.FragmentHomeBinding;
import com.promptmaster.ui.PromptViewModel; // Explicitly import PromptViewModel

import dagger.hilt.android.AndroidEntryPoint;

import java.util.List; // Explicitly import List

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PromptAdapter promptAdapter;
    private PromptViewModel promptViewModel; // Declare PromptViewModel

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("PromptMasterDebug", "HomeFragment: onCreateView called.");
        promptViewModel = new ViewModelProvider(this).get(PromptViewModel.class); // Get PromptViewModel
        Log.d("PromptMasterDebug", "HomeFragment: PromptViewModel obtained.");

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerViewPrompts;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        promptAdapter = new PromptAdapter();
        recyclerView.setAdapter(promptAdapter);

        // Observe prompts from PromptViewModel
        promptViewModel.getPromptsLiveData().observe(getViewLifecycleOwner(), prompts -> {
            Log.d("PromptMasterDebug", "HomeFragment: Observing prompts. Received " + (prompts != null ? prompts.size() : 0) + " prompts.");
            promptAdapter.setPrompts(prompts);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
