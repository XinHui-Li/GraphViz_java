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

import pers.jyb.graph.def.BaseEdge;

/**
 * 途中任意两顶点之间路径
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public interface Path<V> {

  /**
   * 顶点v和顶点w之间是否存在路径
   *
   * @param v 源顶点
   * @param w 目标顶点
   * @return 两顶点之间是否有路径
   */
  boolean hasPath(V v, V w);

  /**
   * 与顶点v连通的顶点个数
   *
   * @param v 顶点
   * @return 连通顶点数量
   */
  int count(V v);

  /**
   * 返回顶点v到顶点w的路径打印字符串
   *
   * @param v 源顶点
   * @param w 目标顶点
   * @return 路径
   */
  String printPath(V v, V w);

  /**
   * 顶点图中任意两个顶点之间路径
   *
   * @param <V> 顶点类型
   */
  interface VertexOp<V> extends Path<V> {

    /**
     * 如果存在，返回<tt>source</tt>到<tt>v</tt>的路径，否则返回null
     *
     * @param v 源顶点
     * @param w 目标顶点
     * @return 路径，顺序及为数组索引
     */
    V[] path(V v, V w);
  }

  /**
   * 边图中任意两个顶点之间路径
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  interface EdgeOp<V, E extends BaseEdge<V, E>> extends Path<V> {

    /**
     * 如果存在，返回<tt>source</tt>到<tt>v</tt>的路径，否则返回null
     *
     * @param v 源顶点
     * @param w 目标顶点
     * @return 路径，顺序及为数组索引
     */
    E[] path(V v, V w);
  }
}
