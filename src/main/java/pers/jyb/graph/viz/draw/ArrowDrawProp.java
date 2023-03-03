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

import java.io.Serializable;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;

public class ArrowDrawProp implements Serializable {

  private static final long serialVersionUID = 9190033726546148926L;

  private final FlatPoint axisBegin;

  private final FlatPoint axisEnd;

  public ArrowDrawProp(FlatPoint axisBegin, FlatPoint axisEnd) {
    Asserts.nullArgument(axisBegin, "axisBegin");
    Asserts.nullArgument(axisEnd, "axisEnd");
    this.axisBegin = axisBegin;
    this.axisEnd = axisEnd;
  }

  public FlatPoint getAxisBegin() {
    return axisBegin;
  }

  public FlatPoint getAxisEnd() {
    return axisEnd;
  }

  @Override
  public String toString() {
    return "ArrowDrawProp{" +
        "axisBegin=" + axisBegin +
        ", axisEnd=" + axisEnd +
        '}';
  }
}
