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

package pers.jyb.graph.viz.layout;

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.Port;
import pers.jyb.graph.viz.api.attributes.Rankdir;
import pers.jyb.graph.viz.api.ext.Box;
import pers.jyb.graph.viz.draw.ContainerDrawProp;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.Rectangle;
import pers.jyb.graph.viz.layout.dot.RouterBox;

public class FlipShifterStrategy extends AbstractShifterStrategy {

  private final DrawGraph drawGraph;

  public FlipShifterStrategy(DrawGraph drawGraph) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.drawGraph = drawGraph;
    flipDrawGraphRange();
  }

  @Override
  public void movePoint(FlatPoint point) {
    movePoint(drawGraph, point);
  }

  @Override
  public void moveContainerDrawProp(ContainerDrawProp containerDrawProp) {
    if (containerDrawProp == null || notNeedMove()) {
      return;
    }

    if (containerDrawProp.getLabelSize() != null && !containerDrawProp.isNodeProp()) {
      Rankdir rankdir = drawGraph.rankdir();
      if (rankdir == Rankdir.LR || rankdir == Rankdir.RL) {
        containerDrawProp.getLabelSize().flip();
      }
    }
    movePoint(containerDrawProp.getLabelCenter());
    moveRectangle(drawGraph, containerDrawProp);
    moveGrid(containerDrawProp);
  }

  @Override
  public void moveBox(RouterBox routerBox) {
    if (routerBox == null || notNeedMove()) {
      return;
    }

    Rectangle rectangle = new Rectangle();
    rectangle.setLeftBorder(routerBox.getLeftBorder());
    rectangle.setRightBorder(routerBox.getRightBorder());
    rectangle.setUpBorder(routerBox.getUpBorder());
    rectangle.setDownBorder(routerBox.getDownBorder());
    moveRectangle(drawGraph, rectangle);

    routerBox.setRightBorder(Double.MAX_VALUE);
    routerBox.setLeftBorder(rectangle.getLeftBorder());
    routerBox.setRightBorder(rectangle.getRightBorder());
    routerBox.setDownBorder(Double.MAX_VALUE);
    routerBox.setUpBorder(rectangle.getUpBorder());
    routerBox.setDownBorder(rectangle.getDownBorder());
  }

  // --------------------------------------- static method  ---------------------------------------

  public static Port movePort(DrawGraph drawGraph, Port port) {
    if (port == null) {
      return null;
    }
    Rankdir rankdir = drawGraph.rankdir();
    switch (rankdir) {
      case LR:
        switch (port) {
          case WEST:
            return Port.NORTH;
          case NORTH_WEST:
            return Port.NORTH_EAST;
          case NORTH:
            return Port.EAST;
          case NORTH_EAST:
            return Port.SOUTH_EAST;
          case EAST:
            return Port.SOUTH;
          case SOUTH_EAST:
            return Port.SOUTH_WEST;
          case SOUTH:
            return Port.WEST;
          case SOUTH_WEST:
            return Port.NORTH_WEST;
          default:
            break;
        }
        break;
      case RL:
        switch (port) {
          case WEST:
            return Port.SOUTH;
          case NORTH_WEST:
            return Port.SOUTH_EAST;
          case NORTH:
            return Port.EAST;
          case NORTH_EAST:
            return Port.NORTH_EAST;
          case EAST:
            return Port.NORTH;
          case SOUTH_EAST:
            return Port.NORTH_WEST;
          case SOUTH:
            return Port.WEST;
          case SOUTH_WEST:
            return Port.SOUTH_WEST;
          default:
            break;
        }
        break;
      case BT:
        switch (port) {
          case NORTH_WEST:
            return Port.SOUTH_WEST;
          case NORTH:
            return Port.SOUTH;
          case NORTH_EAST:
            return Port.SOUTH_EAST;
          case SOUTH_EAST:
            return Port.NORTH_EAST;
          case SOUTH:
            return Port.NORTH;
          case SOUTH_WEST:
            return Port.NORTH_WEST;
          default:
            return port;
        }
      default:
        break;
    }

    return port;
  }

  public static void movePoint(DrawGraph drawGraph, FlatPoint point) {
    if (point == null || drawGraph == null || notNeedMove(drawGraph.rankdir())) {
      return;
    }

    Rankdir rankdir = drawGraph.rankdir();
    double t;
    switch (rankdir) {
      case LR:
        point.flip();
        flipVertical(point, drawGraph.getMaxY());
        break;
      case RL:
        t = point.getX();
        point.setX(2 * drawGraph.getMaxX() - point.getY());
        point.setY(t);
        flipVertical(point, drawGraph.getMaxY());
        break;
      case BT:
        point.setY(2 * drawGraph.getMaxY() - point.getY());
        break;
      default:
        break;
    }
  }

  public static void movePointOpposite(Rankdir rankdir, Box box, FlatPoint point) {
    if (point == null || box == null || notNeedMove(rankdir)) {
      return;
    }

    double t;
    switch (rankdir) {
      case LR:
        flipVertical(point, box.getDownBorder());
        point.flip();
        break;
      case RL:
        flipVertical(point, box.getDownBorder());
        t = point.getY();
        point.setY(2 * box.getRightBorder() - point.getX());
        point.setX(t);
        break;
      case BT:
        point.setY(2 * box.getDownBorder() - point.getY());
        break;
      default:
        break;
    }
  }

  public static void moveRectangle(DrawGraph drawGraph, Rectangle rectangle) {
    Asserts.nullArgument(drawGraph, "drawGraph");
    moveRectangle(drawGraph.rankdir(), drawGraph.getMaxX(), drawGraph.getMaxY(), rectangle);
  }

  public static void moveRectangle(Rankdir rankdir, Rectangle rectangle) {
    moveRectangle(rankdir, null, null, rectangle);
  }

  public static void moveRectangle(Rankdir rankdir, Double maxX, Double maxY, Rectangle rectangle) {
    if (rectangle == null) {
      return;
    }

    double leftBorder = rectangle.getLeftBorder();
    double rightBorder = rectangle.getRightBorder();
    double upBorder = rectangle.getUpBorder();
    double downBorder = rectangle.getDownBorder();

    maxX = maxX != null ? maxX : rectangle.getRightBorder();
    maxY = maxY != null ? maxY : rectangle.getDownBorder();
    switch (rankdir) {
      case LR:
        rectangle.setLeftBorder(upBorder);
        rectangle.setRightBorder(downBorder);
        rectangle.setUpBorder(leftBorder);
        rectangle.setDownBorder(rightBorder);
        flipVertical(rectangle, maxY);
        break;
      case RL:
        rectangle.setLeftBorder(2 * maxX - downBorder);
        rectangle.setRightBorder(2 * maxX - upBorder);
        rectangle.setUpBorder(leftBorder);
        rectangle.setDownBorder(rightBorder);
        flipVertical(rectangle, maxY);
        break;
      case BT:

        flipVertical(rectangle, maxY);
        break;
      default:
        break;
    }
  }

  // --------------------------------------- private method  ---------------------------------------

  private void flipDrawGraphRange() {
    if (notNeedMove()) {
      return;
    }

    Rankdir rankdir = drawGraph.rankdir();
    if (rankdir == Rankdir.BT) {
      flipVerticalDrawGraph(drawGraph);
      return;
    }

    double minX = drawGraph.getMinX();
    double maxX = drawGraph.getMaxX();
    double minY = drawGraph.getMinY();
    double maxY = drawGraph.getMaxY();
    drawGraph.init();

    switch (rankdir) {
      case LR:
        drawGraph.updateXAxisRange(minY);
        drawGraph.updateXAxisRange(maxY);
        drawGraph.updateYAxisRange(minX);
        drawGraph.updateYAxisRange(maxX);
        flipVerticalDrawGraph(drawGraph);
        break;
      case RL:
        drawGraph.updateXAxisRange(minY);
        drawGraph.updateXAxisRange(2 * minY - maxY);
        drawGraph.updateYAxisRange(minX);
        drawGraph.updateYAxisRange(maxX);
        flipVerticalDrawGraph(drawGraph);
        break;
      default:
        break;
    }
  }

  private static void flipVertical(FlatPoint point, double horAxis) {
    point.setY(2 * horAxis - point.getY());
  }

  private static void flipVertical(Rectangle rectangle, double maxY) {
    double leftBorder = rectangle.getLeftBorder();
    double rightBorder = rectangle.getRightBorder();
    double upBorder = rectangle.getUpBorder();
    double downBorder = rectangle.getDownBorder();

    rectangle.setLeftBorder(leftBorder);
    rectangle.setRightBorder(rightBorder);
    rectangle.setUpBorder(2 * maxY - downBorder);
    rectangle.setDownBorder(2 * maxY - upBorder);
  }

  private void flipVerticalDrawGraph(DrawGraph drawGraph) {
    double minX = drawGraph.getMinX();
    double maxX = drawGraph.getMaxX();
    double minY = drawGraph.getMinY();
    double maxY = drawGraph.getMaxY();
    drawGraph.init();

    drawGraph.updateXAxisRange(minX);
    drawGraph.updateXAxisRange(maxX);
    drawGraph.updateYAxisRange(minY);
    drawGraph.updateYAxisRange(2 * minY - maxY);
  }

  private boolean notNeedMove() {
    return drawGraph.rankdir() == Rankdir.TB;
  }

  private static boolean notNeedMove(Rankdir rankdir) {
    if (rankdir == null) {
      return true;
    }
    return rankdir == Rankdir.TB;
  }
}
