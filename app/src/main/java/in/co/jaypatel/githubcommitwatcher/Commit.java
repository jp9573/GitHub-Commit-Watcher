package in.co.jaypatel.githubcommitwatcher;

import java.util.Date;

public class Commit {
    private String name, email, message, userName, avatarUrl;
    Date date;

    public Commit(String name, Date date, String email, String message, String userName, String avatarUrl) {
        this.name = name;
        this.date = date;
        this.email = email;
        this.message = message;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
