/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.emd;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Telmo Menezes (telmo@telmomenezes.com)
 * @author Jan Keim
 */
class MinimalCostFlow {

    long compute(long[] e, List<List<Edge>> c, List<List<Edge0>> x) {

        Integer numNodes = e.length;

        List<Integer> nodesToQ = new ArrayList<>(numNodes);
        List<List<Edge1>> rCostForward = new ArrayList<>(numNodes);
        List<List<Edge2>> rCostCapBackward = new ArrayList<>(numNodes);

        for (var i = 0; i < numNodes; i++) {
            nodesToQ.add(0);
            rCostForward.add(new ArrayList<>(c.get(i).size()));

            for (Edge it : c.get(i)) {
                x.get(i).add(new Edge0(it.to, it.cost, 0));
                x.get(it.to).add(new Edge0(i, -it.cost, 0));
                rCostForward.get(i).add(new Edge1(it.to, it.cost));
            }

            rCostCapBackward.add(new ArrayList<>(c.get(i).size()));
        }

        var u = 0L;
        long delta;

        List<Long> d = new ArrayList<>(numNodes);
        List<Integer> prev = new ArrayList<>(numNodes);

        for (var i = 0; i < numNodes; ++i) {
            if (e[i] > u) {
                u = e[i];
            }
            for (Edge it : c.get(i)) {
                rCostCapBackward.get(it.to).add(new Edge2(i, -it.cost, 0));
            }
            d.add(0L);
            prev.add(0);
        }

        while (true) {
            var maxSupply = 0L;
            var k = 0;
            for (var i = 0; i < numNodes; i++) {
                if (e[i] > 0 && maxSupply < e[i]) {
                    maxSupply = e[i];
                    k = i;
                }
            }
            if (maxSupply == 0) {
                break;
            }
            delta = maxSupply;

            var l = new int[1];
            computeShortestPath(d, prev, k, rCostForward, rCostCapBackward, e, l, nodesToQ, numNodes);

            var to = l[0];
            do {
                int from = prev.get(to);

                var itccb = 0;
                while (itccb < rCostCapBackward.get(from).size() && rCostCapBackward.get(from).get(itccb).to != to) {
                    itccb++;
                }
                if (itccb < rCostCapBackward.get(from).size() && rCostCapBackward.get(from).get(itccb).residualCapacity < delta) {
                    delta = rCostCapBackward.get(from).get(itccb).residualCapacity;
                }

                to = from;
            } while (to != k);

            to = l[0];
            do {
                int from = prev.get(to);
                var itx = 0;
                while (x.get(from).get(itx).to != to) {
                    itx++;
                }
                x.get(from).get(itx).flow += delta;

                // update residual for backward edges
                var itccb = 0;
                while (itccb < rCostCapBackward.get(to).size() && rCostCapBackward.get(to).get(itccb).to != from) {
                    itccb++;
                }
                if (itccb < rCostCapBackward.get(to).size()) {
                    rCostCapBackward.get(to).get(itccb).residualCapacity += delta;
                }
                itccb = 0;
                while (itccb < rCostCapBackward.get(from).size() && rCostCapBackward.get(from).get(itccb).to != to) {
                    itccb++;
                }
                if (itccb < rCostCapBackward.get(from).size()) {
                    rCostCapBackward.get(from).get(itccb).residualCapacity -= delta;
                }

                // update e
                e[to] = e[to] + delta;
                e[from] = e[from] - delta;

                to = from;
            } while (to != k);
        }

        // compute distance from x
        var dist = 0L;
        for (var from = 0; from < numNodes; from++) {
            for (Edge0 it : x.get(from)) {
                dist += it.cost * it.flow;
            }
        }
        return dist;
    }

