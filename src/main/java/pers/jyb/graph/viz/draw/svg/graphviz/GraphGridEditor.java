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

package pers.jyb.graph.viz.draw.svg.graphviz;

import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Element;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.draw.GraphEditor;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;
import pers.jyb.graph.viz.layout.OrthoVisGraph.Segment;

public class GraphGridEditor extends SvgEditor<GraphvizDrawProp, SvgBrush>
		implements GraphEditor<SvgBrush> {

	private static final String GRID_SEGMENT = "grid_segment";

	@Override
	public boolean edit(GraphvizDrawProp graphvizDrawProp, SvgBrush brush) {
		List<Segment> grid = graphvizDrawProp.getGrid();
		if (CollectionUtils.isEmpty(grid)) {
			return true;
		}

		for (int i = 0; i < grid.size(); i++) {
			Segment segment = grid.get(i);
			List<FlatPoint> points = Arrays.asList(segment.getStart(), segment.getEnd());
			String path = pointsToSvgLine(null, points, false);

			Element pathElement = brush.getOrCreateChildElementById(
					GRID_SEGMENT + SvgConstants.UNDERSCORE + i,
					SvgConstants.PATH_ELE
			);

			pathElement.setAttribute(SvgConstants.D, path);
			pathElement.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
			pathElement.setAttribute(SvgConstants.STROKE, Color.BLACK.value());
			pathElement.setAttribute(SvgConstants.STROKE_DASHARRAY, "1,5");
		}
		return true;
	}
}
