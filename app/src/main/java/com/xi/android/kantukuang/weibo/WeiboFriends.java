package com.xi.android.kantukuang.weibo;

import com.google.api.client.util.Key;

import java.util.List;

public class WeiboFriends {
    @Key("users")
    public List<WeiboUserAccount> users;
    @Key("next_cursor")
    public int nextCursor;
}