    void computeShortestPath(List<Long> d, List<Integer> prev, int from, List<List<Edge1>> costForward, List<List<Edge2>> costBackward, long[] e, int[] l,
            List<Integer> nodesToQ, Integer numNodes) {
        // Making heap (all inf except 0, so we are saving comparisons...)
        List<Edge3> q = new ArrayList<>(numNodes);
        for (var i = 0; i < numNodes; i++) {
            q.add(new Edge3());
        }

        q.get(0).to = from;
        nodesToQ.set(from, 0);
        q.get(0).dist = 0;

        var j = 1;

        for (var i = 0; i < from; i++) {
            q.get(j).to = i;
            nodesToQ.set(i, j);
            q.get(j).dist = Long.MAX_VALUE;
            j++;
        }

        for (var i = from + 1; i < numNodes; i++) {
            q.get(j).to = i;
            nodesToQ.set(i, j);
            q.get(j).dist = Long.MAX_VALUE;
            j++;
        }

        var finalNodesFlg = new boolean[numNodes];

        do {
            var u = q.get(0).to;

            d.set(u, q.get(0).dist);
            finalNodesFlg[u] = true;
            if (e[u] < 0) {
                l[0] = u;
                break;
            }

            heapRemoveFirst(q, nodesToQ);

            for (Edge1 it : costForward.get(u)) {
                var alt = d.get(u) + it.reducedCost;
                var v = it.to;
                if (nodesToQ.get(v) < q.size() && alt < q.get(nodesToQ.get(v)).dist) {
                    heapDecreaseKey(q, nodesToQ, v, alt);
                    prev.set(v, u);
                }
            }
            for (Edge2 it : costBackward.get(u)) {
                if (it.residualCapacity > 0) {
                    var alt = d.get(u) + it.reducedCost;
                    var v = it.to;
                    if (nodesToQ.get(v) < q.size() && alt < q.get(nodesToQ.get(v)).dist) {
                        heapDecreaseKey(q, nodesToQ, v, alt);
                        prev.set(v, u);
                    }
                }
            }

        } while (!q.isEmpty());

        for (var _from = 0; _from < numNodes; ++_from) {
            for (Edge1 it : costForward.get(_from)) {
                if (finalNodesFlg[_from]) {
                    it.reducedCost += d.get(_from) - d.get(l[0]);
                }
                if (finalNodesFlg[it.to]) {
                    it.reducedCost -= d.get(it.to) - d.get(l[0]);
                }
            }
        }

        for (var _from = 0; _from < numNodes; ++_from) {
            for (Edge2 it : costBackward.get(_from)) {
                if (finalNodesFlg[_from]) {
                    it.reducedCost += d.get(_from) - d.get(l[0]);
                }
                if (finalNodesFlg[it.to]) {
                    it.reducedCost -= d.get(it.to) - d.get(l[0]);
                }
            }
        }
    }

    void heapDecreaseKey(List<Edge3> q, List<Integer> nodesToQ, int v, long alt) {
        int i = nodesToQ.get(v);
        q.get(i).dist = alt;
        while (i > 0 && q.get(parent(i)).dist > q.get(i).dist) {
            swapHeap(q, nodesToQ, i, parent(i));
            i = parent(i);
        }
    }

    void heapRemoveFirst(List<Edge3> q, List<Integer> nodesToQ) {
        swapHeap(q, nodesToQ, 0, q.size() - 1);
        q.remove(q.size() - 1);
        heapify(q, nodesToQ, 0);
    }

    void heapify(List<Edge3> q, List<Integer> nodesToQ, int i) {
        do {
            // TODO: change to loop
            var l = left(i);
            var r = right(i);
            int smallest;
            if (l < q.size() && q.get(l).dist < q.get(i).dist) {
                smallest = l;
            } else {
                smallest = i;
            }
            if (r < q.size() && q.get(r).dist < q.get(smallest).dist) {
                smallest = r;
            }

            if (smallest == i) {
                return;
            }

            swapHeap(q, nodesToQ, i, smallest);
            i = smallest;

        } while (true);
    }

    void swapHeap(List<Edge3> q, List<Integer> nodesToQ, int i, int j) {
        var tmp = q.get(i);
        q.set(i, q.get(j));
        q.set(j, tmp);
        nodesToQ.set(q.get(j).to, j);
        nodesToQ.set(q.get(i).to, i);
    }

    int left(int i) {
        return 2 * (i + 1) - 1;
    }

    int right(int i) {
        return 2 * (i + 1); // 2 * (i + 1) + 1 - 1
    }

    int parent(int i) {
        return (i - 1) / 2;
    }

}
