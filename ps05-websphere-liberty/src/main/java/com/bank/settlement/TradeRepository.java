package com.bank.settlement;

import com.ibm.websphere.cache.DistributedMap;
import com.ibm.websphere.cache.EntryInfo;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * EJB repository for Trade entities.
 *
 * Combines standard JPA 2.0 persistence (which works on Liberty as-is)
 * with WebSphere's DistributedMap API for cluster-aware caching of
 * in-flight trade state. The cache is used to avoid repeated DB round-trips
 * for the hot path: checking whether a PENDING trade already exists for
 * a given ISIN + counterparty combination within the current settlement window.
 *
 * Cache configuration is managed via the WAS admin console
 * (Dynamic Cache service → cache instance "services/cache/TradeCache").
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TradeRepository {

    private static final Logger LOG = Logger.getLogger(TradeRepository.class.getName());

    /** Cache timeout in seconds — aligned with the T+0 settlement window. */
    private static final int CACHE_TTL_SECONDS = 300;

    /** Priority tier for eviction; WAS Dynamic Cache range 1–16 (1 = highest). */
    private static final int CACHE_PRIORITY = 3;

    /**
     * Standard JPA injection — container-managed persistence context.
     * unitName maps to the persistence unit defined in persistence.xml.
     * This injection pattern is fully portable and will work on Liberty.
     */
    @PersistenceContext(unitName = "TradeSettlementPU")
    private EntityManager em;

    /**
     * WebSphere DistributedMap instance.
     *
     * This is looked up programmatically rather than injected because
     * WAS 8.5 does not expose DistributedMap as a standard resource ref.
     * The cache instance "services/cache/TradeCache" must be pre-configured
     * in the WAS admin console under Resources → Cache instances.
     *
     * NOTE: com.ibm.websphere.cache.DistributedMap is a WAS-proprietary API.
     * The equivalent in Liberty with the distributedMap-1.0 feature is
     * javax.cache.Cache (JCache), but the programming model differs.
     */
    private DistributedMap tradeCache;

    /**
     * Lazily resolve the WAS DistributedMap cache instance.
     * Called on first cache access rather than in a @PostConstruct to
     * avoid failing the bean deployment if the cache is not yet configured.
     */
    private DistributedMap getCache() {
        if (tradeCache == null) {
            try {
                InitialContext ctx = new InitialContext();
                // WAS JNDI name for a Dynamic Cache instance
                tradeCache = (DistributedMap) ctx.lookup("services/cache/TradeCache");
            } catch (NamingException e) {
                LOG.log(Level.WARNING, "DistributedMap lookup failed — caching disabled for this request", e);
            }
        }
        return tradeCache;
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Persists a new trade.  The EntityManager is container-managed;
     * the surrounding transaction is provided by the EJB container.
     */
    public Trade persist(Trade trade) {
        em.persist(trade);
        em.flush(); // force immediate SQL so the DB sequence value is populated
        invalidateCacheEntry(buildCacheKey(trade.getIsin(), trade.getCounterpartyId()));
        LOG.info("Persisted trade: " + trade.getTradeId() + " ISIN=" + trade.getIsin());
        return trade;
    }

    /**
     * Merges changes to a detached trade entity.
     */
    public Trade update(Trade trade) {
        Trade merged = em.merge(trade);
        em.flush();
        invalidateCacheEntry(buildCacheKey(merged.getIsin(), merged.getCounterpartyId()));
        return merged;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    public Trade findById(Long tradeId) {
        return em.find(Trade.class, tradeId);
    }

    /**
     * Returns pending trades for a given ISIN + counterparty.
     *
     * Results are cached in the WAS DistributedMap with a TTL of
     * {@value #CACHE_TTL_SECONDS} seconds to reduce DB load during
     * the peak settlement window (08:00–10:00 LDN).
     */
    @SuppressWarnings("unchecked")
    public List<Trade> findPendingTrades(String isin, String counterpartyId) {
        String cacheKey = buildCacheKey(isin, counterpartyId);

        DistributedMap cache = getCache();
        if (cache != null) {
            List<Trade> cached = (List<Trade>) cache.get(cacheKey);
            if (cached != null) {
                LOG.fine("Cache hit for key: " + cacheKey);
                return cached;
            }
        }

        // Cache miss — query the database
        TypedQuery<Trade> query = em.createNamedQuery("Trade.findByCounterpartyAndStatus", Trade.class);
        query.setParameter("counterpartyId", counterpartyId);
        query.setParameter("status", Trade.SettlementStatus.PENDING);

        List<Trade> results = query.getResultList()
            .stream()
            .filter(t -> isin.equals(t.getIsin()))
            .collect(java.util.stream.Collectors.toList());

        // Populate the cache using WAS EntryInfo for fine-grained TTL + priority control.
        // EntryInfo is a WAS-specific class — there is no standard Java EE equivalent.
        if (cache != null) {
            try {
                EntryInfo entryInfo = new EntryInfo();
                entryInfo.setTimeLimit(CACHE_TTL_SECONDS);
                entryInfo.setPriority(CACHE_PRIORITY);
                entryInfo.setIdObject(cacheKey);
                cache.put(cacheKey, results, entryInfo);
            } catch (Exception e) {
                // Non-fatal — continue without caching
                LOG.log(Level.WARNING, "Failed to populate DistributedMap cache", e);
            }
        }

        return results == null ? Collections.emptyList() : results;
    }

    public List<Trade> findByStatus(Trade.SettlementStatus status) {
        TypedQuery<Trade> query = em.createNamedQuery("Trade.findByStatus", Trade.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    // -------------------------------------------------------------------------
    // Cache helpers
    // -------------------------------------------------------------------------

    private void invalidateCacheEntry(String cacheKey) {
        DistributedMap cache = getCache();
        if (cache != null) {
            cache.remove(cacheKey);
        }
    }

    private String buildCacheKey(String isin, String counterpartyId) {
        return "pendingTrades:" + isin + ":" + counterpartyId;
    }
}
