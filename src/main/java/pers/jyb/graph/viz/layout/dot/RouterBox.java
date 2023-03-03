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

package pers.jyb.graph.viz.layout.dot;

import java.io.Serializable;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.ext.DefaultBox;

public class RouterBox extends DefaultBox implements Serializable {

  private static final long serialVersionUID = 578785652705131390L;

  private final DNode node;

  public RouterBox(double leftBorder, double rightBorder, double upBorder, double downBorder) {
    this(leftBorder, rightBorder, upBorder, downBorder, null);
  }

  public RouterBox(double leftBorder, double rightBorder, double upBorder, double downBorder, DNode node) {
    super(leftBorder, rightBorder, upBorder, downBorder);
    this.node = node;
  }

  @Override
  public void setLeftBorder(double leftBorder) {
    Asserts.illegalArgument(leftBorder > rightBorder, HORIZONTAL_ERROR);
    super.setLeftBorder(leftBorder);
  }

  @Override
  public void setRightBorder(double rightBorder) {
    Asserts.illegalArgument(leftBorder > rightBorder, HORIZONTAL_ERROR);
    super.setRightBorder(rightBorder);
  }

  @Override
  public void setUpBorder(double upBorder) {
    Asserts.illegalArgument(upBorder > downBorder, VERTICAL_ERROR);
    super.setUpBorder(upBorder);
  }

  @Override
  public void setDownBorder(double downBorder) {
    Asserts.illegalArgument(upBorder > downBorder, VERTICAL_ERROR);
    super.setDownBorder(downBorder);
  }

  /**
   * 返回x坐标是否在箱子区域。
   *
   * @param x x坐标
   * @return x坐标是否在箱子区域
   */
  boolean inXRange(double x) {
    return inRange(leftBorder, rightBorder, x);
  }

  /**
   * 返回y坐标是否在箱子区域。
   *
   * @param y y坐标
   * @return y坐标是否在箱子区域
   */
  boolean inYRange(double y) {
    return inRange(upBorder, downBorder, y);
  }

  /**
   * 返回点是否在箱内。
   *
   * @param point 点
   * @return 点是否在箱内
   */
  boolean in(FlatPoint point) {
    if (point == null) {
      return false;
    }

    return inXRange(point.getX()) && inYRange(point.getY());
  }

  /**
   * 如果横坐标距离较近的box wall。如果等于的话返回{@code leftWall}。
   *
   * @param x 水平坐标
   * @return 返回距离较近的box wall
   */
  double closerHorizontalWall(double x) {
    if (x < leftBorder) {
      return leftBorder;
    }
    if (x > rightBorder) {
      return rightBorder;
    }
    if (x - leftBorder <= rightBorder - x) {
      return leftBorder;
    }
    return rightBorder;
  }

  /**
   * 如果纵坐标距离较近的box wall。如果等于的话返回{@code upWall}。
   *
   * @param y 垂直坐标
   * @return 返回距离较近的box wall
   */
  double closerVerticalWall(double y) {
    if (y < upBorder) {
      return upBorder;
    }
    if (y > downBorder) {
      return downBorder;
    }
    if (y - upBorder <= downBorder - y) {
      return upBorder;
    }
    return downBorder;
  }

  public static boolean inRange(double left, double right, double num) {
    return num <= right && num >= left;
  }

  public void minGuarantee(double minWidth, double minHeight) {
    if (getWidth() < minWidth) {
      leftBorder = rightBorder - minWidth;
    }
    if (getHeight() < minHeight) {
      upBorder = downBorder - minHeight;
    }
  }

  DNode getNode() {
    return node;
  }
}
