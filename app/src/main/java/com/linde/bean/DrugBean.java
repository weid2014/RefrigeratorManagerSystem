package com.linde.bean;

import java.util.Objects;

public class DrugBean {

    private String drugName;//药品名称
    private String drugNo;//药品编号
    private String wareHousingTime;//入库时间
    private String outHousingTime;//出库时间
    private String drugSN;//药品序号
    private int drugStatus;//药品出入库状态  0-入库，1-出库

    public DrugBean(String drugName, String drugNo, String wareHousingTime, String outHousingTime, String drugSN, int drugStatus) {
        this.drugName = drugName;
        this.drugNo = drugNo;
        this.wareHousingTime = wareHousingTime;
        this.outHousingTime = outHousingTime;
        this.drugSN = drugSN;
        this.drugStatus = drugStatus;
    }

    public String getOutHousingTime() {
        return outHousingTime;
    }

    public void setOutHousingTime(String outHousingTime) {
        this.outHousingTime = outHousingTime;
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

    public int getDrugStatus() {
        return drugStatus;
    }

    public void setDrugStatus(int drugStatus) {
        this.drugStatus = drugStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrugBean drugBean = (DrugBean) o;
        return drugStatus == drugBean.drugStatus && Objects.equals(drugName, drugBean.drugName) && Objects.equals(drugNo, drugBean.drugNo) && Objects.equals(wareHousingTime, drugBean.wareHousingTime) && Objects.equals(outHousingTime, drugBean.outHousingTime) && Objects.equals(drugSN, drugBean.drugSN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(drugName, drugNo, wareHousingTime, outHousingTime, drugSN, drugStatus);
    }
}
