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

/**
 * Abstract directed edge object.
 *
 * @param <V> vertex type
 * @param <E> subclass type
 * @author jiangyb
 */
public abstract class AbstractDirectedEdge<V, E extends AbstractDirectedEdge<V, E>>
    extends AbstractEdge<V, E> implements DirectedEdge<V, E>, Serializable {

  private static final long serialVersionUID = 7419657082828781230L;

  protected AbstractDirectedEdge(V left, V right) {
    super(left, right);
  }

  protected AbstractDirectedEdge(V from, V to, double weight) {
    super(from, to, weight);
  }

  @Override
  public V from() {
    return left;
  }

  @Override
  public V to() {
    return right;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && obj instanceof AbstractDirectedEdge;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + AbstractDirectedEdge.class.hashCode();
  }

  @Override
  public String toString() {
    return "DefaultDirectedEdge{" +
        "from=" + left +
        ", to=" + right +
        ", weight=" + weight +
        "} " + super.toString();
  }
}
