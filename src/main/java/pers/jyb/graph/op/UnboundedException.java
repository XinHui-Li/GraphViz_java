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

package pers.jyb.graph.op;

/**
 * 线性规划是无界的
 *
 * @author jiangyb
 * @see Simplex
 */
public class UnboundedException extends Exception {

  private static final long serialVersionUID = -7703108589695504331L;

  public UnboundedException() {
    super();
  }

  public UnboundedException(String message) {
    super(message);
  }

  public UnboundedException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnboundedException(Throwable cause) {
    super(cause);
  }
}
