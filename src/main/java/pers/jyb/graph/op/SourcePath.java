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
 * 记录一个初始顶点作为源起点，判断图中其他顶点到源顶点的路径
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public interface SourcePath<V> {

  /**
   * 顶点v是否可达
   *
   * @param v 顶点
   * @return 是否可达
   */
  boolean hasPathTo(V v);

  /**
   * 与起点source连通的顶点个数
   *
   * @return 连通顶点数量
   */
  int count();

  /**
   * 返回路径的打印字符串
   *
   * @param v 顶点
   * @return 路径
   */
  String printPath(V v);

  /**
   * 顶点图路径
   *
   * @param <V> 顶点类型
   */
  interface VertexOp<V> extends SourcePath<V> {

    /**
     * 如果存在，返回<tt>source</tt>到<tt>v</tt>的路径，否则返回null
     *
     * @param v 顶点
     * @return 路径，顺序及为数组索引
     */
    V[] path(V v);
  }

  /**
   * 边图路径
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  interface EdgeOp<V, E extends BaseEdge<V, E>> extends SourcePath<V> {

    /**
     * 如果存在，返回<tt>source</tt>到<tt>v</tt>的路径，否则返回null
     *
     * @param v 顶点
     * @return 路径，顺序及为数组索引
     */
    E[] path(V v);
  }
}
