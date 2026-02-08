package com.mihneacristian.civicwatch.domain.mediator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mihneacristian.civicwatch.data.dto.IssuesDTO;
import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.domain.repository.IssuesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IssuesMediator {

    private final IssuesRepository remoteRepository;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Issue>> liveDataIssues;

    public IssuesMediator(IssuesRepository remoteRepository) {
        this.remoteRepository = remoteRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        this.liveDataIssues = new MutableLiveData<>();
    }

    public LiveData<List<Issue>> getIssuesMediator() {
        executorService.execute(() -> {
            List<Issue> issues = new ArrayList<>();
            List<IssuesDTO> dtos = remoteRepository.getIssues();

            for (IssuesDTO dto : dtos) {
                Issue issue = new Issue(
                        dto.getIssueTitle(),
                        dto.getIssueDescription(),
                        dto.getIssueType(),
                        dto.getIssueSeverity(),
                        dto.getLat(),
                        dto.getLng(),
                        "", // address
                        "", // photoBase64
                        "", // reporterId
                        dto.getUserName(),
                        dto.getUserEmailAddress()
                );
                issue.setCreatedAt(dto.getDateAdded());
                issue.setPhotoBase64(dto.getPhotoBase64());
                issues.add(issue);
            }
            this.liveDataIssues.postValue(issues);
        });
        return liveDataIssues;
    }
}