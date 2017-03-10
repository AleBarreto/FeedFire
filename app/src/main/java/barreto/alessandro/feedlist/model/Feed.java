package barreto.alessandro.feedlist.model;

/**
 * Created by Alessandro on 04/03/2017.
 */

public class Feed {

    String photoAvatar;
    String name;
    Long time;
    String text;
    String photoFeed;
    String idUser;
    String idFeed;

    public Feed() {
    }


    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdFeed() {
        return idFeed;
    }

    public void setIdFeed(String idFeed) {
        this.idFeed = idFeed;
    }

    public String getPhotoAvatar() {
        return photoAvatar;
    }

    public void setPhotoAvatar(String photoAvatar) {
        this.photoAvatar = photoAvatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPhotoFeed() {
        return photoFeed;
    }

    public void setPhotoFeed(String photoFeed) {
        this.photoFeed = photoFeed;
    }
}
