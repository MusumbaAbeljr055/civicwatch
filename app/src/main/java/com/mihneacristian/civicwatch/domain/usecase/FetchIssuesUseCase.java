package com.mihneacristian.civicwatch.domain.usecase;

import androidx.lifecycle.LiveData;

import com.mihneacristian.civicwatch.data.model.Issue;
import com.mihneacristian.civicwatch.domain.mediator.IssuesMediator;

import java.util.List;

public class FetchIssuesUseCase {

    private final IssuesMediator issuesMediator;

    public FetchIssuesUseCase(IssuesMediator issuesMediator) {
        this.issuesMediator = issuesMediator;
    }

    public LiveData<List<Issue>> getIssuesUseCase() {
        return issuesMediator.getIssuesMediator();
    }
}