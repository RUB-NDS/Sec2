package de.adesso.mobile.android.sec2.model;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias ("noticelist")
public class NoticeList {

    @XStreamImplicit (itemFieldName = "notice")
    public List<NoticeListItem> noticeListItem = new ArrayList<NoticeListItem>();;

    public NoticeList() {}

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NoticeList(");
        builder.append("noticeListItem: " + noticeListItem.toString());
        builder.append(")");
        return builder.toString();
    }

}
