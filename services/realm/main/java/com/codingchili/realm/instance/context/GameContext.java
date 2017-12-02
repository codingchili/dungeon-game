package com.codingchili.realm.instance.context;

import com.codingchili.realm.configuration.RealmContext;
import com.codingchili.realm.instance.model.*;
import com.codingchili.realm.instance.model.events.*;
import com.codingchili.realm.instance.model.npc.ListeningPerson;
import com.codingchili.realm.instance.model.npc.TalkingPerson;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.codingchili.core.context.SystemContext;

import static com.codingchili.realm.instance.model.events.SpawnEvent.SpawnType.DESPAWN;

/**
 * @author Robin Duda
 */
public class GameContext {
    private Map<EventType, Map<Integer, EventProtocol<Event>>> listeners = new HashMap<>();
    private Map<Integer, Entity> entities = new HashMap<>();
    private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean closed = new AtomicBoolean(false);
    private Set<Ticker> tickers = new HashSet<>();
    private InstanceContext instance;
    private Long currentTick = 0L;
    private Grid grid;

    public GameContext(InstanceContext instance) {
        this.instance = instance;
        this.grid = new Grid(256, instance.settings().getWidth());

        //instance.periodic(() -> 20, instance.address(), this::tick);
    }

    private void tick(Long timer) {
        // instance.blocking(block -> {

        Runnable runnable;
        while ((runnable = queue.poll()) != null) {
            runnable.run();
        }

        grid.update(entities.values());

        tickers.forEach(ticker -> {
            if (currentTick % ticker.getTick() == 0) {
                ticker.run();
            }
        });
        //         block.complete();
        //    }, (done) -> {
        if (closed.get()) {
            instance.cancel(timer);
        } else {
            currentTick++;
            if (currentTick == Long.MAX_VALUE) {
                currentTick = 0L;
            }
        }
        //      });
    }

    public GameContext runLater(Runnable runnable) {
        queue.add(runnable);
        return this;
    }

    public Grid getGrid() {
        return grid;
    }

    public void close() {
        closed.set(true);
        publishEvent(new ShutdownEvent());
    }

    public Ticker ticker(Consumer<Ticker> runnable, Integer interval) {
        return new Ticker(this, runnable, interval);
    }

    public void setTicker(Ticker ticker) {
        queue.add(() -> {
            if (ticker.getTick() > 0) {
                tickers.add(ticker);
            } else {
                tickers.remove(ticker);
            }
        });
    }

    public void addEntity(Entity entity) {
        queue.add(() -> {
            System.out.println("spawned entity " + entity.getId() + " at " + entity.getVector());
            entities.put(entity.getId(), entity);
            publishEvent(new SpawnEvent().setEntity(entity));
        });
    }

    public void removeEntity(Entity entity) {
        queue.add(() -> {
            entities.remove(entity.getId());
            publishEvent(new SpawnEvent().setEntity(entity).setType(DESPAWN));
            unsubscribe(entity);
        });
    }

    public void unsubscribe(Entity entity) {
        listeners.forEach((key, value) -> value.remove(entity.getId()));
    }

    public EventProtocol<Event> subscribe(Entity entity) {
        EventProtocol<Event> protocol = new EventProtocol<>(entity);

        queue.add(() -> {
            protocol.available().stream()
                    .map(EventType::valueOf)
                    .forEach(event -> {
                        listeners.computeIfAbsent(event, (key) -> new HashMap<>());
                        listeners.get(event).put(protocol.getId(), protocol);
                    });
        });

        return protocol;
    }

    public void publishEvent(Event event) {
        queue.add(() -> {
            Map<Integer, EventProtocol<Event>> scoped = listeners.computeIfAbsent(event.getType(), (key) -> new HashMap<>());
            String type = event.getType().toString();

            switch (event.getBroadcast()) {
                case PARTITION:
                    // todo implement network partitions.
                case GLOBAL:
                    scoped.values().forEach(listener -> listener.get(type).submit(event));
                    break;
                case ADJACENT:
                    event.getSource().ifPresent(source -> {
                        grid.adjacent(source.getVector()).forEach(entity -> {
                            scoped.get(entity.getId()).get(type).submit(event);
                        });
                    });
                    break;
            }
        });
    }

    public Optional<Entity> getEntity(Integer id) {
        return Optional.ofNullable(entities.get(id));
    }

    public static void main(String[] args) throws InterruptedException {
        InstanceContext ins = new InstanceContext(new RealmContext(new SystemContext()), new InstanceSettings());
        GameContext game = new GameContext(ins);

        for (int i = 0; i < 1000; i++) {
            game.addEntity(new TalkingPerson(game));
            game.addEntity(new TalkingPerson(game));
            game.addEntity(new ListeningPerson(game));
        }

        System.out.println("BEGIN");
        long time = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            game.tick(0L);

            System.out.println(i);
        }
        System.out.println("END: " + (System.currentTimeMillis() - time) + "ms.");
        System.out.println(ListeningPerson.called);

        //  System.exit(0);

/*        game.addEntity(new TalkingPerson(game));
        game.addEntity(new TalkingPerson(game));
        game.addEntity(new ListeningPerson(game));*/
    }
}