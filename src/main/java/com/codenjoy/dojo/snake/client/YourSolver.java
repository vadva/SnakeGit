package com.codenjoy.dojo.snake.client;

/*-
 * #%L
 * Codenjoy - it's a dojo-like platform from developers to developers.
 * %%
 * Copyright (C) 2018 Codenjoy
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.codenjoy.dojo.client.Solver;
import com.codenjoy.dojo.client.WebSocketRunner;
import com.codenjoy.dojo.services.*;
import com.codenjoy.dojo.snake.model.Elements;

import java.util.*;

/**
 * User: Vadim
 */
public class YourSolver implements Solver<Board> {

//    private Board board;
    private Node graph[][];

    Node headTail;
    Node destination;
//
    public Node getHeadTail() {
        return headTail;
    }

    private class Node {
        private boolean visited;
        private HashMap<Direction, Node> neighbors;
        private Point point;
        private int peekValue = 1000;

        public Node(Point point) {
            this.point = point;
            this.neighbors = new HashMap<>();
            this.visited = false;
        }

        public int getPeekValue() {
            return peekValue;
        }

        public void setPeekValue(int peekValue) {
            this.peekValue = peekValue;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        public boolean isVisited() {
            return visited;
        }

        public Point getPoint() {
            return point;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return visited == node.visited && Objects.equals(neighbors, node.neighbors) && Objects.equals(point, node.point);
        }

        @Override
        public int hashCode() {
            return Objects.hash(visited, neighbors, point);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "visited=" + visited +
                    ", neighbors=" + neighbors +
                    ", point=" + point +
                    '}';
        }
    }

    private String printGraph(Board board) {
        for (int y = board.size() - 2; y > 0; y--) {
            System.out.println();
            for (int x = 1; x < board.size() - 1; x++) {
                if (graph[x][y] != null) {
                    System.out.format("[%2d,%2d] %2d ", graph[x][y].point.getX(),graph[x][y].point.getY(),graph[x][y].getPeekValue());
                } else {
                    System.out.print("[null]     ");
                }
            }
        }
        return "null";
    }

    private Node[][] createGraph(Board board) {
        for (int x = 1; x < board.size(); x++) {
            for (int y = 1; y < board.size(); y++) {
                if (board.isAt(x, y, Elements.NONE, Elements.GOOD_APPLE,Elements.BAD_APPLE, Elements.HEAD_RIGHT, Elements.HEAD_UP, Elements.HEAD_LEFT, Elements.HEAD_DOWN)) {
                    graph[x][y] = new Node(new PointImpl(x, y));
                    if (graph[x - 1][y] != null) {
                        graph[x - 1][y].neighbors.put(Direction.RIGHT, graph[x][y]);
                        graph[x][y].neighbors.put(Direction.LEFT, graph[x - 1][y]);
                    }
                    if (graph[x + 1][y] != null) {
                        graph[x + 1][y].neighbors.put(Direction.LEFT, graph[x][y]);
                        graph[x][y].neighbors.put(Direction.RIGHT, graph[x + 1][y]);
                    }
                    if (graph[x][y - 1] != null) {
                        graph[x][y - 1].neighbors.put(Direction.UP, graph[x][y]);
                        graph[x][y].neighbors.put(Direction.DOWN, graph[x][y - 1]);
                    }
                    if (graph[x][y + 1] != null) {
                        graph[x][y + 1].neighbors.put(Direction.DOWN, graph[x][y]);
                        graph[x][y].neighbors.put(Direction.UP, graph[x][y + 1]);
                    }
                }
            }
        }
        return graph;
    }

    private void setAllPeekValue(Node dest, int count) {
        count++;
        Set<Map.Entry<Direction, Node>> set = dest.neighbors.entrySet();

        if (dest != null && !dest.isVisited() && dest.getPeekValue() > count) {
            dest.setPeekValue(count);
            dest.setVisited(true);

            for (Map.Entry<Direction, Node> me : set) {
                setAllPeekValue(dest.neighbors.get(me.getKey()), count);
                dest.setVisited(false);
            }
        }
    }

    private Direction findWay(Board board) {
        Direction priorityWay = Direction.RIGHT;

        int currentDistance = 0;
        int shortDistance = 1000;

        Set<Map.Entry<Direction, Node>> set = getHeadTail().neighbors.entrySet();

        for (Map.Entry<Direction, Node> me : set) {
            currentDistance = me.getValue().getPeekValue();
            if (currentDistance < shortDistance) {
                priorityWay = me.getKey();
                shortDistance = currentDistance;
            }
        }

        if (currentDistance == 1000) {

            Set<Map.Entry<Direction, Node>> pull = getHeadTail().neighbors.entrySet();

            for (Map.Entry<Direction, Node> me : pull) {
                priorityWay = me.getKey();
            }
        }
        long ln = System.currentTimeMillis();
        System.out.println(ln);

        return priorityWay;
    }
    private Direction doSolve(Board board){

        this.graph = new Node[board.size()][board.size()];
        createGraph(board);

        this.headTail = graph[board.getHead().getX()][board.getHead().getY()];

        if(board.getSnake().size()>35){
            this.destination=graph[board.getStones().get(0).getX()][board.getStones().get(0).getY()];
        }else this.destination= graph[board.getApples().get(0).getX()][board.getApples().get(0).getY()];

        setAllPeekValue(this.destination, 0);

        printGraph(board);

        return findWay(board);
    }
    @Override
    public String get(Board board) {
        long l = System.currentTimeMillis();
        System.out.println(l);
        return doSolve(board).toString();
    }

    public static void main(String[] args) {
        WebSocketRunner.runClient(
                // paste here board page url from browser after registration
//
                "http://206.81.21.158/codenjoy-contest/board/player/0fxrbriv3uq1our076hk?code=1806689756399325773",
                new YourSolver(),
                new Board());



    }
}
