package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.commons.BugAuditContent;
import me.shib.bugaudit.commons.BugAuditException;
import me.shib.bugaudit.tracker.*;
import me.shib.java.lib.jiraclient.JiraClient;

import java.util.List;

public class JiraTracker extends BATracker {

    private JiraClient client;

    public JiraTracker(BatConfig config) throws BugAuditException {
        super(config);
    }

    @Override
    protected BugAuditContent.Type getContentType() {
        return null;
    }

    @Override
    protected BatIssue createIssue(BatIssueFactory creator) {
        return null;
    }

    @Override
    protected BatIssue updateIssue(BatIssue issue, BatIssueFactory updater) {
        return null;
    }

    @Override
    protected List<BatIssue> searchBatIssues(String projectKey, BatSearchQuery query, int count) {
        return null;
    }
}
