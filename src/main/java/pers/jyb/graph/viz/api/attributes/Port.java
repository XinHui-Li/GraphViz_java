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

import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.ext.Box;
import pers.jyb.graph.viz.api.ext.PortPosition;
import pers.jyb.graph.viz.api.ext.RatioPortPosition;

public enum Port implements PortPosition {
  WEST(0, new RatioPortPosition(-0.5, 0)),
  NORTH_WEST(1, new RatioPortPosition(-0.5, -0.5)),
  NORTH(2, new RatioPortPosition(0, -0.5)),
  NORTH_EAST(3, new RatioPortPosition(0.5, -0.5)),
  EAST(4, new RatioPortPosition(0.5, 0)),
  SOUTH_EAST(5, new RatioPortPosition(0.5, 0.5)),
  SOUTH(6, new RatioPortPosition(0, 0.5)),
  SOUTH_WEST(7, new RatioPortPosition(-0.5, 0.5));

  private final PortPosition portPosition;

  private final int no;

  Port(int no, PortPosition portPosition) {
    this.no = no;
    this.portPosition = portPosition;
  }

  @Override
  public double horOffset(Box box) {
    return portPosition.horOffset(box);
  }

  @Override
  public double verOffset(Box box) {
    return portPosition.verOffset(box);
  }

  public int getNo() {
    return no;
  }

  public boolean isAxis() {
    return this == Port.WEST || this == Port.NORTH || this == Port.EAST || this == Port.SOUTH;
  }

  public Port pre() {
    if (no == 0) {
      return valueOf(maxNo());
    }
    return valueOf(no - 1);
  }

  public Port next() {
    if (no == maxNo()) {
      return valueOf(0);
    }
    return valueOf(no + 1);
  }

  public static int maxNo() {
    return values().length - 1;
  }

  public static Port valueOf(int no) {
    Asserts.illegalArgument(no < 0 || no >= Port.values().length,
                            "Port no must between 0 and " + (Port.values().length - 1));
    return Port.values()[no];
  }
}
