package org.elasticsearch.index.config;

import com.yoho.pinyin.utils.MatchDict;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

import java.io.File;
import java.util.Set;

public class PinyinElasticConfigurator {
    public static ESLogger logger = Loggers.getLogger("pinyin-analyzer");
    private static boolean loaded = false;
    public static Set<String> filter;
    public static Environment environment;

    public static void init(Settings indexSettings, Settings settings) {
    	if (isLoaded()) {
			return;
		}
    	environment  =new Environment(indexSettings);
        try{
        	preheat();
        	logger.info("拼音分词器多音字词库预热完毕，可以使用!");
        }catch(Exception e){
        	logger.error("拼音分词器多音字词库预热失败，请检查路径");
        }
        setLoaded(true);
    }

    public static void preheat() {
        File file = new File(environment.configFile(), "pinyin/multitoneDict.txt");
        MatchDict.init(file);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean loaded) {
        PinyinElasticConfigurator.loaded = loaded;
    }

}
