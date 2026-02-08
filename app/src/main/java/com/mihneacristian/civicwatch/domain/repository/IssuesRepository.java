package com.mihneacristian.civicwatch.domain.repository;

import com.mihneacristian.civicwatch.data.dto.IssuesDTO;

import java.util.List;

public interface IssuesRepository {
    List<IssuesDTO> getIssues();
}