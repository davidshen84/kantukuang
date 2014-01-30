package com.xi.android.kantukuang.event;

public class SectionAttachEvent {
    private int mSectionId;
    private String mSectionName;

    public int getSectionId() {
        return mSectionId;
    }

    public SectionAttachEvent setSectionId(int sectionId) {
        mSectionId = sectionId;

        return this;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public SectionAttachEvent setSectionName(String sectionName) {
        mSectionName = sectionName;

        return this;
    }
}
