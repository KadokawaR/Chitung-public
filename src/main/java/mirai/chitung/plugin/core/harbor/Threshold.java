package mirai.chitung.plugin.core.harbor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Threshold {

    final Map<Long, Integer> data = new ConcurrentHashMap<>();
    final int limit;

    Threshold(int limit) {
        this.limit = limit;
    }

    public void clear() {
        data.clear();
    }

    public void count(long id) {
        data.put(id, data.getOrDefault(id, 0) + 1);
    }

    public int get(long id) {
        return data.getOrDefault(id, 0);
    }

    public boolean reachLimit(long id) {
        return data.getOrDefault(id, 0) >= limit;
    }
}
