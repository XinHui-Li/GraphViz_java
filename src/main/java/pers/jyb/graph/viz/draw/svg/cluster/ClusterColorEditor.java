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

package pers.jyb.graph.viz.draw.svg.cluster;

import org.w3c.dom.Element;
import pers.jyb.graph.viz.api.ClusterAttrs;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.ClusterEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;

public class ClusterColorEditor extends SvgEditor<ClusterDrawProp, SvgBrush>
    implements ClusterEditor<SvgBrush> {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    cluster.checkBox();

    Element clusterEle = brush.getOrCreateChildElementById(
        SvgBrush.getId(cluster.id(), SvgConstants.POLYGON_ELE),
        SvgConstants.POLYGON_ELE
    );

    ClusterAttrs clusterAttrs = cluster.getCluster().clusterAttrs();
    FusionColor bgColor = clusterAttrs.getBgColor();
    if (bgColor != null && bgColor.getColor() != null) {
      clusterEle.setAttribute(SvgConstants.FILL, bgColor.getColor().value());
    } else {
      clusterEle.setAttribute(SvgConstants.FILL, SvgConstants.NONE);
    }

    FusionColor color = clusterAttrs.getColor();
    if (color != null && color.getColor() != null) {
      clusterEle.setAttribute(STROKE, color.getColor().value());
    } else {
      clusterEle.setAttribute(SvgConstants.STROKE, Color.BLACK.value());
    }

    return true;
  }
}
