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

import pers.jyb.graph.viz.api.attributes.Color.ColorItem;
import pers.jyb.graph.viz.api.attributes.Color.HSVColor;
import pers.jyb.graph.viz.api.attributes.Color.MultiColor;
import pers.jyb.graph.viz.api.attributes.Color.RgbColor;
import pers.jyb.graph.viz.api.attributes.Color.RgbaColor;
import pers.jyb.graph.util.Asserts;

/**
 * 构建color属性的一系列方法。
 *
 * @author jiangyb
 */
public final class Colors {

  private Colors() {
  }

  public static Color ofRGB(String color) {
    return new RgbColor(color);
  }

  public static Color ofRGBA(String color) {
    return new RgbaColor(color);
  }

  public static Color ofHSV(String color) {
    return new HSVColor(color);
  }

  public static ColorItem item(Color color, float weight) {
    Asserts.nullArgument(color, "color");
    Asserts.illegalArgument(weight < 0 || weight > 1, "weight must between 0 and 1");
    return new ColorItem(color, weight);
  }

  public static MultiColor asList(Color... items) {
    Asserts.nullArgument(items, "colorlist");
    Asserts.illegalArgument(items.length < 2, "The length of colorlist cannot be less than 2");

    ColorItem[] colorItems = new ColorItem[items.length];
    for (int i = 0; i < items.length; i++) {
      Color item = items[i];
      Asserts.illegalArgument(item == null, "null members in colorlist");
      if (item instanceof ColorItem) {
        colorItems[i] = (ColorItem) item;
      } else {
        colorItems[i] = new ColorItem(item, -1);
      }
    }

    return new MultiColor(colorItems);
  }
}
