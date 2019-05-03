/*
 * MIT License
 *
 * Copyright (c) 2019 Alexander Meisdalen Sagen <alexmsagen@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package app.sagen.serverflight;

import app.sagen.serverflight.util.Graph;
import app.sagen.serverflight.util.Vertex;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Data
public class FlightGraph {

    private String world;
    private Graph graph;

    // flightpaths from a vertex
    private HashMap<Vertex, List<FlightPath>> flightPaths = new HashMap<>();

    public FlightGraph(String world, Graph graph) {
        this.world = world;
        this.graph = graph;

        setupFlightMovers();
    }

    public void setupFlightMovers() {
        // calculate new FlightPats
        HashMap<Vertex, List<FlightPath>> flightMovers = new HashMap<>();
        for (Vertex from : graph.getAdjVertices().keySet()) {
            if (!from.isTeleportable()) continue; // ignore non-teleportable
            List<FlightPath> paths = new ArrayList<>();
            for (Vertex destination : graph.allReachable(from)) {
                if (!destination.isTeleportable()) continue; // ignore non-teleportable
                paths.add(new FlightPath(this, from, destination));
            }
            flightMovers.put(from, paths);
        }

        // cleanup old and setup new
        if (!flightMovers.isEmpty()) shutdown();
        this.flightPaths = flightMovers;
    }

    public Optional<Vertex> getClosesVertex(float x, float y, float z, float maxDistance) {
        Vertex closest = null;
        float heuristics = Float.MAX_VALUE;
        for (Vertex vertex : graph.getAdjVertices().keySet()) {
            if (closest == null) closest = vertex;
            float calculatedHeuristics = vertex.heristic(x, y, z);
            if (calculatedHeuristics < heuristics) {
                closest = vertex;
                heuristics = calculatedHeuristics;
            }
        }
        if (heuristics > maxDistance || closest == null) return Optional.empty();
        return Optional.of(closest);
    }

    public List<FlightPath> getAllAvailableMoversFrom(Vertex vertex) {
        return flightPaths.getOrDefault(vertex, new ArrayList<>());
    }

    public void update() {
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::update));
    }

    public void updateParticles() {
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::updateParticles));
    }

    public void shutdown() {
        flightPaths.values().forEach(paths -> paths.forEach(FlightPath::shutdown));
        flightPaths.clear();
    }
}
