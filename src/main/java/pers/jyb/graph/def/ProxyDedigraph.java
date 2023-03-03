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
import java.util.Iterator;
import java.util.Objects;

/**
 * 抽象的双向有向图，采用两个有向图进行代理实现。
 *
 * @param <V> 顶点类型
 * @param <D> 代理图类型
 * @author jiangyb
 */
abstract class ProxyDedigraph<V, D extends Digraph<V>, R extends Digraph<V>> extends
    AbstractBaseGraph<V> implements Dedigraph<V>, Serializable {

  private static final long serialVersionUID = 5269319670434302102L;

  protected final D digraph;

  protected final R reDigraph;

  protected ProxyDedigraph(D digraph, R reDigraph) {
    Objects.requireNonNull(digraph);
    Objects.requireNonNull(reDigraph);

    this.digraph = digraph;
    this.reDigraph = reDigraph;
  }

  @Override
  public int vertexNum() {
    return digraph.vertexNum();
  }

  @Override
  public int edgeNum() {
    return digraph.edgeNum();
  }

  @Override
  public boolean add(V v) {
    return digraph.add(v) && reDigraph.add(v);
  }

  @Override
  public boolean remove(Object v) {
    return digraph.remove(v) && reDigraph.remove(v);
  }

  @Override
  public int degree(V v) {
    return digraph.degree(v);
  }

  @Override
  public int inDegree(V v) {
    int count = 0;
    for (Object ignore : inIte(v)) {
      count++;
    }

    return count;
  }

  @Override
  public int outDegree(V v) {
    int count = 0;
    for (Object ignore : outIte(v)) {
      count++;
    }

    return count;
  }

  @Override
  public int numberOfLoops() {
    return digraph.numberOfLoops();
  }

  @Override
  public V[] toArray() {
    return digraph.toArray();
  }

  @Override
  public void clear() {
    digraph.clear();
    reDigraph.clear();
  }

  @Override
  public Iterator<V> iterator() {
    return new ProxyIterator<>(digraph);
  }

  @Override
  public int hashCode() {
    return digraph.hashCode() + reDigraph.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }

    if (!(obj instanceof ProxyDedigraph)) {
      return false;
    }

    ProxyDedigraph<?, ?, ?> dedigraph = (ProxyDedigraph<?, ?, ?>) obj;
    return Objects.equals(dedigraph.digraph, digraph)
        && Objects.equals(dedigraph.reDigraph, reDigraph);
  }

  @Override
  public String toString() {
    return digraph.toString();
  }

  protected abstract <T> Iterable<T> inIte(Object v);

  protected abstract <T> Iterable<T> outIte(Object v);

  /*------------------------------------------- Iterable or Iterator Object -------------------------------------------*/

  private static class ProxyIterator<V, D extends Digraph<V>> implements Iterator<V> {

    private final Iterator<V> iterator;

    private ProxyIterator(D digraph) {
      Objects.requireNonNull(digraph);
      this.iterator = digraph.iterator();
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public V next() {
      return iterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("ProxyDedigraph's vertex cannot be deleted by iterator");
    }
  }
}
