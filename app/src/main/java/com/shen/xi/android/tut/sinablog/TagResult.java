package com.shen.xi.android.tut.sinablog;


import com.google.api.client.util.Key;

import java.util.ArrayList;

public class TagResult {
    @Key
    String qjson;
    @Key
    public int cnt;
    @Key
    public ArrayList<String> list;
    @Key
    public boolean isLastPage;
}
