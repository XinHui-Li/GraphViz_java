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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import javax.xml.transform.TransformerException;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.dom.GenericDocumentType;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.DocumentUtils;
import pers.jyb.graph.viz.api.Cluster;
import pers.jyb.graph.viz.api.GraphAttrs;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.DrawBoard;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.FailInitResouceException;
import pers.jyb.graph.viz.draw.GraphResource;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;

public class SvgDrawBoard
    implements DrawBoard<SvgBrush, SvgBrush, SvgBrush, SvgBrush> {

  public static final String GRAPH_ROOT = "graph_root";

  private static final String PUBLIC_ID = "-//W3C//DTD SVG 1.1//EN";

  private static final String SYSTEM_ID = "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd";

  private static final String XML_VERSION = "1.0";

  private static final String VIEWBOX_VAL = "0.00 0.00 %s %s";

  private static final String XMLNS_VAL = "http://www.w3.org/2000/svg";

  private static final String XMLNS_XLINK_VAL = "http://www.w3.org/1999/xlink";

  private final SVGOMDocument svgomDocument;

  private final Element graphElement;

  private final DrawGraph drawGraph;

  public SvgDrawBoard(DrawGraph drawGraph) {
    Asserts.nullArgument(drawGraph, "DrawGraph");
    this.drawGraph = drawGraph;

    double width = drawGraph.width();
    double height = drawGraph.height();
    FlatPoint scale = drawGraph.getGraphviz().graphAttrs().getScale();
    if (scale != null) {
      width *= scale.getX();
      height *= scale.getY();
    }

    GenericDocumentType documentType = new GenericDocumentType(
        SvgConstants.SVG_ELE,
        PUBLIC_ID,
        SYSTEM_ID
    );
    svgomDocument = new SVGOMDocument(documentType, new SVGDOMImplementation());
    svgomDocument.setXmlVersion(XML_VERSION);
    svgomDocument.setDocumentXmlEncoding(StandardCharsets.UTF_8.name());
    svgomDocument.setXmlStandalone(false);
    Element svg = svgomDocument.createElement(SvgConstants.SVG_ELE);
    svg.setAttribute(SvgConstants.XMLNS, XMLNS_VAL);
    svg.setAttribute(SvgConstants.XMLNS_XLINK, XMLNS_XLINK_VAL);
    svg.setAttribute(SvgConstants.HEIGHT, height + SvgConstants.PT);
    svg.setAttribute(SvgConstants.WIDTH, width + SvgConstants.PT);
    svg.setAttribute(SvgConstants.VIEWBOX, String.format(VIEWBOX_VAL, width, height));

    graphElement = svgomDocument.createElement(SvgConstants.G_ELE);
    graphElement.setAttribute(SvgConstants.ID, GRAPH_ROOT);
    graphElement.setAttribute(SvgConstants.CLASS, "graph");

    svgomDocument.appendChild(svg);
    svg.appendChild(graphElement);
  }

  @Override
  public synchronized SvgBrush drawGraph(GraphvizDrawProp graphvizDrawProp) {
    return new SvgBrush(graphElement, svgomDocument, this);
  }

  @Override
  public synchronized SvgBrush drawCluster(ClusterDrawProp cluster) {
    Element element = svgomDocument.createElement(SvgConstants.G_ELE);
    element.setAttribute(SvgConstants.ID, clusterId(cluster));
    element.setAttribute(SvgConstants.CLASS, SvgConstants.CLUSTER);

    graphElement.appendChild(element);
    return new SvgBrush(element, svgomDocument, this);
  }

  @Override
  public synchronized SvgBrush drawNode(NodeDrawProp nodeDrawProp) {
    Element element = svgomDocument.createElement(SvgConstants.G_ELE);
    element.setAttribute(SvgConstants.ID, nodeId(nodeDrawProp));
    element.setAttribute(SvgConstants.CLASS, SvgConstants.NODE);
    graphElement.appendChild(element);
    return new SvgBrush(element, svgomDocument, this);
  }

  @Override
  public synchronized SvgBrush drawLine(LineDrawProp line) {
    Element element = svgomDocument.createElement(SvgConstants.G_ELE);
    graphElement.appendChild(element);
    element.setAttribute(SvgConstants.ID, lineId(line));
    return new SvgBrush(element, svgomDocument, this);
  }

  @Override
  public boolean removeNode(Node node) {
    Element ele = svgomDocument.getChildElementById(graphElement, nodeId(node));
    if (ele == null) {
      return false;
    }
    org.w3c.dom.Node n = graphElement.removeChild(ele);
    return n != null;
  }

  @Override
  public boolean removeLine(Line line) {
    Element ele = svgomDocument.getChildElementById(graphElement, lineId(line));
    if (ele == null) {
      return false;
    }
    org.w3c.dom.Node l = graphElement.removeChild(ele);
    return l != null;
  }

  @Override
  public synchronized GraphResource graphResource() throws FailInitResouceException {
    String content;
    try {
      content = DocumentUtils.docToXml(svgomDocument);
    } catch (IOException | TransformerException e) {
      throw new FailInitResouceException(e);
    }

    return new SvgGraphResource(
        Optional.ofNullable(drawGraph.getGraphviz().graphAttrs().getLabel()).orElse("graphviz"),
        content
    );
  }

  public GraphAttrs graphAttrs() {
    return drawGraph.getGraphviz().graphAttrs();
  }

  public DrawGraph drawGraph() {
    return drawGraph;
  }

  public String clusterId(ClusterDrawProp clusterDrawProp) {
    Objects.requireNonNull(clusterDrawProp);
    return clusterId(clusterDrawProp.getCluster());
  }

  public String nodeId(NodeDrawProp nodeDrawProp) {
    Objects.requireNonNull(nodeDrawProp);
    return nodeId(nodeDrawProp.getNode());
  }

  public String lineId(LineDrawProp lineDrawProp) {
    Objects.requireNonNull(lineDrawProp);
    return lineId(lineDrawProp.getLine());
  }

  public String clusterId(Cluster cluster) {
    Objects.requireNonNull(cluster);
    return drawGraph.clusterId(cluster);
  }

  public String nodeId(Node node) {
    Objects.requireNonNull(node);
    return drawGraph.nodeId(node);
  }

  public String lineId(Line line) {
    Objects.requireNonNull(line);
    return drawGraph.lineId(line);
  }
}
