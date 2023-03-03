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

import pers.jyb.graph.def.BaseGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 访问图中顶点的时候，通过一个数组标记顶点是否被访问过，并且如果需要的话，记录访问的路径到edgeTo当中。
 * 这是图的各种算法中的一个基础步骤，因为只有标记了遍历的顶点，这样才能知道哪些顶点已经被访问，这样程序
 * 才能在一个合适的位置退出。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public class AccessMarked<V> {

  /**
   * 顶点是否被标记
   */
  protected boolean[] marked;

  /**
   * 记录顶点路径
   */
  protected Object[] edgeTo;

  /**
   * 顶点索引记录
   */
  private Map<V, Integer> vertexIndexs;

  private AccessMarked() {
  }

  public AccessMarked(BaseGraph<V> graph) {
    this(graph, null);
  }

  public AccessMarked(BaseGraph<V> graph, Consumer<V> consumer) {
    Objects.requireNonNull(graph, "graph cannot be null");
    int vertexNum = graph.vertexNum();

    if (vertexNum == 0) {
      throw new IllegalArgumentException("graph cannot be empty");
    }

    marked = new boolean[vertexNum];
    edgeTo = new Object[vertexNum];
    vertexIndexs = new HashMap<>(graph.vertexNum());
    int i = 0;

    boolean consumerNotNull = consumer != null;

    for (V v : graph) {
      if (consumerNotNull) {
        consumer.accept(v);
      }

      vertexIndexs.put(v, i++);
    }
  }

  /**
   * 返回并且检索顶点v的索引范围。
   *
   * @param v 顶点
   * @return 顶点索引
   * @throws NullPointerException   空顶点
   * @throws NoSuchElementException 不存在的顶点
   */
  protected int checkAndReturnIndex(V v) {
    Objects.requireNonNull(v, "Vertex can not be null");
    Integer index = vertexIndexs.get(v);
    if (index == null || index < 0 || index > marked.length
        || index > edgeTo.length) {
      throw new NoSuchElementException("Graph don't have this vertex " + v);
    }
    return index;
  }

  /**
   * 安全的返回值，如果出错，返回默认值{@code defaultValue}。
   *
   * @param <T>          任务返回值
   * @param task         执行的任务
   * @param defaultValue 出错的默认值
   * @return 任务返回值或默认值
   */
  protected <T> T safeReturn(Supplier<? extends T> task, T defaultValue) {
    try {
      return task.get();
    } catch (Throwable e) {
      return defaultValue;
    }
  }

  /**
   * 顶点是否被标记。
   *
   * @param v 顶点
   * @return 是否被标记
   * @throws NullPointerException   空顶点
   * @throws NoSuchElementException 没有顶点v
   */
  protected boolean isMarked(V v) {
    return v != null && marked[checkAndReturnIndex(v)];
  }
}
