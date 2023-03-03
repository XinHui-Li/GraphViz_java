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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pers.jyb.graph.viz.api.ColorFormatException;

/**
 * 异常断言。
 *
 * @author jiangyb
 */
public final class Asserts {

  private Asserts() {
  }

  /**
   * 根据断言判断是否抛出异常。
   *
   * @param predicate 判断是否抛异常
   * @param errorMsg  异常信息
   * @throws IllegalArgumentException 非法参数
   */
  public static void illegalArgument(boolean predicate, String errorMsg) {
    if (predicate) {
      throw new IllegalArgumentException(errorMsg);
    }
  }

  /**
   * 根据是否是空，否则根据指定的名称抛出空提示。
   *
   * @param obj  判断对象
   * @param word 空异常字段名称
   * @throws NullPointerException 空对象
   */
  public static void nullArgument(Object obj, String word) {
    Objects.requireNonNull(obj, word + " can not be null");
  }

  /**
   * 颜色格式校验。
   *
   * @param pattern 匹配器
   * @param color   颜色字符串
   * @param msg     错误提示信息
   * @throws ColorFormatException 错误的格式异常
   */
  public static void illegalColorFormat(Pattern pattern, String color, String msg) {
    if (color == null) {
      throw new ColorFormatException(msg);
    }

    if (pattern == null) {
      return;
    }

    Matcher matcher = pattern.matcher(color);

    if (!matcher.find()) {
      throw new ColorFormatException(msg);
    }
  }
}
