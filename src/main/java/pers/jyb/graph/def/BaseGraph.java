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

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>在许多应用中，由相连的顶点所表示的模型起到了关键的作用。这些顶点之间的连接衍生了一
 * 系列的问题：沿着这些连接是否能从一个顶点到达另一个顶点？有多少个顶点和指定的顶点相连？
 * 两个顶点之间最短的连接是哪一条？{@code BaseGraph}不是处理以上问题的方式，而是给以上
 * 问题提供一个基础的操作API，以便支撑上述的一些问题的解决。
 *
 * <p>对于图模型来说，顶点和边几乎是代表全部的内容，但是基于顶点和边还衍生出了一些其他的
 * 概念：
 * <pre>
 *     1.边的无向和有向
 *     对于一条边Edge(V,W)来说，如果同时代表了V指向W，W指向V，那么这条边就是无向的；如果
 *     仅仅代表V指向W，那么这条边是有向的。
 *
 *     2.加权边
 *     如果边具有权重值，那么称这条边为加权边。权重值在不同的图模型当中代表不同的涵义，如
 *     果连接代表的是地图上不同地点之间的路线，路线的距离就可以作为边的权重值。或者说如果
 *     图代表的是某种任务调度的过程，那么权重值就可以代表调度的执行条件，也可以代表任务调
 *     度的优先级。
 *
 *     3.顶点的度
 *     与顶点相连的所有的边的数量。在有向图当中，顶点的度 = 顶点出度 + 顶点入度。
 *
 *     4.自环
 *     一条连接一个顶点和其自身的边。
 *
 *     5.平行边
 *     连接同一对顶点和其自身的边。
 *
 *     6.简单路径
 *     一条没有重复顶点的路径。
 *
 *     7.简单环
 *     一条（除了起点和终点必须相同之外）不含有重复顶点和边的环。
 * </pre>
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public interface BaseGraph<V> extends Iterable<V> {

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
   * 返回顶点自环数量。
   *
   * @param v 顶点
   * @return 顶点自环数量
   */
  int selfLoops(V v);

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
  BaseGraph<V> copy();

  /**
   * clear the current graph.
   */
  void clear();

  /**
   * 返回一个以该{@code BaseGraph}为源的连续{@code Stream}。
   *
   * @return 一个以该{@code BaseGraph}为源的连续{@code Stream}
   */
  default Stream<V> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}