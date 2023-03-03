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

package pers.jyb.graph.viz.api;

import pers.jyb.graph.viz.api.attributes.Color;

/**
 * 颜色格式异常。
 *
 * @author jiangyb
 * @see Color
 */
public class ColorFormatException extends RuntimeException {

  private static final long serialVersionUID = 1033876844829922354L;

  /**
   * 构造一个没有详细消息的{@code ColorFormatException}
   */
  public ColorFormatException() {
  }

  /**
   * 构造一个有详细消息的{@code ColorFormatException}
   *
   * @param message 详细信息
   */
  public ColorFormatException(String message) {
    super(message);
  }
}
