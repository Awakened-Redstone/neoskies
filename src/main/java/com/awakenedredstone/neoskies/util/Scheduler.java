package com.awakenedredstone.neoskies.util;

import com.awakenedredstone.neoskies.logic.IslandLogic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

public class Scheduler {
    private final Queue<Event> events = new ControlledQueue();

    public void close() {
        events.clear();
    }

    public void tick(MinecraftServer server) {
        Event event;
        while ((event = this.events.peek()) != null && event.triggerTime <= server.getOverworld().getTime()) {
            this.events.remove();
            IslandLogic.getServer().execute(event.callback);
        }
    }

    private synchronized static <T> Comparator<Event> createEventComparator() {
        return Comparator.comparingLong(event -> {
            synchronized (ControlledQueue.class) {
                return event.triggerTime;
            }
        });
    }

    public synchronized void schedule(Identifier id, long executeTick, Runnable callback) {
        events.add(new Event(id, executeTick, callback));
    }

    public synchronized void schedule(long executeTick, Runnable callback) {
        schedule(randomIdentifier(), executeTick, callback);
    }

    public synchronized void scheduleDelayed(Identifier id, MinecraftServer server, long delay, Runnable callback) {
        schedule(id, server.getSaveProperties().getMainWorldProperties().getTime() + delay, callback);
    }

    public synchronized void scheduleDelayed(MinecraftServer server, long delay, Runnable callback) {
        schedule(randomIdentifier(), server.getSaveProperties().getMainWorldProperties().getTime() + delay, callback);
    }

    private Identifier randomIdentifier() {
        return new Identifier(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    record Event(Identifier identifier, long triggerTime, Runnable callback) {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Event event && event.identifier.equals(this.identifier);
        }
    }

    static class ControlledQueue extends PriorityQueue<Event> {
        public ControlledQueue() {
            super(createEventComparator());
        }

        @Override
        public synchronized boolean add(@NotNull Event event) {
            this.remove(event);
            return super.add(event);
        }
    }
}
