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
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.ext.Box;
import pers.jyb.graph.viz.api.ext.CirclePropCalc;
import pers.jyb.graph.viz.api.ext.CylinderPropCalc;
import pers.jyb.graph.viz.api.ext.DiamondPropCalc;
import pers.jyb.graph.viz.api.ext.EllipsePropCalc;
import pers.jyb.graph.viz.api.ext.NotePropCalc;
import pers.jyb.graph.viz.api.ext.ParallelogramPropCalc;
import pers.jyb.graph.viz.api.ext.PlainPropCalc;
import pers.jyb.graph.viz.api.ext.PointPropCalc;
import pers.jyb.graph.viz.api.ext.RectanglePropCalc;
import pers.jyb.graph.viz.api.ext.RegularPolylinePropCalc;
import pers.jyb.graph.viz.api.ext.ShapeCenterCalc;
import pers.jyb.graph.viz.api.ext.ShapePropCalc;
import pers.jyb.graph.viz.api.ext.StarPropCalc;
import pers.jyb.graph.viz.api.ext.SymmetryShapeCenterCalc;
import pers.jyb.graph.viz.api.ext.TrapeziumPropCalc;
import pers.jyb.graph.viz.api.ext.TrianglePropCalc;

public enum NodeShapeEnum implements NodeShape {

  NOTE("note",  new NotePropCalc()),
  PLAIN("plain", 0.1, 0.1, new PlainPropCalc()),
  PLAIN_TEXT("plaintext", new EllipsePropCalc()),
  UNDERLINE("underline", new RectanglePropCalc()),
  ELLIPSE("ellipse", new EllipsePropCalc()),
  CIRCLE("circle", 0.75, 0.75, new CirclePropCalc()),
  RECT("rect", new RectanglePropCalc()),
  POINT("point", 0.1, 0.1, new PointPropCalc()),
  TRIANGLE("triangle", new TrianglePropCalc()),
  DIAMOND("diamond", new DiamondPropCalc()),
  TRAPEZIUM("trapezium", new TrapeziumPropCalc()),
  PARALLELOGRAM("parallelogram", new ParallelogramPropCalc()),
  REGULAR_POLYLINE("regular_polyline", 0.75, 0.75, new RegularPolylinePropCalc()),
  STAR("start", 0.75, 0.75, new StarPropCalc()),
  CYLINDER("cylinder", new CylinderPropCalc()),
  ;

  private final String name;

  private double defaultHeight = 0.5;

  private double defaultWidth = 0.75;

  private final ShapeCenterCalc shapeCenterCalc;

  private final ShapePropCalc shapePropCalc;

  NodeShapeEnum(String name, ShapePropCalc shapePropCalc) {
    this.name = name;
    this.shapeCenterCalc = SymmetryShapeCenterCalc.SSPC;
    this.shapePropCalc = shapePropCalc;
  }

  NodeShapeEnum(String name, double defaultHeight,
                double defaultWidth, ShapePropCalc shapePropCalc) {
    this(name, defaultHeight, defaultWidth, SymmetryShapeCenterCalc.SSPC, shapePropCalc);
  }

  NodeShapeEnum(String name, double defaultHeight,
                double defaultWidth, ShapeCenterCalc shapeCenterCalc,
                ShapePropCalc shapePropCalc) {
    this.name = name;
    this.defaultHeight = defaultHeight;
    this.defaultWidth = defaultWidth;
    this.shapeCenterCalc = shapeCenterCalc;
    this.shapePropCalc = shapePropCalc;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public double leftWidth(Double width) {
    return shapeCenterCalc.leftWidth(width);
  }

  @Override
  public double rightWidth(Double width) {
    return shapeCenterCalc.rightWidth(width);
  }

  @Override
  public double topHeight(Double height) {
    return shapeCenterCalc.topHeight(height);
  }

  @Override
  public double bottomHeight(Double height) {
    return shapeCenterCalc.bottomHeight(height);
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    return shapePropCalc.minContainerSize(innerHeight, innerWidth);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    Asserts.nullArgument(box, "shapePosition");
    Asserts.nullArgument(point, "point");
    return shapePropCalc.in(box, point);
  }

  @Override
  public void ratio(FlatPoint boxSize) {
    Asserts.nullArgument(boxSize, "boxSize");
    shapePropCalc.ratio(boxSize);
  }

  @Override
  public FlatPoint labelCenter(FlatPoint labelSize, Box box) {
    Asserts.nullArgument(box, "box");
    Asserts.nullArgument(labelSize, "labelSize");
    return shapePropCalc.labelCenter(labelSize, box);
  }

  @Override
  public boolean needMargin() {
    return shapePropCalc.needMargin();
  }

  @Override
  public NodeShape post(NodeAttrs nodeAttrs) {
    return shapePropCalc.post(nodeAttrs);
  }

  @Override
  public ShapePropCalc getShapePropCalc() {
    return shapePropCalc;
  }

  public double getDefaultHeight() {
    return defaultHeight;
  }

  public double getDefaultWidth() {
    return defaultWidth;
  }
}
