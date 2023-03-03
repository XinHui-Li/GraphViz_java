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
 * 边双向迭代有向图。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 */
public interface EdgeDedigraph<V, E extends DirectedEdge<V, E>>
    extends Dedigraph<V>, Digraph.EdgeDigraph<V, E> {

  /**
   * 顶点数量。
   *
   * @return 顶点个数
   */
  int vertexNum();

  /**
   * 边的数量，包括环。
   *
   * @return 边的个数
   */
  int edgeNum();

  /**
   * 添加顶点。
   *
   * @param v 顶点
   * @return 是否成功
   * @throws NullPointerException 空的顶点
   */
  boolean add(V v);

  /**
   * 移除图中的顶点。
   *
   * @param v 需要移除的顶点
   * @return true - 顶点存在并且移除成功 false - 顶点不存在
   */
  boolean remove(Object v);

  /**
   * 返回某个顶点的度。
   *
   * @param v 顶点
   * @return 顶点度数
   */
  int degree(V v);

  /**
   * 返回入度。
   *
   * @param v 顶点
   * @return 入度
   */
  int inDegree(V v);

  /**
   * 返回出度。
   *
   * @param v 顶点
   * @return 出度
   */
  int outDegree(V v);

  /**
   * 顶点最大的度。
   *
   * @return 图中最大的度数
   */
  int maxDegree();

  /**
   * 获取平均度数。
   *
   * @return 所有顶点的平均度数
   */
  double averageDegree();

  /**
   * 计算自环的个数。
   *
   * @return 图中自环数量
   */
  int numberOfLoops();

  /**
   * 返回顶点数组。
   *
   * @return 顶点数组
   */
  V[] toArray();

  /**
   * 添加一条边。允许使用同一边对象反复添加以达到平行边的效果，不过这些边在{@link #edges()}和{@link #forEachEdges(Consumer)}只迭代一次。
   *
   * @param e 添加的边
   * @throws NullPointerException 空的边
   */
  void addEdge(E e);

  /**
   * 移除一条边。
   *
   * @param e 需要移除的边
   * @return 是否移除成功
   */
  boolean removeEdge(E e);

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

  /**
   * 反转有向图中的边。
   *
   * @param e 需要反转的边
   * @return 反转后的边，反转失败返回null
   */
  E reverseEdge(E e);

  /**
   * 克隆图的副本。
   *
   * @return 图的副本
   */
  EdgeDedigraph<V, E> copy();

  /**
   * 反转有向图。有向图中，一般顶点只会记录自己指向的顶点，对于某个顶点本身来说， 指向自己的顶点通常是未知的。通过反转有向图，这样就可以找出所有指向该顶点的顶点。
   *
   * @return 反转有向图
   */
  EdgeDedigraph<V, E> reverse();

  /**
   * 返回顶点的所有相邻边，迭代对象包含了顶点的出入边。
   *
   * @param v 需要获取的顶点
   * @return 所有相邻边对象
   */
  Iterable<E> adjacent(Object v);

  /**
   * 返回相邻的入边的可遍历对象。
   *
   * @param v 需要获取的顶点
   * @return 入边的遍历对象
   */
  Iterable<E> inAdjacent(Object v);

  /**
   * 返回相邻的出度边的可遍历对象。
   *
   * @param v 需要获取的顶点
   * @return 出边的遍历对象
   */
  Iterable<E> outAdjacent(Object v);
}
