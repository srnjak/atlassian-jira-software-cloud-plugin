package com.atlassian.jira.cloud.jenkins.util;

import com.atlassian.jira.cloud.jenkins.common.service.IssueKeyExtractor;
import com.atlassian.jira.cloud.jenkins.util.BranchNameAndChangeLogIssueKeyExtractor;
import com.google.common.collect.ImmutableList;
import hudson.plugins.git.GitChangeSet;
import hudson.scm.ChangeLogSet;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Set;
import jenkins.plugins.git.GitBranchSCMHead;
import jenkins.plugins.git.GitBranchSCMRevision;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMRevisionAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BranchNameAndChangeLogExtractorTest {

    private IssueKeyExtractor branchNameAndChangeLogExtractor;

    private static final String BRANCH_NAME = "TEST-456-branch-name";

    @Before
    public void setUp() {
        branchNameAndChangeLogExtractor = new BranchNameAndChangeLogIssueKeyExtractor();
    }

    @Test
    public void testExtractIssueKeys_forNoChangeSets() {
        // given
        final WorkflowRun workflowRun = workflowRunWithNoChangeSets();

        // when
        final Set<String> issueKeys = branchNameAndChangeLogExtractor.extractIssueKeys(workflowRun);

        // then
        assertThat(issueKeys).isEmpty();
    }

    @Test
    public void testExtractIssueKeys_forOneChangeLogSetEntry() {
        // given
        final WorkflowRun workflowRun = changeSetWithOneChangeLogSetEntry();

        // when
        final Set<String> issueKeys = branchNameAndChangeLogExtractor.extractIssueKeys(workflowRun);

        // then
        assertThat(issueKeys).containsExactlyInAnyOrder("TEST-123");
    }

    @Test
    public void testExtractIssueKeys_forBranchAndOneChangeSetEntries() {
        // given
        final WorkflowRun workflowRun = changeSetWithBranchAndOneChangeLogSetEntries();

        // when
        final Set<String> issueKeys = branchNameAndChangeLogExtractor.extractIssueKeys(workflowRun);

        // then
        assertThat(issueKeys).containsExactlyInAnyOrder("TEST-456", "TEST-123");
    }

    @Test
    public void testExtractIssueKeys_forBranchAndMultipleChangeSets() {
        // given
        final WorkflowRun workflowRun = workflowRunWithBranchAndMultipleChangeSets();

        // when
        final Set<String> issueKeys = branchNameAndChangeLogExtractor.extractIssueKeys(workflowRun);

        // then
        assertThat(issueKeys).containsExactlyInAnyOrder("TEST-456", "TEST-123", "TEST-789");
    }

    @Test
    public void testExtractIssueKeys_forIssuesAboveLimit() {
        // given
        final WorkflowRun workflowRun = workflowRunWithIssuesAboveLimit();

        // when
        final Set<String> issueKeys = branchNameAndChangeLogExtractor.extractIssueKeys(workflowRun);

        // then
        assertThat(issueKeys).hasSize(100);
    }

    private WorkflowRun workflowRunWithNoChangeSets() {
        final WorkflowRun workflowRun = mock(WorkflowRun.class);

        when(workflowRun.getChangeSets()).thenReturn(Collections.emptyList());
        setEmptyBranchSCM(workflowRun);
        return workflowRun;
    }

    private WorkflowRun changeSetWithOneChangeLogSetEntry() {
        final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        when(entry.getMsg()).thenReturn("TEST-123 Commit message");
        final ChangeLogSet changeLogSet = mock(ChangeLogSet.class);
        when(changeLogSet.getItems()).thenReturn(new Object[] {entry});
        final WorkflowRun workflowRun = mock(WorkflowRun.class);

        when(workflowRun.getChangeSets()).thenReturn(ImmutableList.of(changeLogSet));

        setEmptyBranchSCM(workflowRun);
        return workflowRun;
    }

    private WorkflowRun changeSetWithBranchAndOneChangeLogSetEntries() {
        final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
        when(entry.getMsg()).thenReturn("TEST-123 Commit message");
        final ChangeLogSet changeLogSet = mock(ChangeLogSet.class);
        when(changeLogSet.getItems()).thenReturn(new Object[] {entry});
        final WorkflowRun workflowRun = mock(WorkflowRun.class);

        when(workflowRun.getChangeSets()).thenReturn(ImmutableList.of(changeLogSet));

        setWithBranchSCM(workflowRun);
        return workflowRun;
    }

    private WorkflowRun workflowRunWithBranchAndMultipleChangeSets() {
        final ChangeLogSet.Entry entry1 = mock(ChangeLogSet.Entry.class);
        final ChangeLogSet.Entry entry2 = mock(ChangeLogSet.Entry.class);
        when(entry1.getMsg()).thenReturn("TEST-123 Commit message");
        when(entry2.getMsg()).thenReturn("TEST-789 Commit message");
        final ChangeLogSet changeLogSet1 = mock(ChangeLogSet.class);
        final ChangeLogSet changeLogSet2 = mock(ChangeLogSet.class);
        when(changeLogSet1.getItems()).thenReturn(new Object[] {entry1});
        when(changeLogSet2.getItems()).thenReturn(new Object[] {entry2});
        final WorkflowRun workflowRun = mock(WorkflowRun.class);

        when(workflowRun.getChangeSets())
                .thenReturn(ImmutableList.of(changeLogSet1, changeLogSet2));

        setWithBranchSCM(workflowRun);
        return workflowRun;
    }

    private WorkflowRun workflowRunWithIssuesAboveLimit() {
        int count = 100;
        Object[] changeSetEntries = new Object[count];
        for (int i = 0; i < count; i++) {
            final ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);
            when(entry.getMsg()).thenReturn(String.format("TEST-%d Commit message for %d", i, i));
            changeSetEntries[i] = entry;
        }
        final ChangeLogSet changeLogSet = mock(ChangeLogSet.class);

        when(changeLogSet.getItems()).thenReturn(changeSetEntries);
        final WorkflowRun workflowRun = mock(WorkflowRun.class);

        when(workflowRun.getChangeSets()).thenReturn(ImmutableList.of(changeLogSet));

        setEmptyBranchSCM(workflowRun);
        return workflowRun;
    }

    private void setEmptyBranchSCM(WorkflowRun mockWorkflowRun) {

        final GitBranchSCMHead head = new GitBranchSCMHead("");
        final SCMRevisionAction scmRevisionAction =
                new SCMRevisionAction(new GitSCMSource(""), new GitBranchSCMRevision(head, ""));
        when(mockWorkflowRun.getAction(SCMRevisionAction.class)).thenReturn(scmRevisionAction);
    }

    private void setWithBranchSCM(WorkflowRun mockWorkflowRun) {

        final GitBranchSCMHead head = new GitBranchSCMHead(BRANCH_NAME);
        final SCMRevisionAction scmRevisionAction =
                new SCMRevisionAction(new GitSCMSource(""), new GitBranchSCMRevision(head, ""));
        when(mockWorkflowRun.getAction(SCMRevisionAction.class)).thenReturn(scmRevisionAction);
    }
}
