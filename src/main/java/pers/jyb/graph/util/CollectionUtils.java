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

import java.util.Collection;

/**
 * 集合相关操作工具类。
 *
 * @author jiangyb
 */
public final class CollectionUtils {

  private CollectionUtils() {
  }

  /**
   * 判断集合是否是空值或者空集合。
   *
   * @param collection 集合
   * @return true - 空 false - 非空
   */
  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * 判断集合不是空值或者空集合。
   *
   * @param collection 集合
   * @return true - 非空 false - 空
   */
  public static boolean isNotEmpty(Collection<?> collection) {
    return !isEmpty(collection);
  }
}
