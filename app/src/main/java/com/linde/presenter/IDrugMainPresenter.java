package com.linde.presenter;

import com.linde.bean.DrugBean;

import java.util.List;

public interface IDrugMainPresenter {
    /**
     * 提示框
     */
    void showDiaLog();

    /**
     *
     */
    void showPop();

    void showPopSerialPortTest();

    List<DrugBean> getDrugBeanList();

    String getUserName();
}
