package free.my.tool.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.io.IOException;

public class JsonUtil {
	private static final ObjectMapper mapper = new ObjectMapper();
	//private static final ObjectMapper mapperDefaultType = new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	private static final ObjectMapper mapperDefaultType = new ObjectMapper();
	
	static {
	    PolymorphicTypeValidator polymorphicTypeValidator = mapperDefaultType.getPolymorphicTypeValidator();
	    mapperDefaultType.activateDefaultTyping(polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
	}

	public static <T> T toObject(JsonNode node, Class<T> tClass) {
		return toObject(node, tClass, false);
	}

	public static <T> T toObject(JsonNode node, Class<T> tClass, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.treeToValue(node, tClass);
			}
			return mapper.treeToValue(node, tClass);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static <T> T toObject(String val, Class<T> tClass) {
		return toObject(val, tClass, false);
	}

	public static <T> T toObject(String val, Class<T> tClass, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.readValue(val, tClass);
			}
			return mapper.readValue(val, tClass);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static <T> T toObject(String val, TypeReference<T> valueTypeRef) {
		return toObject(val, valueTypeRef, false);
	}

	public static <T> T toObject(String val, TypeReference<T> valueTypeRef, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.readValue(val, valueTypeRef);
			}
			return mapper.readValue(val, valueTypeRef);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static JsonNode toNode(Object fromValue) {
		return toNode(fromValue, false);
	}

	public static JsonNode toNode(Object fromValue, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.valueToTree(fromValue);
			}
			return mapper.valueToTree(fromValue);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static JsonNode toNode(String val) {
		return toNode(val, false);
	}

	public static JsonNode toNode(String val, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.readTree(val);
			}
			return mapper.readTree(val);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String toString(Object val) {
		return toString(val, false);
	}

	public static String toString(Object val, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.writeValueAsString(val);
			}
			return mapper.writeValueAsString(val);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static byte[] toBytes(Object val) {
		return toBytes(val, false);
	}

	public static byte[] toBytes(Object val, boolean isDefaultTyping) {
		try {
			if (isDefaultTyping) {
				return mapperDefaultType.writeValueAsBytes(val);
			}
			return mapper.writeValueAsBytes(val);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
