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

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.ext.LabelPositionCalc;
import pers.jyb.graph.viz.api.ext.LabelPositionCalc.BottomLabelPositionCalc;
import pers.jyb.graph.viz.api.ext.LabelPositionCalc.TopLabelPositionCalc;
import pers.jyb.graph.viz.api.ext.LabelPositionCalc.VerCenterLabelPositionCalc;

public enum Labelloc {

  TOP(new TopLabelPositionCalc()),

  CENTER(new VerCenterLabelPositionCalc()),

  BOTTOM(new BottomLabelPositionCalc());

  private LabelPositionCalc labelPositionCalc;

  Labelloc(LabelPositionCalc labelPositionCalc) {
    Asserts.nullArgument(labelPositionCalc, "labelPositionCalc");
    this.labelPositionCalc = labelPositionCalc;
  }

  public double getY(FlatPoint upperLeft, FlatPoint lowerRight, FlatPoint labelSize) {
    return labelPositionCalc.centerPos(upperLeft, lowerRight, labelSize);
  }
}
