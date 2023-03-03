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

package pers.jyb.graph.viz.api;

import java.io.Serializable;
import pers.jyb.graph.util.Asserts;

public class FloatLabel implements Serializable {

  private static final long serialVersionUID = 8788129136334958892L;

  private String label;

  private float fontSize;

  private double lengthRatio;

  private double distRatio;

  public FloatLabel(String label, float fontSize, double lengthRatio, double distRatio) {
    this.label = label;
    this.fontSize = fontSize;
    this.lengthRatio = lengthRatio;
    this.distRatio = distRatio;
  }

  public String getLabel() {
    return label;
  }

  public float getFontSize() {
    return fontSize;
  }

  public double getLengthRatio() {
    return lengthRatio;
  }

  public double getDistRatio() {
    return distRatio;
  }

  public static FloatLabelBuilder builder() {
    return new FloatLabelBuilder();
  }

  public static class FloatLabelBuilder {

    private String label;

    private float fontSize = 14;

    private double lengthRatio;

    private double distRatio = 0.5F;

    private FloatLabelBuilder() {
    }

    public FloatLabelBuilder label(String label) {
      Asserts.nullArgument(label, "floatLabel");
      this.label = label;
      return this;
    }

    public FloatLabelBuilder fontSize(float fontSize) {
      Asserts.illegalArgument(fontSize <= 0, "fontSize (" + fontSize + ") must be > 0");
      this.fontSize = fontSize;
      return this;
    }

    public FloatLabelBuilder lengthRatio(double lengthRatio) {
      Asserts.illegalArgument(lengthRatio < 0 || lengthRatio > 1,
                              "lengthRatio" + lengthRatio + " must between 0 and 1");
      this.lengthRatio = lengthRatio;
      return this;
    }

    public FloatLabelBuilder distRatio(double distRatio) {
      this.distRatio = distRatio;
      return this;
    }

    public FloatLabel build() {
      Asserts.nullArgument(label, "floatLabel");
      return new FloatLabel(label, fontSize, lengthRatio, distRatio);
    }
  }
}
