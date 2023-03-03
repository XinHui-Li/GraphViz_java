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
 * 通常来说，图的表示有三种：邻接矩阵、边的数组、邻接表数组。<br>
 *
 * <p><strong>邻接矩阵</strong><br>
 * 使用一个V乘V的布尔矩阵。当顶点v和顶点w之间有相连接的边时，定义v行w列的元素值 为true，否则为false。这种表示方式有两个致命问题：V平方的所需空间；无法表示平 行边。
 *
 * <p><strong>边的数组</strong><br>
 * 创建Edge类，使用Edge数组表示图。其中每个Edge包含两个顶点的信息。这种表示方式 很简洁，但是如果要获取某个顶点的所有相邻顶的时候，需要检查所有边。
 *
 * <p><strong>邻接表数组</strong><br>
 * 使用一个以顶点为索引的列表数组，其中的每个元素都是和该顶点相邻的顶点列表。
 *
 * <p>图的表示不是{@code AbstractBaseGraph}所关注的，{@code AbstractBaseGraph}关注的
 * 是图顶点的一些基础属性操作，如：最大度数{@link #maxDegree}等等。在具体明确 图的表示形式之后，有些操作可以优化，如：顶点度数{@link #degree}，这在本次操
 * 作当中是以<i>O(n)</i>的复杂度进行的。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 * @see BaseGraph
 */
public abstract class AbstractBaseGraph<V> implements BaseGraph<V> {

  protected AbstractBaseGraph() {
  }

  /**
   * 顶点最大的度
   *
   * @return 图中最大的度数
   */
  @Override
  public int maxDegree() {
    int maxDegree = 0;
    for (V value : this) {
      int d;
      if ((d = degree(value)) > maxDegree) {
        maxDegree = d;
      }
    }
    return maxDegree;
  }

  /**
   * 获取平均度数
   *
   * @return 所有顶点的平均度数
   */
  @Override
  public double averageDegree() {
    return vertexNum() != 0 ? (double) 2 * edgeNum() / vertexNum() : 0;
  }

  /**
   * 顶点图
   *
   * @param <V> 顶点类型
   */
  public abstract static class AbstractVertexOpBase<V> extends AbstractBaseGraph<V>
      implements VertexOpGraph<V> {

    @Override
    public String toString() {
      StringBuilder print = new StringBuilder("vertices " + vertexNum() + ", edges:" + edgeNum() + "\n");
      for (V v : this) {
        print.append("[").append(v).append("] ");
        for (V n : adjacent(v)) {
          print.append(v).append(":").append(n).append(" ");
        }
        print.append("\n");
      }
      return print.toString();
    }
  }

  /**
   * 顶点无向图
   *
   * @param <V> 顶点类型
   */
  public abstract static class AbstractVertexOpGraph<V> extends AbstractVertexOpBase<V>
      implements Graph.VertexGraph<V> {

    /**
     * 返回某个顶点的度
     *
     * @param v 顶点
     * @return 顶点度数
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (V ignored : adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }

  /**
   * 顶点有向图
   *
   * @param <V> 顶点类型
   */
  public abstract static class AbstractVertexOpDigraph<V> extends AbstractVertexOpBase<V>
      implements Digraph.VertexDigraph<V> {

    /**
     * 返回某个顶点的度
     *
     * @param v 顶点
     * @return 顶点度数
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (V ignored : adjacent(v)) {
        degree++;
      }
      // 反转有向图
      VertexDigraph<V> digraph = reverse();
      for (V ignored : digraph.adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }

  /**
   * 边图
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  public abstract static class AbstractEdgeOpBase<V, E extends BaseEdge<V, E>>
      extends AbstractBaseGraph<V> implements EdgeOpGraph<V, E> {

    @Override
    public String toString() {
      StringBuilder print = new StringBuilder("vertices " + vertexNum() + ", edges:" + edgeNum() + "\n");
      for (V v : this) {
        print.append("[").append(v).append("]\n");
        for (E e : adjacent(v)) {
          print.append(e).append("\n");
        }
        print.append("\n");
      }
      return print.toString();
    }
  }

  /**
   * 边无向图
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  public abstract static class AbstractEdgeOpGraph<V, E extends Edge<V, E>>
      extends AbstractEdgeOpBase<V, E> implements Graph.EdgeGraph<V, E> {

    /**
     * 返回某个顶点的度
     *
     * @param v 顶点
     * @return 顶点度数
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (E ignored : adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }


  /**
   * 边有向图
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  public abstract static class AbstractEdgeOpDiraph<V, E extends DirectedEdge<V, E>>
      extends AbstractEdgeOpBase<V, E> implements Digraph.EdgeDigraph<V, E> {

    /**
     * 返回某个顶点的度
     *
     * @param v 顶点
     * @return 顶点度数
     */
    @Override
    public int degree(V v) {
      int degree = 0;
      for (E ignored : adjacent(v)) {
        degree++;
      }
      // 反转有向图
      EdgeDigraph<V, E> digraph = reverse();
      for (E ignored : digraph.adjacent(v)) {
        degree++;
      }
      return degree;
    }
  }
}