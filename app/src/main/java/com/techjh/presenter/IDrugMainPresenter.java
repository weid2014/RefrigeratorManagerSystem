package com.techjh.presenter;

import com.techjh.bean.DrugBean;

import java.util.List;

public interface IDrugMainPresenter {
    /**
     * 提示框
     */
    void showDiaLog();

    void showPopOut();

    /**
     *
     */
    void showPop();

    void showPopSerialPortTest();

    List<DrugBean> getDrugBeanList();
    void setDrugBeanList(List<DrugBean> drugBeanList);

    String getUserName();
    void setUserName(String userName);
}
