package com.wuba.easyaop.cfg;

import java.util.Set;

/**
 * Created by wswenyue on 2021/12/17.
 */
public class BaseItem {
    public boolean enabled = true;
    /**
     * 黑名单-class
     */
    public Set<String> skipClazz;
    /**
     * 白名单-class；
     * 注意：如果配置了白名单，就不在处理黑名单（黑名单失效）
     */
    public Set<String> onlyClazz;
}
