package xlk.paperless.standard.util;

import org.ini4j.Config;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import xlk.paperless.standard.data.Constant;

/**
 * @author xlk
 * @date 2020/3/9
 * @desc
 */
public class IniUtil {
    private static IniUtil instance;
    private final Ini ini;
    private File file;
    public static File iniFile = new File(Constant.INI_FILE_PATH);


    private IniUtil() {
        ini = new Ini();
        Config config = new Config();
        //不允许出现重复的部分和选项
        config.setMultiSection(false);
        config.setMultiOption(false);
        ini.setConfig(config);
    }

    public static IniUtil getInstance() {
        if (instance == null) {
            instance = new IniUtil();
        }
        return instance;
    }

    //加载文件
    public boolean loadFile(File filePath) {
        try {
            ini.load(new FileReader(filePath));
            this.file = filePath;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String get(String sectionName, String optionName) {
        if (ini == null || file == null) return null;
        return ini.get(sectionName, optionName);
    }

    public void put(String sectionName, String optionName, Object value) {
        if (ini != null && file != null) {
            ini.put(sectionName, optionName, value);
        }
    }

    public void store() {
        if (file != null && ini != null) {
            try {
                ini.store(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
