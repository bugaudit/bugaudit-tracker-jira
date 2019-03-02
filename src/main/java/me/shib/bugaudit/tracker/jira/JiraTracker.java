package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.commons.BugAuditContent;
import me.shib.bugaudit.tracker.BATracker;
import me.shib.bugaudit.tracker.BatIssue;
import me.shib.bugaudit.tracker.BatIssueFactory;
import me.shib.bugaudit.tracker.BatSearchQuery;
import me.shib.java.lib.jiraclient.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JiraTracker extends BATracker {

    private JiraClient client;

    public JiraTracker(Connection connection, Map<String, Integer> priorityMap) {
        super(connection, priorityMap);
        BasicCredentials credentials = new BasicCredentials(connection.getUsername(), connection.getPassword());
        try {
            this.client = new JiraClient(connection.getEndpoint(), credentials);
        } catch (JiraException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected BugAuditContent.Type getContentType() {
        return BugAuditContent.Type.Jira;
    }

    private String cleanUsername(String username) {
        if (username.contains("@")) {
            return username.split("@")[0];
        }
        return username;
    }

    protected Integer getPriorityNumber(String priorityName) {
        return super.getPriorityNumber(priorityName);
    }

    @Override
    protected BatIssue createIssue(BatIssueFactory creator) {
        try {
            Issue.FluentCreate fluentCreate = client.createIssue(creator.getProject(), creator.getIssueType());
            fluentCreate.field(Field.SUMMARY, creator.getTitle());
            fluentCreate.field(Field.DESCRIPTION, creator.getDescription().getJiraContent());
            if (creator.getAssignee() != null) {
                fluentCreate.field(Field.ASSIGNEE, cleanUsername(creator.getAssignee()));
            }
            if (creator.getPriority() != null) {
                fluentCreate.field(Field.PRIORITY, getPriorityName(creator.getPriority()));
            }
            if (creator.getLabels().size() > 0) {
                fluentCreate.field(Field.LABELS, creator.getLabels());
            }
            if (creator.getCustomFields() != null) {
                for (String key : creator.getCustomFields().keySet()) {
                    fluentCreate.field(key, Field.valueById(creator.getCustomFields().get(key)));
                }
            }
            Issue issue = fluentCreate.execute();
            if (creator.getSubscribers() != null && creator.getSubscribers().size() > 0) {
                for (String watcher : creator.getSubscribers()) {
                    issue.addWatcher(cleanUsername(watcher));
                }
            }
            return new JiraIssue(this, issue);
        } catch (JiraException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected BatIssue updateIssue(BatIssue batIssue, BatIssueFactory updater) {
        JiraIssue jiraIssue = (JiraIssue) batIssue;
        try {
            Issue issue = jiraIssue.getIssue();
            Issue.FluentUpdate fluentUpdate = issue.update();
            if (null != updater.getIssueType()) {
                fluentUpdate.field(Field.ISSUE_TYPE, updater.getIssueType());
            }
            if (null != updater.getTitle()) {
                fluentUpdate.field(Field.SUMMARY, updater.getTitle());
            }
            if (null != updater.getDescription()) {
                fluentUpdate.field(Field.DESCRIPTION, updater.getDescription());
            }
            if (null != updater.getAssignee()) {
                fluentUpdate.field(Field.ASSIGNEE, updater.getAssignee());
            }
            if (null != updater.getStatus()) {
                fluentUpdate.field(Field.STATUS, updater.getStatus());
            }
            if (null != updater.getPriority()) {
                fluentUpdate.field(Field.PRIORITY, getPriorityName(updater.getPriority()));
            }
            if (updater.getLabels().size() > 0) {
                fluentUpdate.field(Field.LABELS, updater.getLabels());
            }
            if (null != updater.getCustomFields()) {
                for (String key : updater.getCustomFields().keySet()) {
                    fluentUpdate.field(key, updater.getCustomFields().get(key));
                }
            }
            fluentUpdate.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jiraIssue;
    }

    @Override
    protected List<BatIssue> searchBatIssues(String projectKey, BatSearchQuery query, int count) {
        List<BatIssue> batIssues = new ArrayList<>();
        StringBuilder jql = new StringBuilder();
        jql.append("project = ").append(projectKey);
        for (BatSearchQuery.BatQueryItem queryItem : query.getQueryItems()) {
            for (String value : queryItem.getValues()) {
                jql.append(" AND ");
                switch (queryItem.getCondition()) {
                    case status:
                        jql.append("status");
                        break;
                    case label:
                        jql.append("labels");
                        break;
                    case type:
                        jql.append("issuetype");
                }
                if (queryItem.getOperator() == BatSearchQuery.Operator.equals) {
                    jql.append(" = ");
                } else if (queryItem.getOperator() == BatSearchQuery.Operator.not) {
                    jql.append(" != ");
                }
                jql.append("\"").append(value).append("\"");
            }
        }
        try {
            List<Issue> issues = client.searchIssues(jql.toString()).issues;
            for (Issue issue : issues) {
                batIssues.add(new JiraIssue(this, issue));
            }
        } catch (JiraException e) {
            e.printStackTrace();
        }
        return batIssues;
    }
}
