/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.emd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Algorithm authors:
 *
 * @author Telmo Menezes (telmo@telmomenezes.com)
 * @author Ofir Pele
 *         <p>
 *         Refactored by Črtomir Majer
 */
public class EarthMovers {

    private static final int REMOVE_NODE_FLAG = -1;
    private static final double MULT_FACTOR = 1000000;

    private MinimalCostFlow minimalCostFlow = new MinimalCostFlow();

    public double distance(double[] p, double[] q, double[][] c, double extraMassPenalty) {

        var n = p.length;
        var iP = new long[n];
        var iQ = new long[n];
        var iC = new long[n][n];

        var sumP = 0D;
        var sumQ = 0D;
        var maxC = c[0][0];

        for (var i = 0; i < n; i++) {
            sumP += p[i];
            sumQ += q[i];
            for (var j = 0; j < n; j++) {
                if (c[i][j] > maxC) {
                    maxC = c[i][j];
                }
            }
        }

        var minSum = Math.min(sumP, sumQ);
        var maxSum = Math.max(sumP, sumQ);
        if (maxSum <= 1e100) {
            maxSum = 1e100;
        }
        var pqNormFactor = MULT_FACTOR / maxSum;
        var cNormFactor = MULT_FACTOR / maxC;

        for (var i = 0; i < n; i++) {
            iP[i] = (long) Math.floor(p[i] * pqNormFactor + 0.5);
            iQ[i] = (long) Math.floor(q[i] * pqNormFactor + 0.5);
            for (var j = 0; j < n; j++) {
                iC[i][j] = (long) Math.floor(c[i][j] * cNormFactor + 0.5);
            }
        }

        double dist = distance(iP, iQ, iC, 0);
        dist = dist / pqNormFactor / cNormFactor;

        if (extraMassPenalty == -1) {
            extraMassPenalty = maxC;
        }
        dist += (maxSum - minSum) * extraMassPenalty;

        return dist;
    }

    public long distance(long[] pc, long[] qc, long[][] c, long extraMassPenalty) {

        var n = pc.length;

        if (qc.length != n) {
            throw new IllegalArgumentException();
        }

        long[] p;
        long[] q;
        long absDiffSumPSumQ;
        var sumP = 0L;
        var sumQ = 0L;

        for (var i = 0; i < n; i++) {
            sumP += pc[i];
            sumQ += qc[i];
        }

        if (sumQ > sumP) {
            p = qc;
            q = pc;
            absDiffSumPSumQ = sumQ - sumP;
        } else {
            p = pc;
            q = qc;
            absDiffSumPSumQ = sumP - sumQ;
        }

        var b = new long[2 * n + 2];
        final var THRESHOLD_NODE = 2 * n;
        final var ARTIFICIAL_NODE = THRESHOLD_NODE + 1;
        System.arraycopy(p, 0, b, 0, n);

        for (var i = n; i < 2 * n; i++) {
            b[i] = q[i - n];
        }

        b[THRESHOLD_NODE] = -absDiffSumPSumQ;
        b[ARTIFICIAL_NODE] = 0L;

        var maxC = 0L;
        for (var i = 0; i < n; i++) {
            for (var j = 0; j < n; j++) {
                if (c[i][j] > maxC) {
                    maxC = c[i][j];
                }
            }
        }
        if (extraMassPenalty == -1) {
            extraMassPenalty = maxC;
        }

        Set<Integer> sourcesThatFlowNotOnlyToThresh = new HashSet<>();
        Set<Integer> sinksThatGetFlowNotOnlyFromThresh = new HashSet<>();
        var preFlowCost = 0L;

        List<List<Edge>> cList = new ArrayList<>(b.length);

        for (var i = 0; i < b.length; i++) {
            cList.add(new LinkedList<>());
        }

        for (var i = 0; i < n; i++) {
            if (b[i] == 0) {
                continue;
            }
            for (var j = 0; j < n; j++) {
                if (b[j + n] == 0 || c[i][j] == maxC) {
                    continue;
                }
                cList.get(i).add(new Edge(j + n, c[i][j]));
                sourcesThatFlowNotOnlyToThresh.add(i);
                sinksThatGetFlowNotOnlyFromThresh.add(j + n);
            }
        }

        for (var i = n; i < 2 * n; i++) {
            b[i] = -b[i];
        }

        for (var i = 0; i < n; ++i) {
            cList.get(i).add(new Edge(THRESHOLD_NODE, 0));
            cList.get(THRESHOLD_NODE).add(new Edge(i + n, maxC));
        }

        for (var i = 0; i < ARTIFICIAL_NODE; i++) {
            cList.get(i).add(new Edge(ARTIFICIAL_NODE, maxC + 1));
            cList.get(ARTIFICIAL_NODE).add(new Edge(i, maxC + 1));
        }

        var currentNodeName = 0;
        var nodesNewNames = new int[b.length];
        Arrays.fill(nodesNewNames, REMOVE_NODE_FLAG);

        for (var i = 0; i < n * 2; i++) {
            if (b[i] != 0) {
                if (sourcesThatFlowNotOnlyToThresh.contains(i) || sinksThatGetFlowNotOnlyFromThresh.contains(i)) {
                    nodesNewNames[i] = currentNodeName;
                    currentNodeName++;
                } else {
                    if (i >= n) {
                        preFlowCost -= b[i] * maxC;
                    }
                    b[THRESHOLD_NODE] = b[THRESHOLD_NODE] + b[i];
                }
            }
        }

        nodesNewNames[THRESHOLD_NODE] = currentNodeName;
        currentNodeName++;
        nodesNewNames[ARTIFICIAL_NODE] = currentNodeName;
        currentNodeName++;

        var bb = new long[currentNodeName];

        var j = 0;
        for (var i = 0; i < b.length; i++) {
            if (nodesNewNames[i] != REMOVE_NODE_FLAG) {
                bb[j] = b[i];
                j++;
            }
        }

        List<List<Edge>> cc = new ArrayList<>(bb.length);
        List<List<Edge0>> flows = new ArrayList<>(bb.length);

        for (var i = 0; i < bb.length; i++) {
            cc.add(new LinkedList<>());
            flows.add(new ArrayList<>(bb.length * 2));
        }

        for (var i = 0; i < cList.size(); i++) {
            if (nodesNewNames[i] == REMOVE_NODE_FLAG) {
                continue;
            }
            for (Edge it : cList.get(i)) {
                if (nodesNewNames[it.to] != REMOVE_NODE_FLAG) {
                    cc.get(nodesNewNames[i]).add(new Edge(nodesNewNames[it.to], it.cost));
                }
            }
        }

        var mcfDist = minimalCostFlow.compute(bb, cc, flows);
        return preFlowCost + mcfDist + absDiffSumPSumQ * extraMassPenalty;
    }
}
