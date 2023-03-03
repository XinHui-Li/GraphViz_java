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
 * 无向图
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public interface Graph<V> extends BaseGraph<V> {

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
  Graph<V> copy();

  /**
   * 顶点操作的无向图
   *
   * @param <V> 顶点类型
   */
  interface VertexGraph<V> extends Graph<V>, VertexOpGraph<V> {

    /**
     * 克隆图的副本。
     *
     * @return 图的副本
     */
    VertexGraph<V> copy();
  }

  /**
   * 边操作的无向图
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  interface EdgeGraph<V, E extends Edge<V, E>> extends Graph<V>, EdgeOpGraph<V, E> {

    /**
     * 克隆图的副本。
     *
     * @return 图的副本
     */
    EdgeGraph<V, E> copy();
  }
}