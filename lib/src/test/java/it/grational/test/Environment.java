package it.grational.test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Environment {
	private final Map<String, String> variables = new HashMap<>();

	public Environment() {
	}

	public Environment(Map<?, ?> variables) {
		Objects.requireNonNull(variables).forEach((key, value) ->
			this.variables.put(String.valueOf(key), String.valueOf(value))
		);
	}

	public void insert() {
		mutableEnv().putAll(variables);
	}

	public void insert(String key, String value) {
		variables.put(key, value);
		mutableEnv().put(key, value);
	}

	public void remove(String key) {
		mutableEnv().remove(key);
	}

	public void clean() {
		variables.keySet().forEach(this::remove);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> mutableEnv() {
		Map<String, String> environment = System.getenv();

		try {
			Field field = environment.getClass().getDeclaredField("m");
			field.setAccessible(true);
			return (Map<String, String>) field.get(environment);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Cannot access the process environment", exception);
		}
	}
}
