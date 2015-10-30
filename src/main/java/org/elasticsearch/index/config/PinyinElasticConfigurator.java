package org.elasticsearch.index.config;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

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
        	//logger.info("ansj分词器预热完毕，可以使用!");
        }catch(Exception e){
        	//logger.error("ansj分词预热失败，请检查路径");
        }
        setLoaded(true);
    }

    public static void preheat() {

    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void setLoaded(boolean loaded) {
        PinyinElasticConfigurator.loaded = loaded;
    }

}
