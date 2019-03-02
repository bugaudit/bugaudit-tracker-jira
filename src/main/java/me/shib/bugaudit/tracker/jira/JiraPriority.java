package me.shib.bugaudit.tracker.jira;

import me.shib.bugaudit.tracker.BatPriority;
import me.shib.java.lib.jiraclient.Priority;

public class JiraPriority implements BatPriority {

    private Priority priority;
    private int value;

    JiraPriority(Priority priority, JiraTracker tracker) {
        this.priority = priority;
        this.value = tracker.getPriorityNumber(priority.getName());
    }

    @Override
    public String getName() {
        return priority.getName();
    }

    @Override
    public int getValue() {
        return value;
    }
}
