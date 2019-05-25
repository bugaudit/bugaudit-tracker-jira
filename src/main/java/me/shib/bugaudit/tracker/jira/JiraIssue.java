package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.commons.BugAuditContent;
import me.shib.bugaudit.commons.BugAuditException;
import me.shib.bugaudit.tracker.BatComment;
import me.shib.bugaudit.tracker.BatIssue;
import me.shib.bugaudit.tracker.BatPriority;
import me.shib.bugaudit.tracker.BatUser;
import me.shib.java.lib.jiraclient.Comment;
import me.shib.java.lib.jiraclient.Issue;
import me.shib.java.lib.jiraclient.JiraException;
import me.shib.java.lib.jiraclient.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JiraIssue extends BatIssue {

    private transient Issue issue;
    private transient JiraTracker tracker;

    protected JiraIssue(JiraTracker tracker, Issue issue) {
        super(tracker);
        this.tracker = tracker;
        this.issue = issue;
    }

    @Override
    public void refresh() throws BugAuditException {
        try {
            issue.refresh();
        } catch (JiraException e) {
            e.printStackTrace();
            throw new BugAuditException(e.getMessage());
        }
    }

    Issue getIssue() {
        return issue;
    }

    void setIssue(Issue issue) {
        this.issue = issue;
    }

    @Override
    public String getKey() {
        return issue.getKey();
    }

    @Override
    public String getProjectKey() {
        return issue.getProject().getKey();
    }

    @Override
    public String getTitle() {
        return issue.getSummary();
    }

    @Override
    public String getDescription() {
        return issue.getDescription();
    }

    @Override
    public String getType() {
        return issue.getIssueType().getName();
    }

    @Override
    public String getStatus() {
        return issue.getStatus().getName();
    }

    @Override
    public BatPriority getPriority() {
        return new JiraPriority(issue.getPriority(), tracker);
    }

    @Override
    public Date getCreatedDate() {
        return issue.getCreatedDate();
    }

    @Override
    public Date getUpdatedDate() {
        return issue.getUpdatedDate();
    }

    @Override
    public Date getDueDate() {
        return issue.getDueDate();
    }

    @Override
    public BatUser getReporter() {
        return new JiraUser(issue.getReporter());
    }

    @Override
    public BatUser getAssignee() {
        return new JiraUser(issue.getAssignee());
    }

    @Override
    public List<BatUser> getSubscribers() {
        List<BatUser> subscribers = new ArrayList<>();
        for (User user : issue.getWatches().getWatchers()) {
            subscribers.add(new JiraUser(user));
        }
        return subscribers;
    }

    @Override
    public List<String> getLabels() {
        return issue.getLabels();
    }

    @Override
    public Object getCustomField(String identifier) {
        return issue.getField(identifier);
    }

    @Override
    public List<BatComment> getComments() {
        List<BatComment> comments = new ArrayList<>();
        for (Comment comment : issue.getComments()) {
            comments.add(new JiraComment(comment));
        }
        return comments;
    }

    @Override
    public BatComment addComment(BugAuditContent comment) throws BugAuditException {
        try {
            return new JiraComment(issue.addComment(comment.getJiraContent()));
        } catch (JiraException e) {
            e.printStackTrace();
            throw new BugAuditException(e.getMessage());
        }
    }
}
