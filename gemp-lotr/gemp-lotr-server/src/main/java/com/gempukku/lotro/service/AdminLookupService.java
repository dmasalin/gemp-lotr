package com.gempukku.lotro.service;

import com.gempukku.lotro.common.BlueprintUtils;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.CardCollection;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.game.packs.SetDefinition;
import com.gempukku.lotro.packs.PackBox;
import com.gempukku.lotro.packs.ProductLibrary;
import com.gempukku.lotro.packs.RandomFoilPack;
import com.gempukku.lotro.packs.WeightedRandomPack;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The lookups behind the admin "Add items to collections" form: fuzzy search over every card and product that can be
 * handed out, fuzzy search over player names, and checking a typed list of players / items before anything is
 * awarded.  The matching and ranking live in static methods so they can be tested without a database.
 * <p>
 * The item strings this produces ({@link ItemEntry#value}) are exactly what {@link CardCollection.Item#createItem}
 * parses: a blueprint id for a card, the product name for a pack, and the "(S)..." product name for a selection.
 */
public class AdminLookupService {
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 50;
    // How many names the player search asks the database for before ranking; generous so that the ranking, not the
    // database order, decides which names make the cut.
    private static final int PLAYER_CANDIDATES = 200;
    private static final int SUGGESTIONS = 3;

    private static final Pattern BLUEPRINT_ID_QUERY = Pattern.compile("^\\d+_\\w*\\*?$");

    public enum ItemKind {
        CARD("card"), PACK("pack"), SELECTION("selection"), AWARD("award");

        public final String label;

        ItemKind(String label) {
            this.label = label;
        }
    }

    /**
     * One thing that can be handed out.
     *
     * @param value     the item string the addItems endpoint expects after the "Nx" count
     * @param kind      card / pack / selection / award (a random reward, usually opened on delivery)
     * @param title     what the admin reads: the card title or the product name
     * @param subtitle  card subtitle, or null
     * @param detail    disambiguation: set name, "alternate of ...", "opened on delivery", ...
     * @param alternate true for promo / alternate-art ids that map onto another card; ranked after the base card
     */
    public record ItemEntry(String value, ItemKind kind, String title, String subtitle, String detail, boolean alternate) {
    }

    /**
     * A pasted player name checked against the database.
     *
     * @param input       what the admin typed
     * @param name        the real player name, or null when there is no such player
     * @param suggestions close names when {@code name} is null
     */
    public record PlayerResolution(String input, String name, List<String> suggestions) {
    }

    /**
     * One line of an item list checked against the libraries.
     *
     * @param line    the line as given, trimmed
     * @param count   the parsed count (0 when unparseable)
     * @param value   the parsed item id / product name, or null
     * @param kind    what it is, or null when unknown
     * @param name    readable name, or null
     * @param problem null when the line is fine, else what is wrong with it
     */
    public record ItemCheck(String line, int count, String value, ItemKind kind, String name, String problem) {
    }

    private final LotroCardBlueprintLibrary _cardLibrary;
    private final ProductLibrary _productLibrary;
    private final PlayerDAO _playerDAO;

    private volatile List<IndexedEntry> _cardIndex;

    public AdminLookupService(LotroCardBlueprintLibrary cardLibrary, ProductLibrary productLibrary, PlayerDAO playerDAO) {
        _cardLibrary = cardLibrary;
        _productLibrary = productLibrary;
        _playerDAO = playerDAO;
        if (_cardLibrary != null)
            _cardLibrary.subscribeToRefreshes(() -> _cardIndex = null);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Items
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Cards and products whose title / id matches {@code query}, best first.
     */
    public List<ItemEntry> searchItems(String query, int limit) {
        List<IndexedEntry> index = new ArrayList<>(getCardIndex());
        for (ItemEntry product : buildProductEntries(_productLibrary))
            index.add(new IndexedEntry(product));
        return rankIndexed(query, index, limit);
    }

    /**
     * Ranks {@code entries} against {@code query}; see {@link #rank} for the order.  Entries that do not match are
     * dropped; at most {@code limit} (clamped to 1..{@link #MAX_LIMIT}) are returned.
     */
    public static List<ItemEntry> rankItems(String query, Collection<ItemEntry> entries, int limit) {
        List<IndexedEntry> index = new ArrayList<>(entries.size());
        for (ItemEntry entry : entries)
            index.add(new IndexedEntry(entry));
        return rankIndexed(query, index, limit);
    }

    private static List<ItemEntry> rankIndexed(String query, List<IndexedEntry> index, int limit) {
        limit = clampLimit(limit);
        String raw = query == null ? "" : query.trim();
        if (raw.isEmpty())
            return new ArrayList<>();
        Query q = new Query(raw);
        if (q.compact.isEmpty() && !q.idQuery)
            return new ArrayList<>();

        List<Scored> scored = new ArrayList<>();
        for (IndexedEntry entry : index) {
            int rank = rank(q, entry);
            if (rank >= 0)
                scored.add(new Scored(entry, rank));
        }
        scored.sort(q.idQuery ? ID_ORDER : SCORED_ORDER);

        List<ItemEntry> result = new ArrayList<>();
        for (int i = 0; i < scored.size() && i < limit; i++)
            result.add(scored.get(i).entry.entry);
        return result;
    }

    /**
     * Lower is better, -1 is no match.
     * <ol start="0">
     *     <li>the id / product name is the query, or the title is the query (ignoring case, accents, punctuation)</li>
     *     <li>the title starts with the query; for an id-shaped query ("1_1"), the id starts with it</li>
     *     <li>a later word of the title starts with the query</li>
     *     <li>the title contains the query</li>
     *     <li>the subtitle contains the query</li>
     *     <li>every word of the query appears somewhere in title, subtitle or id</li>
     * </ol>
     */
    private static int rank(Query q, IndexedEntry e) {
        if (e.valueLower.equals(q.lower))
            return 0;
        if (q.idQuery) {
            // "1_1" means the id, not a title containing "1"; a trailing foil "*" is ignored here (the form has its
            // own foil switch)
            if (e.entry.kind != ItemKind.CARD)
                return -1;
            if (e.valueLower.equals(q.idLower))
                return 0;
            if (e.valueLower.startsWith(q.idLower))
                return 1;
            return -1;
        }
        if (q.compact.isEmpty())
            return -1;
        if (e.titleCompact.equals(q.compact))
            return 0;
        if (e.titleCompact.startsWith(q.compact))
            return 1;
        if (e.titleNorm.contains(" " + q.norm))
            return 2;
        if (e.titleCompact.contains(q.compact))
            return 3;
        if (!e.subtitleCompact.isEmpty() && e.subtitleCompact.contains(q.compact))
            return 4;
        if (q.words.length > 1) {
            for (String word : q.words) {
                if (!e.allCompact.contains(word))
                    return -1;
            }
            return 5;
        }
        return -1;
    }

    private static final Comparator<Scored> SCORED_ORDER = Comparator
            .comparingInt((Scored s) -> s.rank)
            .thenComparing(s -> s.entry.entry.alternate)
            .thenComparingInt(s -> s.entry.titleCompact.length())
            .thenComparing(s -> s.entry.titleNorm)
            .thenComparing(s -> s.entry.entry.value, AdminLookupService::compareIds);

    // an id query lists the ids in set / number order
    private static final Comparator<Scored> ID_ORDER = Comparator
            .comparingInt((Scored s) -> s.rank)
            .thenComparing(s -> s.entry.entry.value, AdminLookupService::compareIds);

    /**
     * Blueprint ids in set / number order ("1_2" before "1_10"); anything else alphabetically.
     */
    static int compareIds(String a, String b) {
        String[] pa = BlueprintUtils.stripModifiers(a).split("_");
        String[] pb = BlueprintUtils.stripModifiers(b).split("_");
        if (pa.length == 2 && pb.length == 2) {
            try {
                int c = Integer.compare(Integer.parseInt(pa[0]), Integer.parseInt(pb[0]));
                if (c != 0)
                    return c;
                c = Integer.compare(Integer.parseInt(pa[1]), Integer.parseInt(pb[1]));
                if (c != 0)
                    return c;
            } catch (NumberFormatException ignored) {
            }
        }
        return a.compareTo(b);
    }

    private List<IndexedEntry> getCardIndex() {
        List<IndexedEntry> index = _cardIndex;
        if (index == null) {
            index = new ArrayList<>();
            for (ItemEntry entry : buildCardEntries(_cardLibrary))
                index.add(new IndexedEntry(entry));
            _cardIndex = index;
        }
        return index;
    }

    /**
     * Every card id that can be handed out: the defined cards plus the alternate ids (promos, alternate art) that
     * map onto them.  The "Future Prize" placeholders are left out; those are handed out as promises.
     */
    public static List<ItemEntry> buildCardEntries(LotroCardBlueprintLibrary library) {
        List<ItemEntry> result = new ArrayList<>();
        if (library == null)
            return result;
        Map<String, LotroCardBlueprint> cards = copyOf(library.getBaseCards());
        Map<String, String> mappings = copyOf(library.getAllMappings());
        Map<String, SetDefinition> sets = copyOf(library.getSetDefinitions());

        for (Map.Entry<String, LotroCardBlueprint> card : cards.entrySet()) {
            String id = card.getKey();
            if (LotroCardBlueprintLibrary.isPlaceholderId(id) || card.getValue() == null)
                continue;
            result.add(new ItemEntry(id, ItemKind.CARD, card.getValue().getTitle(), emptyToNull(card.getValue().getSubtitle()),
                    setDetail(id, sets), false));
        }
        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            String id = mapping.getKey();
            if (cards.containsKey(id) || LotroCardBlueprintLibrary.isPlaceholderId(id))
                continue;
            LotroCardBlueprint base = cards.get(mapping.getValue());
            if (base == null)
                continue;
            String detail = setDetail(id, sets);
            detail = (detail == null ? "" : detail + ", ") + "alternate of " + mapping.getValue();
            result.add(new ItemEntry(id, ItemKind.CARD, base.getTitle(), emptyToNull(base.getSubtitle()), detail, true));
        }
        return result;
    }

    private static String setDetail(String id, Map<String, SetDefinition> sets) {
        try {
            SetDefinition set = sets.get(BlueprintUtils.getSet(id));
            return set == null ? null : set.getSetName();
        } catch (Exception exp) {
            return null;
        }
    }

    /**
     * Every product by name: "(S)..." names are selections (the player picks), random rewards (foil draws, weighted
     * random tables, anything opened on delivery) are awards, everything else is a pack.
     */
    public static List<ItemEntry> buildProductEntries(ProductLibrary library) {
        List<ItemEntry> result = new ArrayList<>();
        if (library == null)
            return result;
        Map<String, PackBox> products = copyOf(library.GetAllProducts());
        for (Map.Entry<String, PackBox> product : products.entrySet()) {
            String name = product.getKey();
            if (name == null || name.isBlank() || name.contains("_") || name.contains("\n"))
                continue; // could not be handed out: the item parser would read it as a card id
            boolean opens = library.opensOnDelivery(name);
            ItemKind kind;
            if (name.startsWith("(S)"))
                kind = ItemKind.SELECTION;
            else if (opens || product.getValue() instanceof RandomFoilPack || product.getValue() instanceof WeightedRandomPack)
                kind = ItemKind.AWARD;
            else
                kind = ItemKind.PACK;
            String detail = opens ? "opened on delivery" : null;
            result.add(new ItemEntry(name, kind, name, null, detail, false));
        }
        return result;
    }

    /**
     * Checks one "Nx item" line the way the addItems endpoint will read it.
     */
    public ItemCheck checkItemLine(String line) {
        String trimmed = line == null ? "" : line.trim();
        CardCollection.Item item;
        try {
            item = CardCollection.Item.createItem(trimmed);
        } catch (RuntimeException exp) {
            return new ItemCheck(trimmed, 0, null, null, null, "'" + trimmed + "' is not in the form Nxitem (e.g. 2x1_1).");
        }
        String value = item.getBlueprintId();
        if (item.getCount() < 1)
            return new ItemCheck(trimmed, item.getCount(), value, null, null, "'" + trimmed + "': the count must be at least 1.");
        switch (item.getType()) {
            case CARD -> {
                if (LotroCardBlueprintLibrary.isPlaceholderId(value))
                    return new ItemCheck(trimmed, item.getCount(), value, null, null,
                            "'" + value + "' is a Future Prize placeholder; hand those out as promises instead.");
                try {
                    LotroCardBlueprint bp = _cardLibrary.getLotroCardBlueprint(value);
                    return new ItemCheck(trimmed, item.getCount(), value, ItemKind.CARD, bp.getFullName(), null);
                } catch (CardNotFoundException | RuntimeException exp) {
                    return new ItemCheck(trimmed, item.getCount(), value, null, null, "'" + value + "' is not a known card.");
                }
            }
            default -> {
                PackBox product = _productLibrary.GetProduct(value);
                if (product == null)
                    return new ItemCheck(trimmed, item.getCount(), value, null, null, "'" + value + "' is not a known pack or selection.");
                ItemKind kind = value.startsWith("(S)") ? ItemKind.SELECTION
                        : (_productLibrary.opensOnDelivery(value) || product instanceof RandomFoilPack || product instanceof WeightedRandomPack)
                        ? ItemKind.AWARD : ItemKind.PACK;
                return new ItemCheck(trimmed, item.getCount(), value, kind, value, null);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Players
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Player names containing {@code query} (case-insensitive), best first.
     */
    public List<String> searchPlayers(String query, int limit) {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty())
            return new ArrayList<>();
        return rankPlayerNames(q, _playerDAO.findPlayerNames(q, PLAYER_CANDIDATES), limit);
    }

    /**
     * Ranks player names against {@code query}: exact (ignoring case) first, then names starting with it, then names
     * containing it; ties go to the shorter name, then alphabetically.  Non-matching names are dropped.
     */
    public static List<String> rankPlayerNames(String query, Collection<String> candidates, int limit) {
        limit = clampLimit(limit);
        String q = foldCase(query == null ? "" : query.trim());
        if (q.isEmpty() || candidates == null)
            return new ArrayList<>();
        record Hit(String name, String folded, int rank) {
        }
        List<Hit> hits = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String name : candidates) {
            if (name == null || !seen.add(name))
                continue;
            String folded = foldCase(name);
            int rank = folded.equals(q) ? 0 : folded.startsWith(q) ? 1 : folded.contains(q) ? 2 : -1;
            if (rank >= 0)
                hits.add(new Hit(name, folded, rank));
        }
        hits.sort(Comparator.comparingInt(Hit::rank).thenComparingInt(h -> h.name.length()).thenComparing(Hit::folded).thenComparing(Hit::name));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < hits.size() && i < limit; i++)
            result.add(hits.get(i).name);
        return result;
    }

    /**
     * Resolves every name of a pasted list to a real player (exact name first, then a case-insensitive match), or
     * reports it as unknown with a few close names.  Duplicates (ignoring case) are reported once.
     */
    public List<PlayerResolution> resolvePlayers(Collection<String> inputs) {
        List<PlayerResolution> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String raw : inputs) {
            String input = raw == null ? "" : raw.trim();
            if (input.isEmpty() || !seen.add(foldCase(input)))
                continue;
            Player player = _playerDAO.getPlayer(input);
            if (player != null) {
                result.add(new PlayerResolution(input, player.getName(), List.of()));
                continue;
            }
            List<String> close = rankPlayerNames(input, _playerDAO.findPlayerNames(input, PLAYER_CANDIDATES), MAX_LIMIT);
            String caseInsensitive = null;
            for (String name : close) {
                if (name.equalsIgnoreCase(input)) {
                    caseInsensitive = name;
                    break;
                }
            }
            if (caseInsensitive != null)
                result.add(new PlayerResolution(input, caseInsensitive, List.of()));
            else
                result.add(new PlayerResolution(input, null, close.subList(0, Math.min(SUGGESTIONS, close.size()))));
        }
        return result;
    }

    /**
     * Splits a pasted player list: one name per line, or separated by commas, semicolons or spaces (player names
     * never contain any of those).
     */
    public static List<String> splitPlayerList(String text) {
        List<String> result = new ArrayList<>();
        if (text == null)
            return result;
        for (String part : text.split("[\\s,;]+")) {
            if (!part.isBlank())
                result.add(part.trim());
        }
        return result;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Normalisation
    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Lower case, accents removed ("Nazgûl" -> "nazgul"), curly quotes straightened, and every run of other
     * punctuation / spaces turned into one space.
     */
    public static String normalize(String text) {
        if (text == null)
            return "";
        String n = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('’', '\'')
                .replace('‘', '\'')
                .toLowerCase(Locale.ROOT);
        // apostrophes join ("Gorbag's" -> "gorbags"); everything else separates words
        n = n.replace("'", "");
        return n.replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    /**
     * {@link #normalize} without the spaces, so "witchking", "witch-king" and "Witch-king" all compare equal.
     */
    public static String compact(String text) {
        return normalize(text).replace(" ", "");
    }

    private static String foldCase(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    public static int clampLimit(int limit) {
        if (limit <= 0)
            return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /**
     * The libraries hand out read-only views of maps a reload refills in place; copy (retrying once if a reload was
     * caught mid-way) before iterating.
     */
    private static <K, V> Map<K, V> copyOf(Map<K, V> map) {
        if (map == null)
            return new HashMap<>();
        for (int attempt = 0; ; attempt++) {
            try {
                return new HashMap<>(map);
            } catch (ConcurrentModificationException exp) {
                if (attempt >= 2)
                    throw exp;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private static final class Query {
        final String lower;
        final String norm;
        final String compact;
        final String[] words;
        final boolean idQuery;
        final String idLower;

        Query(String raw) {
            lower = raw.toLowerCase(Locale.ROOT);
            idLower = lower.endsWith("*") ? lower.substring(0, lower.length() - 1) : lower;
            norm = normalize(raw);
            compact = norm.replace(" ", "");
            words = norm.isEmpty() ? new String[0] : norm.split(" ");
            idQuery = BLUEPRINT_ID_QUERY.matcher(raw).matches();
        }
    }

    private static final class IndexedEntry {
        final ItemEntry entry;
        final String valueLower;
        final String titleNorm;
        final String titleCompact;
        final String subtitleCompact;
        final String allCompact;

        IndexedEntry(ItemEntry entry) {
            this.entry = entry;
            valueLower = entry.value.toLowerCase(Locale.ROOT);
            titleNorm = normalize(entry.title);
            titleCompact = titleNorm.replace(" ", "");
            subtitleCompact = compact(entry.subtitle);
            allCompact = titleCompact + " " + subtitleCompact + " " + valueLower;
        }
    }

    private record Scored(IndexedEntry entry, int rank) {
    }
}
