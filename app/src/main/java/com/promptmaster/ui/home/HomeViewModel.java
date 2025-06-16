package com.promptmaster.ui.home;

import android.util.Log; // Import Log
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.promptmaster.data.Prompt;
import com.promptmaster.data.PromptRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final LiveData<List<Prompt>> allPrompts;

    @Inject
    public HomeViewModel(PromptRepository promptRepository) {
        Log.d("PromptMasterDebug", "HomeViewModel: Constructor called.");
        allPrompts = promptRepository.getAllPromptsLiveData();

        // Add an observer to log when allPrompts LiveData changes
        allPrompts.observeForever(prompts -> {
            Log.d("PromptMasterDebug", "HomeViewModel: allPrompts LiveData updated with " + (prompts != null ? prompts.size() : 0) + " prompts.");
        });
    }

    public LiveData<List<Prompt>> getAllPrompts() {
        Log.d("PromptMasterDebug", "HomeViewModel: getAllPrompts() called.");
        return allPrompts;
    }
}
