package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.commons.BugAuditContent;
import me.shib.bugaudit.commons.BugAuditException;
import me.shib.bugaudit.tracker.BatIssue;
import me.shib.bugaudit.tracker.BatIssueFactory;
import me.shib.bugaudit.tracker.BatSearchQuery;
import me.shib.bugaudit.tracker.BugAuditTracker;
import me.shib.java.lib.jiraclient.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JiraTracker extends BugAuditTracker {

    private JiraClient client;

    public JiraTracker(Connection connection, Map<String, Integer> priorityMap) throws BugAuditException {
        super(connection, priorityMap);
        BasicCredentials credentials = new BasicCredentials(connection.getUsername(), connection.getPassword());
        try {
            this.client = new JiraClient(connection.getEndpoint(), credentials);
        } catch (JiraException e) {
            e.printStackTrace();
            throw new BugAuditException(e.getMessage());
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
    public BatIssue createIssue(BatIssueFactory creator) throws BugAuditException {
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
                    try {
                        fluentCreate.field(key, Field.valueById((String) creator.getCustomFields().get(key)));
                    } catch (Exception e) {
                        fluentCreate.field(key, creator.getCustomFields().get(key));
                    }
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
            throw new BugAuditException(e.getMessage());
        }
    }

    private boolean transitionIssue(Issue issue, String status) throws JiraException {
        List<Transition> transitions = issue.getTransitions();
        for (Transition transition : transitions) {
            if (transition.getToStatus().getName().equalsIgnoreCase(status)) {
                issue.transition().execute(transition.getName());
                return true;
            }
        }
        return false;
    }

    @Override
    public BatIssue updateIssue(BatIssue batIssue, BatIssueFactory updater) throws BugAuditException {
        JiraIssue jiraIssue = (JiraIssue) batIssue;
        boolean fluentUpdatable = false;
        try {
            Issue issue = jiraIssue.getIssue();
            Issue.FluentUpdate fluentUpdate = issue.update();
            if (null != updater.getIssueType()) {
                fluentUpdate.field(Field.ISSUE_TYPE, updater.getIssueType());
                fluentUpdatable = true;
            }
            if (null != updater.getTitle()) {
                fluentUpdate.field(Field.SUMMARY, updater.getTitle());
                fluentUpdatable = true;
            }
            if (null != updater.getDescription()) {
                fluentUpdate.field(Field.DESCRIPTION, updater.getDescription().getJiraContent());
                fluentUpdatable = true;
            }
            if (null != updater.getAssignee()) {
                fluentUpdate.field(Field.ASSIGNEE, updater.getAssignee());
                fluentUpdatable = true;
            }
            if (null != updater.getStatus()) {
                transitionIssue(issue, updater.getStatus());
            }
            if (null != updater.getPriority()) {
                fluentUpdate.field(Field.PRIORITY, getPriorityName(updater.getPriority()));
                fluentUpdatable = true;
            }
            if (updater.getLabels().size() > 0) {
                fluentUpdate.field(Field.LABELS, updater.getLabels());
                fluentUpdatable = true;
            }
            if (null != updater.getCustomFields()) {
                for (String key : updater.getCustomFields().keySet()) {
                    fluentUpdate.field(key, updater.getCustomFields().get(key));
                    fluentUpdatable = true;
                }
            }
            if (fluentUpdatable) {
                fluentUpdate.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BugAuditException(e.getMessage());
        }
        return jiraIssue;
    }

    private String getJqlForBatQuery(String projectKey, BatSearchQuery query) {
        StringBuilder jql = new StringBuilder();
        jql.append("project = ").append(projectKey);
        for (BatSearchQuery.BatQueryItem queryItem : query.getQueryItems()) {
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
            if (queryItem.getOperator() == BatSearchQuery.Operator.matching) {
                jql.append(" in ");
            } else if (queryItem.getOperator() == BatSearchQuery.Operator.not_matching) {
                jql.append(" not in ");
            }
            jql.append("(");
            jql.append("\"").append(queryItem.getValues().get(0)).append("\"");
            for (int i = 1; i < queryItem.getValues().size(); i++) {
                jql.append(", ").append("\"").append(queryItem.getValues().get(i)).append("\"");
            }
            jql.append(")");
        }
        return jql.toString();
    }

    @Override
    public List<BatIssue> searchBatIssues(String projectKey, BatSearchQuery query, int count) throws BugAuditException {
        List<BatIssue> batIssues = new ArrayList<>();
        try {
            List<Issue> issues = client.searchIssues(getJqlForBatQuery(projectKey, query)).issues;
            for (Issue issue : issues) {
                batIssues.add(new JiraIssue(this, issue));
            }
        } catch (JiraException e) {
            e.printStackTrace();
            throw new BugAuditException(e.getMessage());
        }
        return batIssues;
    }

    JiraClient getClient() {
        return client;
    }
}
