package com.example.bean;

import javax.lang.model.element.VariableElement;

/**
 * Created by Administrator on 2018/8/18.
 */

public class VariableInfo {

    private int resid;
    private VariableElement variableElement;

    public int getResid() {
        return resid;
    }

    public void setResid(int resid) {
        this.resid = resid;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }

    public void setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
    }
}
