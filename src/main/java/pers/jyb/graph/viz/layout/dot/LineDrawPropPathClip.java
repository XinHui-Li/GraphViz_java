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

import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.findInOutPair;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.getFirst;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.getLast;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.newArrowShapePosition;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.straightLineClipShape;

import java.util.ArrayList;
import java.util.List;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.viz.api.ext.ShapePosition;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.InOutPointPair;

class LineDrawPropPathClip extends PathClip<LineDrawProp> {

	public static final LineDrawPropPathClip DEFAULT = new LineDrawPropPathClip();

	@Override
	protected FlatPoint pathFrom(LineDrawProp path) {
		return getFirst(path);
	}

	@Override
	protected FlatPoint pathTo(LineDrawProp path) {
		return getLast(path);
	}

	@Override
	protected LineDrawProp fromArrowClip(double arrowSize, LineDrawProp path) {
		FlatPoint first = getFirst(path);
		return arrowClip(arrowSize, path, first, true);
	}

	@Override
	protected LineDrawProp toArrowClip(double arrowSize, LineDrawProp path) {
		FlatPoint last = getLast(path);
		return arrowClip(arrowSize, path, last, false);
	}

	@Override
	protected LineDrawProp clusterClip(ClusterDrawProp clusterDrawProp, LineDrawProp path) {
		InOutPointPair inOutPair = findInOutPair(1, path, true, clusterDrawProp);

		if (inOutPair != null) {
			FlatPoint p = straightLineClipShape(clusterDrawProp, inOutPair.getIn(),
			                                    inOutPair.getOut());

			return subPath(path, inOutPair, p);
		}

		return null;
	}

	@Override
	protected LineDrawProp nodeClip(NodeDrawProp node, LineDrawProp path, boolean firstStart) {
		InOutPointPair inOutPair = findInOutPair(1, path, firstStart, node);

		if (inOutPair != null) {
			FlatPoint p = straightLineClipShape(node, inOutPair.getIn(), inOutPair.getOut());

			return subPath(path, inOutPair, p);
		}

		return path;
	}

	private LineDrawProp subPath(LineDrawProp path, InOutPointPair inOutPair,
	                             FlatPoint p) {
		if (inOutPair.isDeleteBefore()) {
			List<FlatPoint> temp = subList(inOutPair.getIdx(), path.size(), path);
			path.clear();
			path.addAll(temp);
			path.set(0, p);
		} else {
			List<FlatPoint> temp = subList(0, inOutPair.getIdx() + 1, path);
			path.clear();
			path.addAll(temp);
			path.set(path.size() - 1, p);
		}
		return path;
	}

	private LineDrawProp arrowClip(double arrowSize, LineDrawProp path,
	                               FlatPoint first, boolean firstStart) {
		if (first == null) {
			return path;
		}

		ShapePosition shapePosition = newArrowShapePosition(first, arrowSize);

		InOutPointPair inOutPair = findInOutPair(1, path, firstStart, shapePosition);

		if (inOutPair != null) {
			FlatPoint p = straightLineClipShape(shapePosition, inOutPair.getIn(),
			                                    inOutPair.getOut());

			return subPath(path, inOutPair, p);
		}

		return null;
	}

	private List<FlatPoint> subList(int start, int end, LineDrawProp lineDrawProp) {
		List<FlatPoint> temp = new ArrayList<>(end - start);
		for (int i = start; i < end; i++) {
			temp.add(lineDrawProp.get(i));
		}
		return temp;
	}
}
