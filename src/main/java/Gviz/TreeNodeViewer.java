package Gviz;

import java.awt.BorderLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.batik.transcoder.TranscoderException;
import pers.jyb.graph.util.DocumentUtils;
import pers.jyb.graph.viz.api.Graphviz;
import pers.jyb.graph.viz.api.Graphviz.GraphvizBuilder;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.Subgraph;
import pers.jyb.graph.viz.api.attributes.LineStyle;
import pers.jyb.graph.viz.api.attributes.NodeStyle;
import pers.jyb.graph.viz.api.attributes.Rank;
import pers.jyb.graph.viz.draw.ExecuteException;
import pers.jyb.graph.viz.draw.GraphResource;
import pers.jyb.graph.viz.draw.svg.SvgRenderEngine;

public class TreeNodeViewer {

    /**
     * 根据一个String类型的TreeNode测试用例，绘制对应的二叉树。
     *
     * @param scale 图片缩放率
     * @param val String类型的TreeNode测试用例
     * @param printDot 是否需要把对应的dot脚本打印出来
     * @return {@link Graphviz}
     */
    public static Graphviz treeNodeToGraphviz(double scale, String val, boolean printDot) {
        if (val == null) {
            return null;
        }

        val = val.replace("[", "");
        val = val.replace("]", "");
        val = val.replace(" ", "");

        String[] strs = val.split(",");

        Map<String, TNode> map = new HashMap<>();
        Map<Integer, List<String>> rankRecord = new HashMap<>();

        int cn = 1;
        int nn = 0;
        int rank = 1;

        for (int i = 0; i < strs.length; i++) {
            if (cn == 0) {
                cn = nn * 2 - 1;
                nn = 0;
                rank++;
            } else {
                cn--;
            }
            rankRecord.computeIfAbsent(rank, r -> new ArrayList<>()).add(strs[i]);

            if (Objects.equals(strs[i], "null")) {
                continue;
            }
            nn++;
            TNode tNode = new TNode();
            tNode.rank = rank;
            tNode.rankIndex = nn;
            tNode.idx = rankRecord.get(rank).size() - 1;
            tNode.node = Node.builder().label(strs[i]).build();

            map.put(strs[i], tNode);
        }

        GraphvizBuilder digraphBuilder = Graphviz.digraph();

        if (printDot) {
            System.out.println("----------------------- dot脚本 -----------------------");
            System.out.println("digraph G {");
        }

        for (int i = 0; i < strs.length; i++) {
            if (Objects.equals(strs[i], "null")) {
                continue;
            }

            TNode tNode = map.get(strs[i]);
            List<String> nextRankNodes = rankRecord.get(tNode.rank + 1);
            if (nextRankNodes == null) {
                continue;
            }

            int childIndex = (tNode.rankIndex - 1) * 2;
            String child;
            Node ln = null, rn = null;
            if (childIndex < nextRankNodes.size() && !Objects
                    .equals(child = nextRankNodes.get(childIndex), "null")) {
                ln = map.get(child).node;
            }
            childIndex = (tNode.rankIndex - 1) * 2 + 1;
            if (childIndex < nextRankNodes.size() && !Objects
                    .equals(child = nextRankNodes.get(childIndex), "null")) {
                rn = map.get(child).node;
            }

            if (ln != null) {
                if (printDot) {
                    System.out.println("  " + tNode.node.nodeAttrs().getLabel() + "->" + ln.nodeAttrs().getLabel());
                }
                digraphBuilder.addLine(tNode.node, ln);
            }
            if (rn != null) {
                if (printDot) {
                    System.out.println("  " + tNode.node.nodeAttrs().getLabel() + "->" + rn.nodeAttrs().getLabel());
                }
                digraphBuilder.addLine(tNode.node, rn);
            }

            if (rn == null && ln == null) {
                continue;
            }

            // 创建隐藏的节点和线，影响布局效果，让左右节点分布的比较对称
            Node node = Node.builder().style(NodeStyle.INVIS).build();
            digraphBuilder
                    .addLine(Line.builder(tNode.node, node).style(LineStyle.INVIS).weight(5).build());
            String n = "n" + i;
            if (printDot) {
                System.out.println("  " + n + "[style=invis]");
                System.out.println("  " + tNode.node.nodeAttrs().getLabel() + "->" + n + "[style=invis,weight=5]");
            }
            if (ln != null && rn != null) {
                // 隐藏节点居中，设置同层级的线限制顺序
                digraphBuilder.subgraph(
                        Subgraph.builder()
                                .tempLine(Line.tempLine().style(LineStyle.INVIS).build())
                                .rank(Rank.SAME)
                                .addLine(ln, node)
                                .addLine(node, rn)
                                .build()
                );
                if (printDot) {
                    System.out.println(
                            "  {rank=same;edge[style=invis];"
                                    + ln.nodeAttrs().getLabel() + "->" + n + ";"
                                    + n + "->" + rn.nodeAttrs().getLabel() + ";}");
                }
            } else if (ln != null) {
                // 隐藏节点摆在左节点右边
                digraphBuilder.subgraph(
                        Subgraph.builder()
                                .rank(Rank.SAME)
                                .addLine(Line.builder(ln, node).style(LineStyle.INVIS).build())
                                .build()
                );
                if (printDot) {
                    System.out.println(
                            "  {rank=same;edge[style=invis];"
                                    + ln.nodeAttrs().getLabel() + "->" + n + ";}");
                }
            } else {
                // 隐藏节点摆在右节点左边
                digraphBuilder.subgraph(
                        Subgraph.builder()
                                .rank(Rank.SAME)
                                .addLine(Line.builder(node, rn).style(LineStyle.INVIS).build())
                                .build()
                );
                if (printDot) {
                    System.out.println(
                            "  {rank=same;edge[style=invis];"
                                    + n + "->" + rn.nodeAttrs().getLabel() + ";}");
                }
            }
        }

        if (printDot) {
            System.out.println("}");
        }
        return digraphBuilder.scale(scale).build();
    }

