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

package pers.jyb.graph.viz.layout;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 判断顶点是否被访问的模板类。
 *
 * @author jiangyb
 * */
public abstract class Mark<T> {

  protected final Set<T> marked;

  protected Mark() {
    this.marked = new HashSet<>();
  }

  protected Mark(int initialCapacity) {
    this.marked = new HashSet<>(initialCapacity);
  }

  protected void mark(T node) {
    Objects.requireNonNull(node);

    marked.add(node);
  }

  protected boolean isMark(T node) {
    return marked.contains(node);
  }

  protected void clear() {
    marked.clear();
  }
}
