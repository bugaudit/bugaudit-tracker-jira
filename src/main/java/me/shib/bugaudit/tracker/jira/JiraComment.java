package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.tracker.BatComment;
import me.shib.java.lib.jiraclient.Comment;

import java.util.Date;

public class JiraComment implements BatComment {

    private Comment comment;

    JiraComment(Comment comment) {
        this.comment = comment;
    }

    @Override
    public String getBody() {
        return comment.getBody();
    }

    @Override
    public Date getUpdated() {
        return comment.getUpdatedDate();
    }
}
