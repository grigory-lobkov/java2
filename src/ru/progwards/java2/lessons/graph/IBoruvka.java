package ru.progwards.java2.lessons.graph;

import java.util.List;

public interface IBoruvka<N, E> {
    List<BoruvkaModel.Edge<N, E>> getMinEdgeTree(BoruvkaModel.Graph<N, E> graph);
}
