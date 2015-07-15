package zhexian.app.smartcall.contact;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.Serializable;

@JsonObject
public class ContactEntity implements Comparable<ContactEntity>, Serializable {
    @JsonField(name = "UserName")
    private String userName;

    @JsonField(name = "Company")
    private String company;

    @JsonField(name = "Department")
    private String department;

    @JsonField(name = "JobTitle")
    private String jobTitle;

    @JsonField(name = "Phone")
    private String phone;

    @JsonField(name = "ShortPhone")
    private String shortPhone;

    @JsonField(name = "AvatarURL")
    private String avatarURL;

    /**
     * 全拼 chenjunjie
     */
    @JsonField
    private String userNamePY;

    /**
     * 简拼 cjj
     */
    @JsonField
    private String userNameHeadPY;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getShortPhone() {
        return shortPhone;
    }

    public void setShortPhone(String shortPhone) {
        this.shortPhone = shortPhone;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public String getUserNamePY() {
        return userNamePY;
    }

    public void setUserNamePY(String userNamePY) {
        this.userNamePY = userNamePY;
    }

    public String getUserNameHeadPY() {
        return userNameHeadPY;
    }

    public void setUserNameHeadPY(String userNameHeadPY) {
        this.userNameHeadPY = userNameHeadPY;
    }

    public char getUserNameStartPY() {
        return userNameHeadPY.charAt(0);
    }

    @Override
    public int compareTo(@NonNull ContactEntity contactEntity) {
        char firstChar = this.getUserNameStartPY();
        char compareChar = contactEntity.getUserNameStartPY();

        if (firstChar > compareChar)
            return 1;
        else if (firstChar == compareChar)
            return 0;
        else
            return -1;
    }

}
