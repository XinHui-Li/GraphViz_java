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

import java.util.Objects;
import pers.jyb.graph.viz.api.Node;

public interface NodeEditor<B extends Brush> extends Editor<NodeDrawProp, B> {

  @Override
  default boolean edit(NodeDrawProp nodeDrawProp, B brush) {
    Objects.requireNonNull(nodeDrawProp);

    return edit(nodeDrawProp.getNode(), brush);
  }

  default boolean edit(Node b, B brush) {
    return true;
  }
}
