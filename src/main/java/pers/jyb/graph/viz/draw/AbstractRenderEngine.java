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

package pers.jyb.graph.viz.draw;

import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.attributes.Layout;
import pers.jyb.graph.viz.layout.LayoutEngine;

public abstract class AbstractRenderEngine implements RenderEngine {

  @Override
  public GraphResource render(Graphviz graphviz)
      throws ExecuteException {
    Asserts.nullArgument(graphviz, "graphviz");

    Layout layout = graphviz.graphAttrs().getLayout();

    layout = layout == null ? Layout.DOT : layout;

    try {
      LayoutEngine layoutEngine = layout.getLayoutEngine();
      return render0(layoutEngine.layout(graphviz, this));
    } catch (Exception e) {
      throw new ExecuteException("Layout engine execute error: ", e);
    }
  }

  protected abstract GraphResource render0(DrawGraph drawGraph) throws ExecuteException;
}
