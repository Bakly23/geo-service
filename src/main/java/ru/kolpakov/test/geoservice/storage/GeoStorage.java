package ru.kolpakov.test.geoservice.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.kolpakov.test.geoservice.domain.Coordinates;
import ru.kolpakov.test.geoservice.domain.GeoCellValues;
import ru.kolpakov.test.geoservice.domain.UserLabel;
import ru.kolpakov.test.geoservice.exceptions.GeoServiceException;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static java.lang.Math.*;

@Slf4j
@Component
public class GeoStorage {
    private static final int EARTH_RADIUS = 6378100;
    private final ConcurrentMap<Coordinates, GeoCellValues> geoMap;
    private final ConcurrentMap<Integer, UserLabel> users;
    private final ConcurrentMap<Integer, ReadWriteLock> locks;

    public GeoStorage(ConcurrentMap<Coordinates, GeoCellValues> geoMap,
                      ConcurrentMap<Integer, UserLabel> users) {
        log.info("Size of geo map is {}, number of initially loaded users {}", geoMap.size(), users.size());
        this.geoMap = geoMap;
        this.users = users;
        this.locks = new ConcurrentHashMap<>();
    }

    public Optional<UserLabel> upsertUser(UserLabel user) throws InterruptedException {
        return blockedByIdAction(ReadWriteLock::writeLock, user, UserLabel::getId, u -> {
            Coordinates cell = new Coordinates(u);
            if (!geoMap.containsKey(cell)) {
                throw new GeoServiceException("There is no cell for user " + u);
            }
            if (users.containsKey(u.getId())) {
                UserLabel oldUser = users.get(u.getId());
                geoMap.get(new Coordinates(oldUser)).getUserCounter().decrementAndGet();
            }
            geoMap.get(cell).getUserCounter().incrementAndGet();
            return Optional.ofNullable(users.put(u.getId(), u));
        });
    }

    public boolean checkPlacement(UserLabel user) throws InterruptedException {
        return blockedByIdAction(ReadWriteLock::readLock, user, UserLabel::getId, u -> {
            if (users.containsKey(u.getId())) {
                UserLabel currUser = users.get(u.getId());
                double distanceError = geoMap.get(new Coordinates(currUser)).getDistanceError();
                return calcDistance(u, currUser) < distanceError;
            } else {
                throw new GeoServiceException(String.format("User with id %s does not exist", user.getId()));
            }
        });
    }

    private double calcDistance(UserLabel user, UserLabel currUser) {
        return centralAngle(user, currUser) * EARTH_RADIUS;
    }

    private double centralAngle(UserLabel user, UserLabel currUser) {
        double userLat = user.getLat() * PI / 180;
        double userLon = user.getLon() * PI / 180;
        double currUserLat = currUser.getLat() * PI / 180;
        double currUserLon = currUser.getLon() * PI / 180;
        if (userLat == currUserLat && userLon == currUserLon) {
            return 0.0;
        } else if (userLat == -currUserLat && (userLon == currUserLon + 180 || userLon == currUserLon - 180)) {
            return PI;
        } else {
            return acos(sin(userLat) * sin(currUserLat) + cos(userLat) * cos(currUserLat) * cos(abs(userLon - currUserLon)));
        }
    }

    public Optional<UserLabel> deleteUser(Integer id) throws InterruptedException {
        return blockedByIdAction(ReadWriteLock::writeLock, id, Function.identity(), i -> {
            if (users.containsKey(i)) {
                UserLabel user = users.get(i);
                Coordinates cell = new Coordinates(user);
                geoMap.get(cell).getUserCounter().decrementAndGet();
                return Optional.of(users.remove(i));
            }
            return Optional.empty();
        });
    }

    private <T, P> T blockedByIdAction(Function<ReadWriteLock, Lock> lockTypeDefiner,
                                       P parameter,
                                       Function<P, Integer> idGetter,
                                       Function<P, T> action) throws InterruptedException {
        ReadWriteLock lock;
        while (true) {
            Integer id = idGetter.apply(parameter);
            lock = locks.computeIfAbsent(id, i -> new ReentrantReadWriteLock());
            if (lockTypeDefiner.apply(lock).tryLock(Long.MAX_VALUE, TimeUnit.DAYS)) {
                //checking whether another thread did not put another lock for same key
                if (locks.putIfAbsent(id, lock) == null || locks.get(id) == lock) {
                    T result;
                    try {
                        result = action.apply(parameter);
                    } finally {
                        locks.remove(id);
                        lockTypeDefiner.apply(lock).unlock();
                    }
                    return result;
                }
                lockTypeDefiner.apply(lock).unlock();
            }
        }
    }

    public int countInGeoCell(double lon, double lat) {
        return Optional
                .ofNullable(geoMap.get(new Coordinates(lon, lat)))
                .map(gcv -> gcv.getUserCounter().intValue())
                .orElseThrow(() -> new GeoServiceException("There is no such cell"));
    }
}
