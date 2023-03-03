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
import pers.jyb.graph.def.BaseGraph;
import pers.jyb.graph.def.EdgeOpGraph;
import pers.jyb.graph.def.VertexOpGraph;

import java.lang.reflect.Array;

/**
 * {@link SourcePaths}的加强版本。能够获取任意两个顶点之间的路径，如果不关注所有顶点之间的连接情况， 建议还是用{@link
 * SourcePaths}，因为检索所有顶点的连接情况将付出额外的性能代价。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 */
public abstract class Paths<V> extends AccessMarked<V> implements Path<V> {

  /**
   * 记录各顶点的{@code SourcePath}对象
   */
  protected SourcePath<V>[] paths;

  /**
   * @param graph 图
   * @throws NullPointerException 空图
   */
  Paths(BaseGraph<V> graph) {
    super(graph);
  }

  /**
   * 顶点v和顶点w之间是否存在路径
   *
   * @param v 源顶点
   * @param w 目标顶点
   * @return 两顶点之间是否有路径
   */
  @Override
  public boolean hasPath(V v, V w) {
    return safeReturn(
        () -> paths[checkAndReturnIndex(v)].hasPathTo(w),
        false
    );
  }

  /**
   * 与顶点v连通的顶点个数
   *
   * @param v 顶点
   * @return 连通顶点数量
   */
  @Override
  public int count(V v) {
    return safeReturn(
        () -> paths[checkAndReturnIndex(v)].count(),
        0
    );
  }

  /**
   * 返回顶点v到顶点w的路径打印字符串
   *
   * @param v 源顶点
   * @param w 目标顶点
   * @return 路径
   */
  @Override
  public String printPath(V v, V w) {
    return safeReturn(
        () -> paths[checkAndReturnIndex(v)].printPath(w),
        null
    );
  }

  /**
   * 深度遍历发现顶点图中各顶点之间的路径情况
   *
   * @param <V>   顶点类型
   * @param graph 顶点图
   * @return 顶点之间的路径
   * @throws NullPointerException 空图
   */
  public static <V> VertexOp<V> depth(VertexOpGraph<V> graph) {
    return new VertexOpPaths<>(graph, 0);
  }

  /**
   * 深度遍历发现边图中各顶点之间的路径情况
   *
   * @param <V>   顶点类型
   * @param <E>   边类型
   * @param graph 边图
   * @return 顶点之间的路径
   * @throws NullPointerException 空图
   */
  public static <V, E extends BaseEdge<V, E>> EdgeOp<V, E> depth(
      EdgeOpGraph<V, E> graph) {
    return new EdgeOpPaths<>(graph, 0);
  }

  /**
   * 广度遍历发现顶点图中各顶点之间的路径情况
   *
   * @param <V>   顶点类型
   * @param graph 顶点图
   * @return 顶点之间的路径
   * @throws NullPointerException 空图
   */
  public static <V> VertexOp<V> breadth(VertexOpGraph<V> graph) {
    return new VertexOpPaths<>(graph, 1);
  }

  /**
   * 广度遍历发现边图中各顶点之间的路径情况
   *
   * @param <V>   顶点类型
   * @param <E>   边类型
   * @param graph 边图
   * @return 顶点之间的路径
   * @throws NullPointerException 空图
   */
  public static <V, E extends BaseEdge<V,E>> EdgeOp<V, E> breadth(
      EdgeOpGraph<V, E> graph) {
    return new EdgeOpPaths<>(graph, 1);
  }

  /**
   * 顶点图路径
   *
   * @param <V> 顶点类型
   */
  private static class VertexOpPaths<V> extends Paths<V>
      implements VertexOp<V> {

    /**
     * @param graph 图
     * @param type  类型 0-深度优先 其他-广度优先
     */
    @SuppressWarnings("unchecked")
    VertexOpPaths(VertexOpGraph<V> graph, int type) {
      super(graph);
      for (V v : graph) {
        SourcePath.VertexOp<V> sourcePath =
            type == 0
                ? SourcePaths.depth(graph, v)
                : SourcePaths.breadth(graph, v);
        if (paths == null) {
          paths = (SourcePath.VertexOp<V>[]) Array.newInstance(
              sourcePath.getClass(),
              graph.vertexNum()
          );
        }
        paths[checkAndReturnIndex(v)] = sourcePath;
      }
    }

    @Override
    public V[] path(V v, V w) {
      return safeReturn(() -> ((SourcePath.VertexOp<V>) paths[checkAndReturnIndex(v)]).path(w),
          null);
    }
  }

  /**
   * 边图路径
   *
   * @param <V> 顶点类型
   * @param <E> 边类型
   */
  private static class EdgeOpPaths<V, E extends BaseEdge<V, E>> extends Paths<V>
      implements EdgeOp<V, E> {

    /**
     * @param graph 图
     * @param type  类型 0-深度优先 其他-广度优先
     * @throws NullPointerException 空图
     */
    @SuppressWarnings("unchecked")
    EdgeOpPaths(EdgeOpGraph<V, E> graph, int type) {
      super(graph);
      for (V v : graph) {
        SourcePath.EdgeOp<V, E> sourcePath =
            type == 0
                ? SourcePaths.depth(graph, v)
                : SourcePaths.breadth(graph, v);
        if (paths == null) {
          paths = (SourcePath.EdgeOp<V, E>[]) Array.newInstance(
              sourcePath.getClass(),
              graph.vertexNum()
          );
        }
        paths[checkAndReturnIndex(v)] = sourcePath;
      }
    }

    @Override
    public E[] path(V v, V w) {
      return safeReturn(
          () -> ((SourcePath.EdgeOp<V, E>) paths[checkAndReturnIndex(v)]).path(w),
          null
      );
    }
  }
}
