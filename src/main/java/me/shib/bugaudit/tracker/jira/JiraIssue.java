package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.commons.BugAuditContent;
import me.shib.bugaudit.tracker.BatComment;
import me.shib.bugaudit.tracker.BatIssue;
import me.shib.bugaudit.tracker.BatPriority;
import me.shib.bugaudit.tracker.BatUser;
import me.shib.java.lib.jiraclient.Comment;
import me.shib.java.lib.jiraclient.Issue;
import me.shib.java.lib.jiraclient.JiraException;
import me.shib.java.lib.jiraclient.User;

import java.util.ArrayList;
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
    public void refresh() {
        try {
            issue.refresh();
        } catch (JiraException e) {
            e.printStackTrace();
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
    public String getTitle() {
        return issue.getSummary();
    }

    @Override
    public String getDescription() {
        return issue.getDescription();
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
    protected BatUser getReporter() {
        return new JiraUser(issue.getReporter());
    }

    @Override
    protected BatUser getAssignee() {
        return new JiraUser(issue.getAssignee());
    }

    @Override
    protected List<BatUser> getSubscribers() {
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
    public List<BatComment> getComments() {
        List<BatComment> comments = new ArrayList<>();
        for (Comment comment : issue.getComments()) {
            comments.add(new JiraComment(comment));
        }
        return comments;
    }

    @Override
    public BatComment addComment(BugAuditContent comment) {
        try {
            return new JiraComment(issue.addComment(comment.getJiraContent()));
        } catch (JiraException e) {
            e.printStackTrace();
            return null;
        }
    }
}
