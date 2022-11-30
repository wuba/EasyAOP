package com.wuba.easyaop.cfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.commons.text.lookup.StringLookupFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

/**
 * Created by wswenyue on 2021/12/17.
 */
public final class EasyConfig {
    public String version = "1.0.0";
    public boolean logEnabled = false;
    public int logLevel = 1;
    /**
     * 是否需要进行AOP语法检查
     */
    public boolean needAopCheck = false;
    /**
     * 是否需要进行dex打包检查
     */
    public boolean needDexCheck = false;
    /**
     * 黑名单-class
     */
    public Set<String> skipClazz;
    /**
     * 白名单-class；
     * 注意：如果配置了白名单，就不在处理黑名单（黑名单失效）
     */
    public Set<String> onlyClazz;
    /**
     * 黑名单-jar
     */
    public Set<String> skipJars;
    /**
     * 白名单-jar
     * 注意：如果配置了白名单，就不在处理黑名单（黑名单失效）
     */
    public Set<String> onlyJars;
    public Map<String, ProxyItem> proxyItemsCfg;
    public Map<String, EmptyItem> emptyItemsCfg;
    public Map<String, InsertItem> insertItemsCfg;


    public static EasyConfig buildFromYaml(File cfgFile, Map<String, ?> gradleProp) {
        String source = null;
        try {
            source = new String(Files.readAllBytes(cfgFile.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (source == null) {
            System.out.println("read source config is empty!!!");
            return null;
        }

        try {
            final StringLookup systemLookup = StringLookupFactory.INSTANCE.environmentVariableStringLookup();
            String content = new StringSubstitutor(key -> {
                if (key == null) {
                    return null;
                }
                String value = null;
                if (gradleProp != null && gradleProp.containsKey(key)) {
                    value = gradleProp.get(key).toString();
                }
                if (value == null) {
                    value = systemLookup.lookup(key);
                }
                System.out.printf("StringLookup lookup found ==> %s=%s\n", key, value);
                return value;
            }).replace(source);

            if (content == null) {
                System.out.println("format config is empty!!!");
                return null;
            }

            System.out.println("========format config====begin======");
            System.out.println(content);
            System.out.println("========format config====end========");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return (EasyConfig) mapper.readValue(content, EasyConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveToYaml(File cfgFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        mapper.writeValue(cfgFile, this);
    }
}
