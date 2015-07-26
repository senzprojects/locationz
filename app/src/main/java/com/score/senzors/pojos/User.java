package com.score.senzors.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Plain object to hold user attributes
 * Need to implement parcelable, since Sensor object using User
 * objects(Sensor is a parcelable object)
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class User implements Parcelable {
    String id;
    String phoneNo;
    String username;
    String password;

    public User(String id, String phoneNo, String password) {
        this.id = id;
        this.phoneNo = phoneNo;
        this.password = password;
    }

    /**
     * Use when reconstructing User object from parcel
     * This will be used only by the 'CREATOR'
     * @param in a parcel to read this object
     */
    public User(Parcel in) {
        this.id = in.readString();
        this.phoneNo = in.readString();
        this.username = in.readString();
        this.password = in.readString();
    }

    /**
     * Define the kind of object that you gonna parcel,
     * You can use hashCode() here
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Actual object serialization happens here, Write object content
     * to parcel one by one, reading should be done according to this write order
     * @param dest parcel
     * @param flags Additional flags about how the object should be written
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(phoneNo);
        dest.writeString(username);
        dest.writeString(password);
    }

    /**
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays
     *
     * If you don’t do that, Android framework will through exception
     * Parcelable protocol requires a Parcelable.Creator object called CREATOR
     */
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User) {
            User toCompare = (User) obj;
            return (this.phoneNo.equalsIgnoreCase(toCompare.getPhoneNo()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.getPhoneNo()).hashCode();
    }

}
