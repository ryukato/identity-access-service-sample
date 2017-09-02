package app.domain;

import javax.persistence.Embeddable;

@Embeddable
public class LoginCredential {
    private String account;
    private String password;

    //for JPA
    public LoginCredential() {
    }

    public LoginCredential(String account, String password) {
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "LoginCredential{" +
                "account='" + account + '\'' +
                ", password= not visible"+
                "}";

    }
}
