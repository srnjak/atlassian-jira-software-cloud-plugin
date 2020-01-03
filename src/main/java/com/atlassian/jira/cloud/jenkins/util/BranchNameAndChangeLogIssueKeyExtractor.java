package com.atlassian.jira.cloud.jenkins.util;


import com.atlassian.jira.cloud.jenkins.common.service.IssueKeyExtractor;
import com.google.common.collect.ImmutableSet;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses the change log from the current build and extracts the issue keys from the commit
 * messages. It also tries to extract from squashed commits.
 */
public final class BranchNameAndChangeLogIssueKeyExtractor implements IssueKeyExtractor {
    private BranchNameIssueKeyExtractor branchNameExtractor = new BranchNameIssueKeyExtractor();
    private ChangeLogIssueKeyExtractor changeLogextractor = new ChangeLogIssueKeyExtractor();

    public Set<String> extractIssueKeys(final WorkflowRun workflowRun) {

        Set<String> issueKeys = branchNameExtractor.extractIssueKeys(workflowRun);
        issueKeys.addAll(changeLogextractor.extractIssueKeys(workflowRun));

        return ImmutableSet.copyOf(
                issueKeys.stream().limit(ISSUE_KEY_MAX_LIMIT).collect(Collectors.toSet()));
    }
}
