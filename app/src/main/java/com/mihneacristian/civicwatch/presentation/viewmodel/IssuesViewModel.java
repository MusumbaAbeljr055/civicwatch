package com.mihneacristian.civicwatch.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mihneacristian.civicwatch.data.dto.IssuesDTO;
import com.mihneacristian.civicwatch.data.remote.ApplicationAPI;
import com.mihneacristian.civicwatch.data.remote.RemoteDataSource;

import java.util.List;

public class IssuesViewModel extends ViewModel {

    private final MutableLiveData<List<IssuesDTO>> issuesList = new MutableLiveData<>();
    private final RemoteDataSource remoteDataSource;

    public IssuesViewModel() {
        ApplicationAPI api = ApplicationAPI.createAPI();
        remoteDataSource = new RemoteDataSource(api);
        loadIssues();
    }

    public LiveData<List<IssuesDTO>> getIssuesList() {
        return issuesList;
    }

    private void loadIssues() {
        new Thread(() -> {
            List<IssuesDTO> issues = remoteDataSource.getIssues();
            issuesList.postValue(issues);
        }).start();
    }
}