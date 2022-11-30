package com.wuba.easyaop.cfg;

import java.util.Set;

/**
 * Created by wswenyue on 2021/12/17.
 */
public class InsertItem extends BaseItem {
    public String insertEnterClass;
    public String insertEnterMethodName;
    public Set<String> targetEnterMethodList;


    public String insertExitClass;
    public String insertExitMethodName;
    public Set<String> targetExitMethodList;
}
