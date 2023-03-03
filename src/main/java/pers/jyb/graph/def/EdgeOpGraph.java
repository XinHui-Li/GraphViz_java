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

import java.util.function.Consumer;

/**
 * 以边对象{@code Edge}为单位操作图，相比与以顶点为单位操作的图{@code VertexOperator}， {@code
 * EdgeOperator}能携带更多边的属性信息。如：边权重，边类型等等。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 */
public interface EdgeOpGraph<V, E extends BaseEdge<V, E>>
    extends BaseGraph<V> {

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
  EdgeOpGraph<V, E> copy();

  /**
   * 添加一条边。允许使用同一边对象反复添加以达到平行边的效果，不过这些边在{@link #edges()}和{@link #forEachEdges(Consumer)}只迭代一次。
   *
   * @param e 添加的边
   * @throws NullPointerException 空的边
   */
  void addEdge(E e);

  /**
   * 移除一条边
   *
   * @param e 需要移除的边
   * @return 是否移除成功
   */
  boolean removeEdge(E e);

  /**
   * 返回顶点的所有边
   *
   * @param v 需要获取的顶点
   * @return 所有相邻边对象
   */
  Iterable<E> adjacent(Object v);

  /**
   * 返回图的所有边，任何以同一边对象创建的平行边在结果中只会存在一次。
   *
   * @return 图的所有边
   */
  Iterable<E> edges();

  /**
   * 消费所有边，平行边和无向图的边会被多次消费。
   *
   * @param consumer 边消费者
   */
  void forEachEdges(Consumer<E> consumer);
}
