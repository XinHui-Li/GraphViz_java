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

package pers.jyb.graph.viz.api;

import java.io.Serializable;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.Rank;

public class Subgraph extends GraphContainer implements Serializable {

  private static final long serialVersionUID = -6058438891682789815L;

  private Rank rank;

  private Subgraph() {
  }

  public Rank getRank() {
    return rank;
  }

  /*------------------------------------------ static ---------------------------------------*/

  public static SubgraphBuilder builder() {
    return new SubgraphBuilder();
  }

  public static class SubgraphBuilder extends GraphContainerBuilder<Subgraph, SubgraphBuilder> {

    private SubgraphBuilder() {
    }

    public SubgraphBuilder rank(Rank rank) {
      Asserts.nullArgument(rank, "rank");
      initContainer().rank = rank;
      return this;
    }

    @Override
    protected SubgraphBuilder self() {
      return this;
    }

    @Override
    protected Subgraph newContainer() {
      return new Subgraph();
    }

    @Override
    protected Subgraph copy() {
      Subgraph sub = new Subgraph();
      sub.rank = initContainer().rank;
      return sub;
    }
  }

  public static class IntegrationSubgraphBuilder<
      G extends GraphContainer,
      B extends GraphContainerBuilder<G, B>>
      extends GraphContainerBuilder<Subgraph, IntegrationSubgraphBuilder<G, B>> {

    private final B container;

    IntegrationSubgraphBuilder(B container) {
      Asserts.nullArgument(container, "container");
      this.container = container;
    }

    public IntegrationSubgraphBuilder<G, B> rank(Rank rank) {
      Asserts.nullArgument(rank, "rank");
      initContainer().rank = rank;
      return this;
    }

    public B endSub() {
      Subgraph subgraph = build();
      container.subgraph(subgraph);
      return container;
    }

    @Override
    protected IntegrationSubgraphBuilder<G, B> self() {
      return this;
    }

    @Override
    protected Subgraph newContainer() {
      return new Subgraph();
    }

    @Override
    protected Subgraph copy() {
      Subgraph sub = new Subgraph();
      sub.rank = initContainer().rank;
      return sub;
    }
  }
}
