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

package pers.jyb.graph.def;

/**
 * 无法接受的图。可能是由于图的某些特殊属性无法接受，比如环。
 *
 * @author jiangyb
 */
public class IllegalGraphException extends RuntimeException {

  private static final long serialVersionUID = -2111900049212557801L;

  public IllegalGraphException() {
    super();
  }

  public IllegalGraphException(String message) {
    super(message);
  }

  public IllegalGraphException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalGraphException(Throwable cause) {
    super(cause);
  }
}
