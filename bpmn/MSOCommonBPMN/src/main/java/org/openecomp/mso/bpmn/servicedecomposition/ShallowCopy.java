package org.openecomp.mso.bpmn.servicedecomposition;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface ShallowCopy<T> {
	default T shallowCopyId() {
		try {
			T obj = (T) this.getClass().newInstance();
			for (Field field : this.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(Id.class)) {
					String fieldName = Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
					Method setter = this.getClass().getMethod("set" + fieldName, field.getType());
					Method getter = this.getClass().getMethod("get" + fieldName, null);
					setter.invoke(obj, getter.invoke(this, null));
				}
			}
			return obj;
		}catch(Exception e){
			throw new RuntimeException(e);
		}

	}
}
