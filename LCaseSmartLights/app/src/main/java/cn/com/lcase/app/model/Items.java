package cn.com.lcase.app.model;

import java.util.List;

/**
 * Created by admin on 2016/10/21.
 */
public class Items {
    String Title;
    boolean isCheck;
    List<Item> mItem;

    public List<Item> getmItem() {
        return mItem;
    }

    public void setmItem(List<Item> mItem) {
        this.mItem = mItem;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