    /****************************************************** 辅助类 ******************************************************************/
    private static class TNode {

        private Node node;

        private int rankIndex;

        private int idx;

        private int rank;

        @Override
        public String toString() {
            return "TNode{" +
                    "node=" + node.nodeAttrs().getLabel() +
                    ", rankIndex=" + rankIndex +
                    ", idx=" + idx +
                    ", rank=" + rank +
                    '}';
        }
    }

    /****************************************************** 可视化面板 ******************************************************************/
    static class GraphView extends JFrame {

        public GraphView(Graphviz graphviz, GraphResource graphResource)
                throws IOException, TranscoderException {
            this(graphviz, graphResource, false);
        }

        public GraphView(Graphviz graphviz, GraphResource graphResource, boolean close)
                throws IOException, TranscoderException {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DocumentUtils.svgDocToImg(graphResource.inputStream(), os);
            ImageIcon imageIcon = new ImageIcon(os.toByteArray(), "graphviz");

            JFrame mainframe = new JFrame("graph-support");
            if (close) {
                mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
            JPanel cp = (JPanel) mainframe.getContentPane();
            cp.setLayout(new BorderLayout());
            JLabel label = new JLabel(imageIcon);
            cp.add("Center", label);
            mainframe.pack();
            mainframe.setVisible(true);
        }
    }

    public static void main(String[] args) throws ExecuteException, IOException, TranscoderException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("请输入(示例 [5,2,8,1,3,6,7]) :");
            String str = scanner.nextLine();
            String[] s = str.split(" ");
            if (s.length > 2) {
                return;
            }
            double scale = 0.5;
            if (s.length == 2) {
                scale = Double.parseDouble(s[0]);
            }
            Graphviz graphviz = treeNodeToGraphviz(scale, str, true);
            GraphResource graphResource = SvgRenderEngine.getInstance().render(graphviz);
            new GraphView(graphviz, graphResource);
        }
    }
}

