package com.linde.bean;

public class DrugBean {

    private String drugName;//药品名称
    private String drugNo;//药品编号
    private String wareHousingTime;//入库时间
    private String drugSN;//药品序号

    public DrugBean(String drugName, String drugNo, String wareHousingTime, String drugSN) {
        this.drugName = drugName;
        this.drugNo = drugNo;
        this.wareHousingTime = wareHousingTime;
        this.drugSN = drugSN;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDrugNo() {
        return drugNo;
    }

    public void setDrugNo(String drugNo) {
        this.drugNo = drugNo;
    }

    public String getWareHousingTime() {
        return wareHousingTime;
    }

    public void setWareHousingTime(String wareHousingTime) {
        this.wareHousingTime = wareHousingTime;
    }

    public String getDrugSN() {
        return drugSN;
    }

    public void setDrugSN(String drugSN) {
        this.drugSN = drugSN;
    }
}
