package barreto.alessandro.feedlist.model;

/**
 * Created by Alessandro on 04/03/2017.
 */

public class User {

    private String name;
    private String email;
    private String uId;
    private String photoUrl;

    public User() {
    }

    public User(String name, String email, String uId, String photoUrl) {
        this.name = name;
        this.email = email;
        this.uId = uId;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
