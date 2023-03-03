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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pers.jyb.graph.viz.draw.ClusterEditor;
import pers.jyb.graph.viz.draw.DefaultPipelineFactory;
import pers.jyb.graph.viz.draw.DrawBoard;
import pers.jyb.graph.viz.draw.DrawGraph;
import pers.jyb.graph.viz.draw.GraphEditor;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.NodeEditor;
import pers.jyb.graph.viz.draw.PipelineFactory;
import pers.jyb.graph.viz.draw.PipelineRenderEngine;
import pers.jyb.graph.viz.draw.svg.cluster.ClusterBorderEditor;
import pers.jyb.graph.viz.draw.svg.cluster.ClusterColorEditor;
import pers.jyb.graph.viz.draw.svg.cluster.ClusterLabelEditor;
import pers.jyb.graph.viz.draw.svg.graphviz.GraphBasicEditor;
import pers.jyb.graph.viz.draw.svg.graphviz.GraphGridEditor;
import pers.jyb.graph.viz.draw.svg.graphviz.GraphLabelEditor;
import pers.jyb.graph.viz.draw.svg.graphviz.GraphTransformEditor;
import pers.jyb.graph.viz.draw.svg.line.LineArrowEditor;
import pers.jyb.graph.viz.draw.svg.line.LineBoxesEditor;
import pers.jyb.graph.viz.draw.svg.line.LineControlPointsEditor;
import pers.jyb.graph.viz.draw.svg.line.LineFloatLabelsEditor;
import pers.jyb.graph.viz.draw.svg.line.LineLabelEditor;
import pers.jyb.graph.viz.draw.svg.line.LinePathEditor;
import pers.jyb.graph.viz.draw.svg.line.LineStyleEditor;
import pers.jyb.graph.viz.draw.svg.node.NodeColorEditor;
import pers.jyb.graph.viz.draw.svg.node.NodeLabelEditor;
import pers.jyb.graph.viz.draw.svg.node.NodeShapeEditor;
import pers.jyb.graph.viz.draw.svg.node.NodeStyleEditor;
import pers.jyb.graph.viz.layout.FlatShifterStrategy;
import pers.jyb.graph.viz.layout.ShifterStrategy;

public class SvgRenderEngine extends
    PipelineRenderEngine<SvgBrush, SvgBrush, SvgBrush, SvgBrush> {

  private static final SvgRenderEngine svgRenderEngine;

  static {
    svgRenderEngine = new SvgRenderEngine(
        new DefaultPipelineFactory()
    );
  }

  private SvgRenderEngine(PipelineFactory pipelineFactory) {
    super(pipelineFactory);
  }


  public static SvgRenderEngine getInstance() {
    return svgRenderEngine;
  }

  @Override
  protected List<NodeEditor<SvgBrush>> initNodeEditors() {
    return Arrays.asList(
        new NodeShapeEditor(),
        new NodeLabelEditor(),
        new NodeStyleEditor(),
        new NodeColorEditor()
    );
  }

  @Override
  protected List<LineEditor<SvgBrush>> initLineEditors() {
    return Arrays.asList(
        new LinePathEditor(),
        new LineArrowEditor(),
        new LineStyleEditor(),
        new LineLabelEditor(),
        new LineBoxesEditor(),
        new LineFloatLabelsEditor(),
        new LineControlPointsEditor()
    );
  }

  @Override
  protected List<ClusterEditor<SvgBrush>> initClusterEditors() {
    return Arrays.asList(
        new ClusterBorderEditor(),
        new ClusterLabelEditor(),
        new ClusterColorEditor()
    );
  }

  @Override
  protected List<GraphEditor<SvgBrush>> initGraphEditors() {
    return Arrays.asList(
        new GraphBasicEditor(),
        new GraphTransformEditor(),
        new GraphLabelEditor(),
        new GraphGridEditor()
    );
  }

  @Override
  protected DrawBoard<SvgBrush, SvgBrush, SvgBrush, SvgBrush> drawBoard(DrawGraph drawGraph) {
    return new SvgDrawBoard(drawGraph);
  }

  @Override
  public List<ShifterStrategy> shifterStrategys(DrawGraph drawGraph) {
    return Collections.singletonList(
        new FlatShifterStrategy(-drawGraph.getMinX(), -drawGraph.getMinY())
    );
  }
}
