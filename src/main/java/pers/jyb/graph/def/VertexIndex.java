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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>顶点索引，邻接数组会使用索引定位顶点。当顶点是伴随邻接数组一起序列化的时候，反序列化的时候，
 * 顶点索引只会记录顶点在此邻接数组当中的索引位置。其他的索引记录丢失。
 *
 * @author jiangyb
 * @see AdjVertexGraph
 * @see AdjEdgeGraph
 */
public class VertexIndex implements Serializable {

  private static final long serialVersionUID = -826073470335347686L;

  /**
   * 顶点索引记录，当前索引在不同图中的索引位置
   */
  private transient volatile Map<GraphRef, Integer> graphIndex;

  public VertexIndex() {
  }

  Map<GraphRef, Integer> getGraphIndex() {
    if (graphIndex == null) {
      synchronized (this) {
        if (graphIndex == null) {
          graphIndex = new ConcurrentHashMap<>(1);
        }
      }
    }

    return graphIndex;
  }

  /**
   * 返回图的索引
   *
   * @param graphRef 图引用
   * @return 索引位置
   */
  Integer index(GraphRef graphRef) {
    return graphIndex.get(graphRef);
  }

  /**
   * 如果图只有索引使用，直接GC。否则当索引用于多个图当中，可能图没有引用了，但是顶点被应用于 另一个图，无引用的图是无法GC。 并且使用{@code
   * GraphRef}而不是使用原来的图对象可以避免{@link BaseGraph}子类重写hashCode之后的引用变动。
   */
  static class GraphRef extends WeakReference<BaseGraph<?>> {

    public GraphRef(BaseGraph<?> referent) {
      super(referent);
    }
  }
}
