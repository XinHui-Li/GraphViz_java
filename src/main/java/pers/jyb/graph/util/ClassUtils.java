/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pers.jyb.graph.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("all")
public class ClassUtils {

  private ClassUtils() {
  }

  public static Map<String, Object> propValMap(Object obj) throws IllegalAccessException {
    if (obj == null) {
      return Collections.emptyMap();
    }

    Class<?> cls = obj.getClass();

    Field[] fields = cls.getDeclaredFields();
    if (fields.length == 0) {
      return Collections.emptyMap();
    }

    Map<String, Object> map = new HashMap<>(fields.length);
    for (Field field : fields) {
      if (Modifier.isStatic(field.getModifiers())) {
        continue;
      }

      String name = field.getName();
      field.setAccessible(true);
      Object propVal = field.get(obj);
      if (propVal == null) {
        continue;
      }
      field.setAccessible(false);
      map.put(name, propVal);
    }

    return map;
  }
}
