package free.my.tool.util;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CommandConfigPropertiesUtil {
	
	public static Map<String, String> readAllProperty(String path) {
		Properties property = new Properties();
		Map<String, String> propertyMap = new HashMap<>();
		
		try(FileInputStream fis = new FileInputStream(path)) {
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
}
