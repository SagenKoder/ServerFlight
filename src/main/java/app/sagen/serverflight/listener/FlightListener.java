package app.sagen.serverflight.listener;

import app.sagen.serverflight.FlightPath;
import app.sagen.serverflight.ServerFlight;
import app.sagen.serverflight.FlightGraph;
import app.sagen.serverflight.WorldController;
import app.sagen.serverflight.util.Vertex;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FlightListener implements Listener {

    // testing
    @EventHandler
    public void onClickBeacon(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();

        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || block == null
                || !block.getType().equals(Material.BEACON)) return;

        e.setCancelled(true);

        FlightGraph flightGraph = WorldController.get().getGraphInWorld(block.getWorld().getName());
        Optional<Vertex> closesVertex = flightGraph.getClosesVertex(block.getX(), block.getY(), block.getZ(), 10);
        if (!closesVertex.isPresent()) {
            p.sendMessage("No nearby flightpath!");
            return;
        }

        List<FlightPath> availableMovers = flightGraph.getAllAvailableMoversFrom(closesVertex.get());
        if (availableMovers.isEmpty()) {
            p.sendMessage("No available flights from this point!");
            return;
        }

        int radius = 2;
        List<FlightPath> cleanPaths = new LinkedList<>();
        for(FlightPath flightPath : availableMovers) {
            int xStart = (int)flightPath.getTo().getX() - radius;
            int yStart = (int)flightPath.getTo().getY() - radius;
            int zStart = (int)flightPath.getTo().getZ() - radius;
            boolean clean = true;
            xLoop:for(int x = xStart; x < xStart + 2*radius; x++ ) {
                for(int y = yStart; y < yStart + 2*radius; y++ ) {
                    for(int z = zStart; z < zStart + 2*radius; z++ ) {
                        if(e.getPlayer().getWorld().getBlockAt(x, y, z).getType().equals(Material.BEACON)) {
                            clean = false;
                            break xLoop;
                        }
                    }
                }
            }
            if(!clean) {
                cleanPaths.add(flightPath);
            }
        }

        p.sendMessage("** All available destinations **");
        for (FlightPath flightPath : cleanPaths) {
            p.sendMessage(" - " + flightPath.getTo().toString());
        }
        p.sendMessage("**");

        FlightPath flightPath = cleanPaths.get(ThreadLocalRandom.current().nextInt(cleanPaths.size()));
        flightPath.addPlayer(p);
    }

}
