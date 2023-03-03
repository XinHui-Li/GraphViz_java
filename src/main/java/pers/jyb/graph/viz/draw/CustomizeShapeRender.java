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

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.draw.svg.SvgBrush;

@SuppressWarnings("all")
public abstract class CustomizeShapeRender {

  private static volatile Map<String, CustomizeShapeRender> CUSTOMIZE_REGISTER;

  static {
    ServiceLoader<CustomizeShapeRender> customizeShapeRenders = ServiceLoader
        .load(CustomizeShapeRender.class);
    for (CustomizeShapeRender customizeShapeRender : customizeShapeRenders) {
      register(customizeShapeRender);
    }
  }

  public static void register(CustomizeShapeRender customizeShapeRender) {
    Asserts.nullArgument(customizeShapeRender, "custimizeNodeShape");
    Asserts.nullArgument(customizeShapeRender.getShapeName(),
                         "CustimizeNodeShape can not return null shapeName");
    customizeNodeShapeMap().put(customizeShapeRender.getShapeName(), customizeShapeRender);
  }

  public static CustomizeShapeRender getCustomizeShapeRender(String shapeName) {
    if (CUSTOMIZE_REGISTER == null) {
      return null;
    }
    return CUSTOMIZE_REGISTER.get(shapeName);
  }

  // --------------------------------- Sub class method ---------------------------------
  public abstract void drawSvg(SvgBrush nodeBrush, NodeDrawProp nodeDrawProp);

  public abstract String getShapeName();

  // --------------------------------- private method ---------------------------------
  private static Map<String, CustomizeShapeRender> customizeNodeShapeMap() {
    if (CUSTOMIZE_REGISTER == null) {
      synchronized (CustomizeShapeRender.class) {
        if (CUSTOMIZE_REGISTER == null) {
          CUSTOMIZE_REGISTER = new ConcurrentHashMap<>();
        }
      }
    }
    return CUSTOMIZE_REGISTER;
  }
}
