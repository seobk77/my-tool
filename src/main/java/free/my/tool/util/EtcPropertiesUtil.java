package free.my.tool.util;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EtcPropertiesUtil {
	private static final String PROPERTY_FILE_PATH ="etc.properties";
	private static Properties property = new Properties();
	
	public static Map<String, String> readAllProperty() {
		Map<String, String> propertyMap = new HashMap<>();
		
		try(FileInputStream fis = new FileInputStream(PROPERTY_FILE_PATH)) {
			property.load(fis);
			Set<Object> keySet = property.keySet();
			
			keySet.forEach(key -> {
				propertyMap.put((String)key, (String)property.get((String)key));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return propertyMap;
	}
	
	public static String readLookAndFeel() {
		String lookAndFeel = null;
		
		try(FileInputStream fis = new FileInputStream(PROPERTY_FILE_PATH)) {
			property.load(fis);
			lookAndFeel = (String) property.get("look.and.feel");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return lookAndFeel;
	}
	
	public static void writeLookAndFeel(String strLookAndFeel) {
		try(FileOutputStream fos = new FileOutputStream(PROPERTY_FILE_PATH)) {
			property.setProperty("look.and.feel", strLookAndFeel);
			property.store(fos, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readCurrentConfigFileName() {
		String configFileInfo = null;
		
		try(FileInputStream fis = new FileInputStream(PROPERTY_FILE_PATH)) {
			property.load(fis);
			configFileInfo = (String) property.get("current.config.file.info");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return configFileInfo;
	}
	
	public static void writeCurrentConfigFileName(String fileName) {
		try(FileOutputStream fos = new FileOutputStream(PROPERTY_FILE_PATH)) {
			property.setProperty("current.config.file.info", fileName);
			property.store(fos, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readLastFramInfo() {
		String info = null;
		
		try(FileInputStream fis = new FileInputStream(PROPERTY_FILE_PATH)) {
			property.load(fis);
			info = (String) property.get("last.frame.info");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	public static void writeLastFramInfo(String info) {
		try(FileOutputStream fos = new FileOutputStream(PROPERTY_FILE_PATH)) {
			property.setProperty("last.frame.info", info);
			property.store(fos, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readPassKey() {
		String folderInfo = null;
		
		try(FileInputStream fis = new FileInputStream(PROPERTY_FILE_PATH)) {
			property.load(fis);
			folderInfo = (String) property.get("pass.key");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return folderInfo;
	}
}
