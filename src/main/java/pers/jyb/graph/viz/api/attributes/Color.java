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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import pers.jyb.graph.viz.api.ColorFormatException;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.StringUtils;

public abstract class Color implements Serializable {

  private static final long serialVersionUID = 2828231898265525591L;

  public static final Color BLACK = Colors.ofRGB("#000000");
  public static final Color WHITE = Colors.ofRGB("#ffffff");
  public static final Color RED = Colors.ofRGB("#ff0000");
  public static final Color ORANGE = Colors.ofRGB("#ffa500");
  public static final Color YELLOW = Colors.ofRGB("#ffff00");
  public static final Color GREEN = Colors.ofRGB("#00ff00");
  public static final Color BLUE = Colors.ofRGB("#0000ff");
  public static final Color INDIGO = Colors.ofRGB("#4b0082");
  public static final Color PURPLE = Colors.ofRGB("#800080");
  public static final Color GOLD = Colors.ofRGB("#ffd700");

  public abstract String value();

  /*------------------------------------------ subgraph ---------------------------------------*/

  public static class MultiColor implements Serializable {

    private static final long serialVersionUID = -8528195538026402857L;

    private final ColorItem[] colorItems;

    MultiColor(ColorItem[] colorItems) {
      Asserts.illegalArgument(colorItems == null || colorItems.length == 0,
          "colorItems can not be null or empty");
      for (int i = 0; i < colorItems.length; i++) {
        if (colorItems[i] == null) {
          throw new IllegalArgumentException("color list contains null item");
        }
      }

      this.colorItems = colorItems;
    }

    public List<ColorItem> getColorItems() {
      return Arrays.asList(colorItems);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MultiColor that = (MultiColor) o;
      return Arrays.equals(colorItems, that.colorItems);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(colorItems);
    }

    @Override
    public String toString() {
      return "MultiColor{" +
          "colorItems=" + Arrays.toString(colorItems) +
          '}';
    }
  }

  public static class FusionColor implements Serializable {

    private static final long serialVersionUID = -1130211242085275427L;

    private Color color;

    private MultiColor colorList;

    public FusionColor(Color color) {
      Asserts.nullArgument(color, "color");
      this.color = color;
    }

    public FusionColor(MultiColor multiColor) {
      Asserts.nullArgument(multiColor, "colorList");
      this.colorList = multiColor;
    }

    public Color getColor() {
      return color;
    }

    public MultiColor getColorList() {
      return colorList;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FusionColor that = (FusionColor) o;
      return Objects.equals(color, that.color) &&
          Objects.equals(colorList, that.colorList);
    }

    @Override
    public int hashCode() {
      return Objects.hash(color, colorList);
    }

    @Override
    public String toString() {
      return "FusionColor{" +
          "color=" + color +
          ", colorList=" + colorList +
          '}';
    }
  }

  public static class ColorItem extends Color implements Serializable {

    private static final long serialVersionUID = -2579526412969480903L;

    private final Color color;

    private final float weight;

    ColorItem(Color color, float weight) {
      Asserts.nullArgument(color, "color");
      this.color = color;
      this.weight = weight;
    }

    public Color getColor() {
      return color;
    }

    public float getWeight() {
      return weight;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ColorItem colorItem = (ColorItem) o;
      return Float.compare(colorItem.weight, weight) == 0 &&
          Objects.equals(color, colorItem.color);
    }

    @Override
    public int hashCode() {
      return Objects.hash(color, weight);
    }

    @Override
    public String value() {
      return color.value() + ";" + weight;
    }
  }

  abstract static class AbstractColor extends Color implements Serializable {

    private static final long serialVersionUID = -7757877504759666027L;

    protected String colorVal;

    protected AbstractColor(String color) {
      this.colorVal = formatNormal(color);
    }

    protected static void emptyColorValid(String color) {
      if (StringUtils.isEmpty(color)) {
        throw new ColorFormatException("color can not be empty");
      }
    }

    @Override
    public String value() {
      return colorVal;
    }

    protected abstract String formatNormal(String color) throws ColorFormatException;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      AbstractColor that = (AbstractColor) o;
      return Objects.equals(colorVal, that.colorVal);
    }

    @Override
    public int hashCode() {
      return Objects.hash(colorVal);
    }
  }

  static class RgbColor extends AbstractColor {

    private static final long serialVersionUID = -1294848178007135771L;

    private static final Pattern pattern = Pattern.compile("^#([0-9a-fA-F]{6}|[0-9a-fA-F]{3})$");

    RgbColor(String color) {
      super(color);
    }

    @Override
    protected String formatNormal(String color) throws ColorFormatException {
      emptyColorValid(color);

      String c = StringUtils.removeSpace(color);
      Asserts.illegalColorFormat(pattern, c, "Illegal RGB color param [" + color + "]");

      if (c.length() == 4) {
        c = c + "000";
      }
      return c;
    }

    @Override
    public String toString() {
      return "RgbColor{" +
          "colorVal='" + colorVal + '\'' +
          '}';
    }
  }

  static class RgbaColor extends AbstractColor {

    private static final long serialVersionUID = -7177006372566626206L;

    private static final Pattern pattern = Pattern.compile("^#([0-9a-fA-F]{6}[0-9]{2})$");

    RgbaColor(String color) {
      super(color);
    }

    @Override
    protected String formatNormal(String color) throws ColorFormatException {
      emptyColorValid(color);

      String c = StringUtils.removeSpace(color);
      Asserts.illegalColorFormat(pattern, c, "Illegal RGBA color param [" + color + "]");
      return c;
    }

    @Override
    public String toString() {
      return "RgbaColor{" +
          "colorVal='" + colorVal + '\'' +
          "} " + super.toString();
    }
  }

  static class HSVColor extends AbstractColor {

    private static final long serialVersionUID = 6100507557414214361L;

    private static final Pattern pattern = Pattern.compile("^(0\\.[0-9]\\d*|1|0|1\\.[0]{1,})$");

    protected HSVColor(String color) {
      super(color);
    }

    @Override
    protected String formatNormal(String color) throws ColorFormatException {
      emptyColorValid(color);

      String c = color.trim();
      c = c.replace(",", " ");
      String[] strs = c.split(" ");

      if (strs.length < 3) {
        throw new ColorFormatException("Illegal HSV color param [" + color + "]");
      }

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < 3; i++) {
        String str = strs[i];
        str = str.trim();
        Asserts.illegalColorFormat(pattern, str, "Illegal HSV color param [" + color + "]");
        sb.append(str);
        if (i != 2) {
          sb.append(",");
        }
      }

      return sb.toString();
    }

    @Override
    public String toString() {
      return "HSVColor{" +
          "colorVal='" + colorVal + '\'' +
          "} " + super.toString();
    }
  }
}