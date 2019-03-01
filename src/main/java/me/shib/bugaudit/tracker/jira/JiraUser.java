package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.tracker.BatUser;
import me.shib.java.lib.jiraclient.User;

public class JiraUser implements BatUser {

    private User user;

    JiraUser(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getDisplayName();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }
}
