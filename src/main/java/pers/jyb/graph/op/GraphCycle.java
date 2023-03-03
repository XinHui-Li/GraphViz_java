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

import pers.jyb.graph.def.DirectedEdge;

import java.util.List;
import java.util.Stack;

/**
 * 图环的寻找
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public interface GraphCycle<V> {

  /**
   * 是否含有环
   *
   * @return 图中是否存在环
   */
  boolean hasCycle();

  /**
   * 顶点图的环寻找
   *
   * @param <V> 顶点类型
   */
  interface VertexOpGC<V> extends GraphCycle<V> {

    /**
     * 如果图中存在环，返回发现的第一个环。向量图中的环的顺序等于{@link Stack#pop}的顺序
     *
     * @return 环路径
     */
    Stack<V> cycle();
  }

  /**
   * 边图的环寻找
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  interface EdgeOpGC<V, E extends DirectedEdge<V, E>> extends GraphCycle<V> {

    /**
     * 如果图中存在环，返回发现的第一个环
     *
     * @return 环路径
     */
    Stack<E> cycle();
  }

  /**
   * 顶点图所有环寻找
   *
   * @param <V> 顶点类型
   */
  interface VertexOpAllGC<V> extends GraphCycle<V> {

    /**
     * 如果图中存在环，返回所有环
     *
     * @return 所有环
     */
    List<Stack<V>> cycles();
  }

  /**
   * 边图所有环寻找
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  interface EdgeOpAllGC<V, E extends DirectedEdge<V, E>> extends GraphCycle<V> {

    /**
     * 如果图中存在环，返回所有环
     *
     * @return 环路径
     */
    List<Stack<E>> cycle();
  }
}
