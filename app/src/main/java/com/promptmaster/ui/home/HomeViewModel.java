package com.promptmaster.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import com.promptmaster.data.Prompt;
import com.promptmaster.data.PromptRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function2;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Prompt>> allPrompts;

    @Inject
    public HomeViewModel(PromptRepository promptRepository) {
        allPrompts = new MutableLiveData<>();

        Flow<List<Prompt>> promptsFlow = promptRepository.getPromptsFiltered(null);

        BuildersKt.launch(ViewModelKt.getViewModelScope(this), null, null,
            new Function2<CoroutineScope, Continuation<? super Unit>, Object>() {
                @Override
                public Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> $completion) {
                    promptsFlow.collect(new FlowCollector<List<Prompt>>() {
                        @Override
                        public Object emit(List<Prompt> prompts, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
                            allPrompts.postValue(prompts);
                            return kotlin.Unit.INSTANCE;
                        }
                    }, $completion);
                    return kotlin.Unit.INSTANCE;
                }
            }
        );
    }

    public LiveData<List<Prompt>> getAllPrompts() {
        return allPrompts;
    }
}
