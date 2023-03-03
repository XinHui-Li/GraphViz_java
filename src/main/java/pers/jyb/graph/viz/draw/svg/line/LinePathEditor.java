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

package pers.jyb.graph.viz.draw.svg.line;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.LineEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;
import pers.jyb.graph.viz.draw.svg.SvgEditors;

public class LinePathEditor extends SvgEditor<LineDrawProp, SvgBrush>
    implements LineEditor<SvgBrush> {

	private static final Logger log = LoggerFactory.getLogger(LinePathEditor.class);

	@Override
	public boolean edit(LineDrawProp lineDrawProp, SvgBrush brush) {
		if (CollectionUtils.isEmpty(lineDrawProp)) {
			if (log.isWarnEnabled()) {
				log.warn("Find the wrong LineDrawProp attribute, "
						         + "terminate the drawing of the svg path, line={}", lineDrawProp.lineAttrs());
			}
			return false;
		}

		Element pathElement = brush.getOrCreateChildElementById(
				SvgBrush.getId(
						SvgEditors.lineId(lineDrawProp, brush),
						SvgConstants.PATH_ELE
				),
				SvgConstants.PATH_ELE
		);

		pathElement.setAttribute(SvgConstants.D, pointsToSvgLine(lineDrawProp.getStart(), lineDrawProp,
		                                                         lineDrawProp.isBesselCurve()));
		pathElement.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
		pathElement.setAttribute(SvgConstants.STROKE, Color.BLACK.value());

		Element title = brush.getOrCreateChildElementById(
				SvgBrush.getId(
						SvgEditors.lineId(lineDrawProp, brush),
						SvgConstants.TITLE_ELE
				),
				SvgConstants.TITLE_ELE
		);

		String text;
		if (brush.drawBoard().drawGraph().getGraphviz().isDirected()) {
			text = lineDrawProp.getLine().tail().nodeAttrs().getLabel()
					+ "->"
					+ lineDrawProp.getLine().head().nodeAttrs().getLabel();
		} else {
			text = lineDrawProp.getLine().tail().nodeAttrs().getLabel()
					+ "--"
					+ lineDrawProp.getLine().head().nodeAttrs().getLabel();
		}

		title.setTextContent(text);
		return true;
	}
}