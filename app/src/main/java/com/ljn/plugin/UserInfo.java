package com.ljn.plugin;

/**
 * <pre>
 *     author : created by ljn
 *     e-mail : liujinan@edreamtree.com
 *     time   : 2017/08/07
 *     desc   : 用户信息(用于传给ecg信息)
 *     modify :
 * </pre>
 */

public class UserInfo {
    private String name;//使用用户
    private int male;//性别
    private String email;//邮件(环信的huid)
    private String password;//环信的password
    private long dob;//生日
    private int smoker;
    private float height;
    private float weight;
    private boolean isRegister;//是否是新用户

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMale() {
        return male;
    }

    public void setMale(int male) {
        this.male = male;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getDob() {
        return dob;
    }

    public void setDob(long dob) {
        this.dob = dob;
    }

    public int getSmoker() {
        return smoker;
    }

    public void setSmoker(int smoker) {
        this.smoker = smoker;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setRegister(boolean register) {
        isRegister = register;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", male=" + male +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", dob=" + dob +
                ", smoker=" + smoker +
                ", height=" + height +
                ", weight=" + weight +
                ", isRegister=" + isRegister +
                '}';
    }
}
