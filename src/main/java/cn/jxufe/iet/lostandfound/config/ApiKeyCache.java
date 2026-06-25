package cn.jxufe.iet.lostandfound.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * API密钥内存缓存，不持久化到数据库
 * 密钥仅在内存中保存，服务重启后需重新输入
 */
public class ApiKeyCache {

    // 密钥缓存，24小时过期
    private static final long EXPIRE_HOURS = 24;

    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // 每小时清理过期缓存
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        }, 1, 1, TimeUnit.HOURS);
    }

    public static void put(String key, String value) {
        cache.put(key, new CacheEntry(value, System.currentTimeMillis() + EXPIRE_HOURS * 3600 * 1000));
    }

    public static String get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null || entry.isExpired(System.currentTimeMillis())) {
            cache.remove(key);
            return null;
        }
        return entry.value;
    }

    public static void remove(String key) {
        cache.remove(key);
    }

    public static void clear() {
        cache.clear();
    }

    /**
     * 密钥是否已配置
     */
    public static boolean hasKey(String key) {
        return get(key) != null;
    }

    private static class CacheEntry {
        final String value;
        final long expireTime;

        CacheEntry(String value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired(long now) {
            return now > expireTime;
        }
    }
}
