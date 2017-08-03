package cn.com.lcase.app.model;

/**
 * Created by admin on 2016/10/21.
 */
public class Item {
    int ImgId;
    String name;
    String fenZu;
    boolean isChose;

    public int getImgId() {
        return ImgId;
    }

    public void setImgId(int imgId) {
        ImgId = imgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFenZu() {
        return fenZu;
    }

    public void setFenZu(String fenZu) {
        this.fenZu = fenZu;
    }

    public boolean isChose() {
        return isChose;
    }

    public void setChose(boolean chose) {
        isChose = chose;
    }

}
