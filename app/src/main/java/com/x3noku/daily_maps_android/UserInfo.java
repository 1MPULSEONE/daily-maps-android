package com.x3noku.daily_maps_android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class UserInfo {
    private String nickname;
    private List<String> tasksId;
    private List<String> templatesId;

    UserInfo() {
        this.nickname = "New User";
        this.tasksId = new LinkedList<String>();
        this.templatesId = new ArrayList<String>();
    }

    UserInfo( String nickname ) {
        this.nickname = nickname;
        this.tasksId = new LinkedList<String>();
        this.templatesId = new ArrayList<String>();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return nickname;
    }

    public void setTasksId(List<String> tasksId) {
        this.tasksId = tasksId;
    }
    public List<String> getTasksId() {
        return tasksId;
    }

    public void setTemplatesId(List<String> templatesId) {
        this.templatesId = templatesId;
    }
    public List<String> getTemplatesId() {
        return templatesId;
    }

}
