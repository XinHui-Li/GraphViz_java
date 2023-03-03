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

package pers.jyb.graph.viz.draw.svg;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.w3c.dom.Element;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.draw.Brush;

public class SvgBrush implements Brush {

  private final Element element;

  private final SVGOMDocument svgomDocument;

  private final SvgDrawBoard svgDrawBoard;

  private Map<String, List<Element>> eleGroups;

  public SvgBrush(Element element, SVGOMDocument svgomDocument, SvgDrawBoard svgDrawBoard) {
    Asserts.nullArgument(element, "element");
    Asserts.nullArgument(svgomDocument, "svgomDocument");
    Asserts.nullArgument(svgDrawBoard, "svgDrawBorad");
    this.element = element;
    this.svgomDocument = svgomDocument;
    this.svgDrawBoard = svgDrawBoard;
  }

  public SVGOMDocument getSvgomDocument() {
    return svgomDocument;
  }

  public void setAttr(String name, String attr) {
    element.setAttribute(name, attr);
  }

  public Element getOrCreateChildElementById(String id, String tagName) {
    Element ele = svgomDocument.getChildElementById(element, id);
    if (ele == null) {
      ele = svgomDocument.createElement(tagName);
      ele.setAttribute(SvgConstants.ID, id);
      element.appendChild(ele);
    }

    return ele;
  }

  public Element getOrCreateShapeEleById(String id, String tagName) {
    Element shapeEle = getOrCreateChildElementById(id, tagName);
    addGroup(SvgConstants.SHAPE_GROUP_KEY, Collections.singletonList(shapeEle));
    return shapeEle;
  }

  public void addGroup(String key, List<Element> group) {
    if (CollectionUtils.isEmpty(group)) {
      return;
    }

    if (eleGroups == null) {
      eleGroups = new HashMap<>(1);
    }
    eleGroups.put(key, group);
  }

  public List<Element> getEleGroup(String groupKey) {
    if (eleGroups == null) {
      return Collections.emptyList();
    }

    List<Element> group = eleGroups.get(groupKey);
    return CollectionUtils.isEmpty(group) ? Collections.emptyList() : group;
  }

  @Override
  @SuppressWarnings("unchecked")
  public SvgDrawBoard drawBoard() {
    return svgDrawBoard;
  }

  /* -------------------------------------------- static ------------------------------------------ */

  public static String getId(String parentElementId, String elementName) {
    return parentElementId + SvgConstants.UNDERSCORE + elementName;
  }

  public interface ChildEleGenerator {

  }
}
