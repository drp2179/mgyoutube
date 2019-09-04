package com.djpedersen.mgyoutube.behavior_tests.cucumber;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {

	private static final Map<String, Object> context = new HashMap<>();

	public static Object get(final String key) {
		return context.get(key);
	}

	public static void put(final String key, final Object value) {
		context.put(key, value);
	}

	public static <T> T get(final String key, final Class<T> classOfT) {
		final Object object = context.get(key);
		return classOfT.cast(object);
	}

	// public static void putIfNotExist(final String key, final Object value) {
	// if (!context.containsKey(key)) {
	// context.put(key, value);
	// }
	// }

	public static void clear() {
		context.clear();
	}
}
