package lielietea.thevaultnorthkinchovsk.vault;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class VaultCore<U, V extends Valuable> {
    private final Map<V, Map<U, BigDecimal>> space = new HashMap<>();
    private Set<U> historicalUsers = new HashSet<>();
    private Set<V> historicalValuables = new HashSet<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    public void set(U user, V valuable, BigDecimal amount) {
        writeLock.lock();
        try {
            if (!space.containsKey(valuable))
                space.put(valuable, new HashMap<>());
            if (amount.compareTo(BigDecimal.ZERO) == 0){
                space.get(valuable).remove(user);
                if(space.get(valuable).isEmpty())
                    space.remove(valuable);
            }
            else
                space.get(valuable).put(user, amount);
            if(!historicalUsers.contains(user))
                historicalUsers.add(user);
            if(!historicalValuables.contains(valuable))
                historicalValuables.add(valuable);
        } finally {
            writeLock.unlock();
        }
    }

    public BigDecimal getBalance(U user, V valuable) {
        readLock.lock();
        try {
            return space.getOrDefault(valuable, Collections.emptyMap()).getOrDefault(user, BigDecimal.ZERO);
        } finally {
            readLock.unlock();
        }
    }

    public String getDisplayBalance(U user, V valuable, BalanceDisplayFormat format) {
        readLock.lock();
        try {
            return space.getOrDefault(valuable, Collections.emptyMap()).getOrDefault(user, BigDecimal.ZERO).setScale(format.getScale(), format.getRoundingMode()).toPlainString();
        } finally {
            readLock.unlock();
        }
    }

    public Map<V,BigDecimal> getAllBalanceOfUser(U user){
        readLock.lock();
        try {
            Map<V,BigDecimal> m = new HashMap<>();
            space.forEach((v, uBigDecimalMap) -> {
                if(uBigDecimalMap.containsKey(user))
                    m.put(v,uBigDecimalMap.get(user));
            });
            return Collections.unmodifiableMap(m);
        } finally {
            readLock.unlock();
        }
    }

    public Map<U,BigDecimal> getAllBalanceOfValuable(V valuable){
        readLock.lock();
        try {
            return Collections.unmodifiableMap(space.get(valuable));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * @return true if given user has given valuable in vault
     */
    public boolean clearBalance(U user, V valuable) {
        boolean flag = false;
        writeLock.lock();
        try {
            if (space.containsKey(valuable)) {
                if(space.get(valuable).containsKey(user)){
                    space.get(valuable).remove(user);
                    flag = true;
                }
                if(space.get(valuable).isEmpty())
                    space.remove(valuable);
            }
        } finally {
            writeLock.unlock();
        }
        return flag;
    }

    /**
     * @return true if given user has valuable in vault
     */
    public boolean clearUserBalance(U user) {
        AtomicBoolean flag = new AtomicBoolean(false);
        writeLock.lock();
        try {
            space.forEach((v, uBigDecimalMap) -> {
                boolean f = uBigDecimalMap.remove(user) != null;
                if(f) flag.set(true);
            });
        } finally {
            writeLock.unlock();
        }
        return flag.get();
    }

    /**
     * @return true if given user has historical record
     */
    public boolean clearUserComplete(U user) {
        boolean flag;
        writeLock.lock();
        try {
            flag = historicalUsers.remove(user);
            space.forEach((v, uBigDecimalMap) -> {
                uBigDecimalMap.remove(user);
            });
        } finally {
            writeLock.unlock();
        }
        return flag;
    }

    /**
     * @return true if given valuable is in vault
     */
    public boolean clearValuableBalance(V valuable) {
        writeLock.lock();
        try {
            return space.remove(valuable) != null;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * @return true if given valuable has historical record
     */
    public boolean clearValuableComplete(V valuable) {
        boolean flag;
        writeLock.lock();
        try {
            flag = historicalValuables.remove(valuable);
            if(flag)
                space.remove(valuable);
        } finally {
            writeLock.unlock();
        }
        return flag;
    }

    public void clearVaultSpaceOnly() {
        writeLock.lock();
        try {
            space.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public void clearVault() {
        writeLock.lock();
        try {
            space.clear();
            historicalValuables.clear();
            historicalUsers.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public Map<V, Map<U, BigDecimal>> getVaultSpace() {
        readLock.lock();
        try {
            return Collections.unmodifiableMap(space);
        } finally {
            readLock.unlock();
        }
    }

    public Set<U> getHistoricalUsers() {
        readLock.lock();
        try {
            return Collections.unmodifiableSet(historicalUsers);
        } finally {
            readLock.unlock();
        }
    }

    public Set<U> getCurrentUsers() {
        readLock.lock();
        try {
            Set<U> s = new HashSet<>();
            for(Map<U,BigDecimal> m:space.values()){
                m.forEach((u, bigDecimal) -> {
                    s.add(u);
                });
            }
            return Collections.unmodifiableSet(s);
        } finally {
            readLock.unlock();
        }
    }

    public Set<V> getHistoricalValuables() {
        readLock.lock();
        try {
            return Collections.unmodifiableSet(historicalValuables);
        } finally {
            readLock.unlock();
        }
    }

    public Set<V> getCurrentValuables() {
        readLock.lock();
        try {
            return Collections.unmodifiableSet(space.keySet());
        } finally {
            readLock.unlock();
        }
    }

    public void setVaultSpaceOnly(Map<V, Map<U, BigDecimal>> map) {
        writeLock.lock();
        try {
            space.clear();
            space.putAll(map);
            map.forEach((v, uBigDecimalMap) -> {
                uBigDecimalMap.forEach( (u, bigDecimal) -> {
                    historicalUsers.add(u);
                });
                historicalValuables.add(v);
            });
        } finally {
            writeLock.unlock();
        }
    }

    public void setVault(Map<V, Map<U, BigDecimal>> map,Set<U> historicalUsers,Set<V> historicalValuables) {
        writeLock.lock();
        try {
            this.historicalValuables.clear();
            this.historicalValuables = historicalValuables;
            this.historicalUsers.clear();
            this.historicalUsers = historicalUsers;
            space.clear();
            space.putAll(map);
            map.forEach((v, uBigDecimalMap) -> {
                uBigDecimalMap.forEach( (u, bigDecimal) -> {
                    historicalUsers.add(u);
                });
                historicalValuables.add(v);
            });
        } finally {
            writeLock.unlock();
        }
    }
}
