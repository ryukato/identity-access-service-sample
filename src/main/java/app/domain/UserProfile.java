package app.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class UserProfile {

    private String firstName;

    private String lastName;

    private String middleName;

    private String nickName;

    private String mobilePhoneNo;

    private String birthDate;

    private String country;

    private String locale;

    private String language;

    private String timezone;

    @JsonDeserialize(using = GenderDeserializer.class)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private boolean isRequiredVerifyEmail = false;

    private boolean isVerifiedEmail = false;

    private boolean isRequiredVerifyMobileNo = false;

    private boolean isVerifiedMobileNo = false;

    private Address address;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobilePhoneNo() {
        return mobilePhoneNo;
    }

    public void setMobilePhoneNo(String mobilePhoneNo) {
        this.mobilePhoneNo = mobilePhoneNo;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public boolean isRequiredVerifyEmail() {
        return isRequiredVerifyEmail;
    }

    public void setRequiredVerifyEmail(boolean requiredVerifyEmail) {
        isRequiredVerifyEmail = requiredVerifyEmail;
    }

    public boolean isVerifiedEmail() {
        return isVerifiedEmail;
    }

    public void setVerifiedEmail(boolean verifiedEmail) {
        isVerifiedEmail = verifiedEmail;
    }

    public boolean isRequiredVerifyMobileNo() {
        return isRequiredVerifyMobileNo;
    }

    public void setRequiredVerifyMobileNo(boolean requiredVerifyMobileNo) {
        isRequiredVerifyMobileNo = requiredVerifyMobileNo;
    }

    public boolean isVerifiedMobileNo() {
        return isVerifiedMobileNo;
    }

    public void setVerifiedMobileNo(boolean verifiedMobileNo) {
        isVerifiedMobileNo = verifiedMobileNo;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", mobilePhoneNo='" + mobilePhoneNo + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", country='" + country + '\'' +
                ", locale='" + locale + '\'' +
                ", language='" + language + '\'' +
                ", timezone='" + timezone + '\'' +
                ", gender=" + gender +
                ", isRequiredVerifyEmail=" + isRequiredVerifyEmail +
                ", isVerifiedEmail=" + isVerifiedEmail +
                ", isRequiredVerifyMobileNo=" + isRequiredVerifyMobileNo +
                ", isVerifiedMobileNo=" + isVerifiedMobileNo +
                ", address=" + address +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserProfile profile = (UserProfile) o;

        if (isRequiredVerifyEmail != profile.isRequiredVerifyEmail) return false;
        if (isVerifiedEmail != profile.isVerifiedEmail) return false;
        if (isRequiredVerifyMobileNo != profile.isRequiredVerifyMobileNo) return false;
        if (isVerifiedMobileNo != profile.isVerifiedMobileNo) return false;
        if (firstName != null ? !firstName.equals(profile.firstName) : profile.firstName != null) return false;
        if (lastName != null ? !lastName.equals(profile.lastName) : profile.lastName != null) return false;
        if (middleName != null ? !middleName.equals(profile.middleName) : profile.middleName != null) return false;
        if (nickName != null ? !nickName.equals(profile.nickName) : profile.nickName != null) return false;
        if (mobilePhoneNo != null ? !mobilePhoneNo.equals(profile.mobilePhoneNo) : profile.mobilePhoneNo != null)
            return false;
        if (birthDate != null ? !birthDate.equals(profile.birthDate) : profile.birthDate != null) return false;
        if (country != null ? !country.equals(profile.country) : profile.country != null) return false;
        if (locale != null ? !locale.equals(profile.locale) : profile.locale != null) return false;
        if (language != null ? !language.equals(profile.language) : profile.language != null) return false;
        if (timezone != null ? !timezone.equals(profile.timezone) : profile.timezone != null) return false;
        if (gender != profile.gender) return false;
        return address != null ? address.equals(profile.address) : profile.address == null;

    }

    @Override
    public int hashCode() {
        int result = firstName != null ? firstName.hashCode() : 0;
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (nickName != null ? nickName.hashCode() : 0);
        result = 31 * result + (mobilePhoneNo != null ? mobilePhoneNo.hashCode() : 0);
        result = 31 * result + (birthDate != null ? birthDate.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (timezone != null ? timezone.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (isRequiredVerifyEmail ? 1 : 0);
        result = 31 * result + (isVerifiedEmail ? 1 : 0);
        result = 31 * result + (isRequiredVerifyMobileNo ? 1 : 0);
        result = 31 * result + (isVerifiedMobileNo ? 1 : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }
}
