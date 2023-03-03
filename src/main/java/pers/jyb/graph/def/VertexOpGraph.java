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

import pers.jyb.graph.op.SourcePaths;

/**
 * 以顶点为单位操作图。这种操作手段无法保存边的属性，如果仅仅关注顶点之间的连接 关系的时候，可以选择{@code VertexOperator}。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public interface VertexOpGraph<V> extends BaseGraph<V> {

  /**
   * 顶点数量
   *
   * @return 顶点个数
   */
  int vertexNum();

  /**
   * 边的数量，包括环
   *
   * @return 边的个数
   */
  int edgeNum();

  /**
   * 添加顶点
   *
   * @param v 顶点
   * @return 是否成功
   * @throws NullPointerException 空的顶点
   */
  boolean add(V v);

  /**
   * 移除图中的顶点
   *
   * @param v 需要移除的顶点
   * @return true - 顶点存在并且移除成功 false - 顶点不存在
   */
  boolean remove(Object v);

  /**
   * 返回某个顶点的度
   *
   * @param v 顶点
   * @return 顶点度数
   */
  int degree(V v);

  /**
   * 顶点最大的度
   *
   * @return 图中最大的度数
   */
  int maxDegree();

  /**
   * 获取平均度数
   *
   * @return 所有顶点的平均度数
   */
  double averageDegree();

  /**
   * 计算自环的个数
   *
   * @return 图中自环数量
   */
  int numberOfLoops();

  /**
   * 返回顶点数组
   *
   * @return 顶点数组
   */
  V[] toArray();

  /**
   * 克隆图的副本。
   *
   * @return 图的副本
   */
  VertexOpGraph<V> copy();

  /**
   * 向图中添加一条边v-w
   *
   * @param v 源顶点
   * @param w 目标顶点
   */
  void addEdge(V v, V w);

  /**
   * 移除图中一条边v-w
   *
   * @param v 边的端点
   * @param w 边的端点
   * @return true - 边存在并且移除成功 false - 边不存在
   */
  boolean removeEdge(Object v, Object w);

  /**
   * <p>返回相邻的所有顶点的可遍历对象，当不明确图的具体实现方式的时候，需要计算顶点的度数
   * {@link #degree(Object)}，或者计算图中顶点的最大度数的时候，{@link #maxDegree()}， 可以使用本方法遍历所有相邻节点计算度数。
   *
   * <p>使用此方式获取度数的时间复杂度是<i>O(n)</i>。对于一些特定的实现，比如邻接数组
   * {@link AdjVertexGraph}，它会在每次添加边的时候，使用一个内部属性记住当前顶点的度，这种 时候应该直接返回这个值，来达到<i>O(1)</i>复杂度的获取。
   *
   * <p>此外，对于一些需要遍历图的操作{@link SourcePaths}，会采用此方法来达到深度
   * ({@link SourcePaths#depth})或者广度({@link SourcePaths#breadth})的遍历目的。
   *
   * @param v 需要获取的顶点
   * @return 相邻顶点的遍历对象
   */
  Iterable<V> adjacent(Object v);
}
