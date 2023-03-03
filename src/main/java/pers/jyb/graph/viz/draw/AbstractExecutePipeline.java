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

import java.util.List;
import java.util.function.Function;
import pers.jyb.graph.util.Asserts;

public abstract class AbstractExecutePipeline<
    I,
    B extends Brush,
    T extends Editor<I, B>,
    S extends AbstractExecutePipeline<I, B, T, S>> {

  protected final List<T> editors;

  protected final DrawGraph drawGraph;

  protected AbstractExecutePipeline(
      List<T> editors,
      DrawGraph drawGraph
  ) {
    Asserts.nullArgument(editors, "editors");
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.editors = editors;
    this.drawGraph = drawGraph;
  }

  public void trigger(Function<I, B> brushFactory) {
    Asserts.nullArgument(brushFactory, "brushFactory");

    for (I item : consumerItems()) {
      B brush = brushFactory.apply(item);

      Asserts.illegalArgument(brush == null, "BrushFactory cannot create null brush!");

      for (int i = 0; i < editors.size(); i++) {
        T editor = editors.get(i);
        if (!editor.edit(item, brush)) {
          break;
        }
      }
    }
  }

  protected abstract Iterable<I> consumerItems();
}
