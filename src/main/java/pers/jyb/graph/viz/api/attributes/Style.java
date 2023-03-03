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

package pers.jyb.graph.viz.api.attributes;

import java.io.Serializable;
import pers.jyb.graph.util.Asserts;

public class Style<T extends StyleItem> implements Serializable {

  private static final long serialVersionUID = -6890189319983242315L;

  private final T[] styleItems;

  public Style(T... styleItems) {
    Asserts.nullArgument(styleItems, "styleItems");
    Asserts.illegalArgument(
        styleItems.length == 0,
        "styleItems can not be empty"
    );

    for (StyleItem styleItem : styleItems) {
      Asserts.illegalArgument(
          styleItem == null,
          "null members in styleItems"
      );
    }

    this.styleItems = styleItems;
  }

  public T[] getStyleItems() {
    return styleItems;
  }
}
