package com.gempukku.lotro.service;

import com.gempukku.lotro.at.AbstractAtTest;
import com.gempukku.lotro.db.PlayerDAO;
import com.gempukku.lotro.game.Player;
import com.gempukku.lotro.service.AdminLookupService.ItemEntry;
import com.gempukku.lotro.service.AdminLookupService.ItemKind;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The matching and ranking behind the admin add-items form: synthetic entries for the ranking rules, the real card
 * and product libraries for "does the admin find what they are looking for".
 */
public class AdminLookupServiceTest extends AbstractAtTest {

    private static ItemEntry card(String id, String title, String subtitle) {
        return new ItemEntry(id, ItemKind.CARD, title, subtitle, null, false);
    }

    private static ItemEntry alt(String id, String title) {
        return new ItemEntry(id, ItemKind.CARD, title, null, "alternate", true);
    }

    private static ItemEntry product(String name, ItemKind kind) {
        return new ItemEntry(name, kind, name, null, null, false);
    }

    private static List<String> values(List<ItemEntry> entries) {
        List<String> result = new ArrayList<>();
        for (ItemEntry e : entries)
            result.add(e.value());
        return result;
    }

    private static final List<ItemEntry> SYNTHETIC = List.of(
            card("1_1", "The One Ring", "The Ruling Ring"),
            card("1_10", "Ring of Barahir", null),
            card("1_2", "Gandalf", "The Grey Wizard"),
            card("1_100", "Úlairë Nertëa", "Messenger of Dol Guldur"),
            card("1_217", "Nazgûl Sword", null),
            card("6_1", "Between Nazgûl and Prey", null),
            card("2_85", "Witch-king", "Lord of the Nazgûl"),
            card("3_1", "Gorbag's Sword", null),
            alt("0_1", "Gandalf"),
            product("Random PC Full Art", ItemKind.AWARD),
            product("(S)FotR - Tengwar", ItemKind.SELECTION),
            product("FotR - Booster", ItemKind.PACK),
            product("Ring Pack", ItemKind.PACK)
    );

    // ---- normalisation ----

    @Test
    public void normalizeStripsAccentsCaseAndPunctuation() {
        assertEquals("nazgul", AdminLookupService.normalize("Nazgûl"));
        assertEquals("ulaire nertea", AdminLookupService.normalize("Úlairë Nertëa"));
        assertEquals("witch king", AdminLookupService.normalize("Witch-king"));
        assertEquals("witchking", AdminLookupService.compact("Witch-king"));
        assertEquals("gorbags sword", AdminLookupService.normalize("Gorbag’s Sword"));
        assertEquals("s fotr tengwar", AdminLookupService.normalize("(S)FotR - Tengwar"));
        assertEquals("", AdminLookupService.normalize(null));
    }

    // ---- ranking ----

    @Test
    public void accentInsensitiveTitleMatch() {
        var hits = values(AdminLookupService.rankItems("nazgul", SYNTHETIC, 10));
        assertTrue(hits.contains("1_217"));
        assertTrue(hits.contains("6_1"));
        assertTrue("subtitle matches too", hits.contains("2_85"));
        assertEquals("title prefix before later word before subtitle", List.of("1_217", "6_1", "2_85"), hits);

        assertEquals(List.of("1_100"), values(AdminLookupService.rankItems("ulaire", SYNTHETIC, 10)));
        assertEquals("typing the accent works as well", List.of("1_217", "6_1", "2_85"),
                values(AdminLookupService.rankItems("Nazgûl", SYNTHETIC, 10)));
    }

    @Test
    public void punctuationAndSpacingDoNotMatter() {
        assertEquals("2_85", AdminLookupService.rankItems("witchking", SYNTHETIC, 10).getFirst().value());
        assertEquals("2_85", AdminLookupService.rankItems("witch king", SYNTHETIC, 10).getFirst().value());
        assertEquals("3_1", AdminLookupService.rankItems("gorbags", SYNTHETIC, 10).getFirst().value());
    }

    @Test
    public void blueprintIdMatch() {
        var hits = values(AdminLookupService.rankItems("1_1", SYNTHETIC, 10));
        assertEquals("exact id first, then ids starting with it in set/number order", List.of("1_1", "1_10", "1_100"), hits);
        assertEquals("a foil star is ignored for matching", "1_1", AdminLookupService.rankItems("1_1*", SYNTHETIC, 10).getFirst().value());
        assertEquals("id query never matches titles or products", List.of("6_1"), values(AdminLookupService.rankItems("6_1", SYNTHETIC, 10)));
        assertEquals(List.of("0_1"), values(AdminLookupService.rankItems("0_1", SYNTHETIC, 10)));
    }

    @Test
    public void rankingOrder() {
        // "ring": exact title? no. Prefix: "Ring of Barahir", "Ring Pack". Later word: "The One Ring". Subtitle: "The Ruling Ring" (1_1 already matched)
        var hits = values(AdminLookupService.rankItems("ring", SYNTHETIC, 10));
        assertEquals(List.of("Ring Pack", "1_10", "1_1"), hits);

        // exact title beats everything; the base card beats its alternate id
        hits = values(AdminLookupService.rankItems("gandalf", SYNTHETIC, 10));
        assertEquals(List.of("1_2", "0_1"), hits);

        // substring inside a word ranks after prefix / word-prefix matches
        hits = values(AdminLookupService.rankItems("arahir", SYNTHETIC, 10));
        assertEquals(List.of("1_10"), hits);

        // every word somewhere in title / subtitle
        hits = values(AdminLookupService.rankItems("gandalf grey", SYNTHETIC, 10));
        assertEquals(List.of("1_2"), hits);
        assertTrue(AdminLookupService.rankItems("gandalf balrog", SYNTHETIC, 10).isEmpty());
    }

    @Test
    public void productsAndSelectionsAreReachable() {
        var hits = AdminLookupService.rankItems("tengwar", SYNTHETIC, 10);
        assertEquals("(S)FotR - Tengwar", hits.getFirst().value());
        assertEquals(ItemKind.SELECTION, hits.getFirst().kind());

        assertEquals("(S)FotR - Tengwar", AdminLookupService.rankItems("(S)FotR - Tengwar", SYNTHETIC, 10).getFirst().value());
        assertEquals("Random PC Full Art", AdminLookupService.rankItems("random pc", SYNTHETIC, 10).getFirst().value());
        assertEquals("FotR - Booster", AdminLookupService.rankItems("fotr boost", SYNTHETIC, 10).getFirst().value());
    }

    @Test
    public void limits() {
        List<ItemEntry> many = new ArrayList<>();
        for (int i = 1; i <= 120; i++)
            many.add(card("7_" + i, "Orc Soldier " + i, null));
        assertEquals(5, AdminLookupService.rankItems("orc", many, 5).size());
        assertEquals("0 means the default", AdminLookupService.DEFAULT_LIMIT, AdminLookupService.rankItems("orc", many, 0).size());
        assertEquals("capped", AdminLookupService.MAX_LIMIT, AdminLookupService.rankItems("orc", many, 1000).size());
        assertEquals("7_1", AdminLookupService.rankItems("orc", many, 5).getFirst().value());
        assertTrue(AdminLookupService.rankItems("", many, 5).isEmpty());
        assertTrue(AdminLookupService.rankItems("   ", many, 5).isEmpty());
        assertTrue(AdminLookupService.rankItems("--", many, 5).isEmpty());
        assertTrue(AdminLookupService.rankItems(null, many, 5).isEmpty());
    }

    // ---- the real libraries ----

    @Test
    public void realLibrarySearch() {
        var service = new AdminLookupService(_cardLibrary, _productLibrary, null);

        var nazgul = service.searchItems("nazgul", 50);
        assertFalse(nazgul.isEmpty());
        assertTrue("finds Nazgûl Sword", nazgul.stream().anyMatch(e -> e.title().equals("Nazgûl Sword")));
        assertTrue("cards and products in one list", nazgul.stream().anyMatch(e -> e.kind() == ItemKind.PACK
                && e.value().equals("The Great Eye - Dwarves+Gondor/Nazgul Starter")));
        assertEquals("a card whose title starts with it ranks first", ItemKind.CARD, nazgul.getFirst().kind());

        var oneOne = service.searchItems("1_1", 5);
        assertEquals("1_1", oneOne.getFirst().value());
        assertEquals("The One Ring", oneOne.getFirst().title());
        assertEquals(ItemKind.CARD, oneOne.getFirst().kind());
        assertNotNull("set name shown for disambiguation", oneOne.getFirst().detail());

        var tengwar = service.searchItems("(S)FotR - Tengwar", 5);
        assertEquals("(S)FotR - Tengwar", tengwar.getFirst().value());
        assertEquals(ItemKind.SELECTION, tengwar.getFirst().kind());

        var fullArt = service.searchItems("random pc full art", 5);
        assertEquals("Random PC Full Art", fullArt.getFirst().value());
        assertEquals("openOnDelivery product is an award", ItemKind.AWARD, fullArt.getFirst().kind());
        assertEquals("opened on delivery", fullArt.getFirst().detail());

        var booster = service.searchItems("FotR - Booster", 5);
        assertEquals("FotR - Booster", booster.getFirst().value());
        assertEquals(ItemKind.PACK, booster.getFirst().kind());

        assertEquals(ItemKind.AWARD, service.searchItems("Random Rare Foil", 1).getFirst().kind());

        // placeholders are never offered
        assertTrue(service.searchItems("404_", 50).isEmpty());
        assertTrue(service.searchItems("Future Prize", 50).stream().noneMatch(e -> e.value().startsWith("404_")));
    }

    @Test
    public void everySearchResultIsAcceptedByTheItemCheck() {
        var service = new AdminLookupService(_cardLibrary, _productLibrary, null);
        for (String q : List.of("gandalf", "nazgul", "1_1", "tengwar", "booster", "random", "starter", "selection")) {
            for (var entry : service.searchItems(q, 50)) {
                var check = service.checkItemLine("2x" + entry.value());
                assertNull(q + " -> " + entry.value() + ": " + check.problem(), check.problem());
                assertEquals(entry.kind(), check.kind());
                assertEquals(2, check.count());
                assertEquals(entry.value(), check.value());
            }
        }
    }

    @Test
    public void itemCheck() {
        var service = new AdminLookupService(_cardLibrary, _productLibrary, null);
        var card = service.checkItemLine("3x1_1");
        assertNull(card.problem());
        assertEquals(ItemKind.CARD, card.kind());
        assertEquals(3, card.count());
        assertEquals("1_1", card.value());

        assertNull(service.checkItemLine("1x1_1*").problem());
        assertEquals(ItemKind.AWARD, service.checkItemLine("1xRandom PC Full Art").kind());
        assertEquals(ItemKind.SELECTION, service.checkItemLine("1x(S)FotR - Tengwar").kind());
        assertEquals(ItemKind.PACK, service.checkItemLine("2xFotR - Booster").kind());

        assertNotNull(service.checkItemLine("1xNo Such Pack").problem());
        assertNotNull(service.checkItemLine("1x99_9999").problem());
        assertNotNull(service.checkItemLine("0x1_1").problem());
        assertNotNull(service.checkItemLine("ax1_1").problem());
        assertNotNull("placeholders are handed out as promises", service.checkItemLine("1x404_1").problem());
    }

    // ---- players ----

    @Test
    public void playerRanking() {
        var names = List.of("xKetura", "Ketura", "ketura_alt", "Bob", "KeturaFan", "aKETURAb", "Ketura");
        assertEquals(List.of("Ketura", "KeturaFan", "ketura_alt", "xKetura", "aKETURAb"),
                AdminLookupService.rankPlayerNames("ketura", names, 10));
        assertEquals(List.of("Ketura", "KeturaFan"), AdminLookupService.rankPlayerNames("KETURA", names, 2));
        assertEquals(List.of("Bob"), AdminLookupService.rankPlayerNames("ob", names, 10));
        assertTrue(AdminLookupService.rankPlayerNames("zzz", names, 10).isEmpty());
        assertTrue(AdminLookupService.rankPlayerNames("", names, 10).isEmpty());
        assertEquals("underscores are literal", List.of("ketura_alt"), AdminLookupService.rankPlayerNames("a_a", names, 10));
    }

    @Test
    public void splitPlayerList() {
        assertEquals(List.of("a", "b", "c", "d", "e"), AdminLookupService.splitPlayerList("a\nb, c;d \r\n  e \n\n"));
        assertTrue(AdminLookupService.splitPlayerList(null).isEmpty());
        assertTrue(AdminLookupService.splitPlayerList(" \n ,").isEmpty());
    }

    @Test
    public void resolvePlayers() {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Player ketura = new Player(1, "Ketura", "p", "u", null, null, null, null, false);
        Mockito.when(dao.getPlayer("Ketura")).thenReturn(ketura);
        Mockito.when(dao.findPlayerNames(Mockito.eq("ketura"), Mockito.anyInt())).thenReturn(List.of("Ketura", "KeturaFan"));
        Mockito.when(dao.findPlayerNames(Mockito.eq("ketur"), Mockito.anyInt())).thenReturn(List.of("Ketura", "KeturaFan"));
        Mockito.when(dao.findPlayerNames(Mockito.eq("nobody"), Mockito.anyInt())).thenReturn(List.of());

        var service = new AdminLookupService(null, null, dao);
        var result = service.resolvePlayers(List.of("Ketura", "ketura", "ketur", "nobody", " ", "KETURA"));
        assertEquals("duplicates (ignoring case) reported once", 3, result.size());

        assertEquals("Ketura", result.get(0).name());
        assertEquals("ketur", result.get(1).input());
        assertNull(result.get(1).name());
        assertEquals(List.of("Ketura", "KeturaFan"), result.get(1).suggestions());
        assertNull(result.get(2).name());
        assertTrue(result.get(2).suggestions().isEmpty());

        // a case-insensitive database miss still resolves when the search finds the exact name in another case
        var caseOnly = service.resolvePlayers(List.of("ketura"));
        assertEquals("Ketura", caseOnly.getFirst().name());
    }

    @Test
    public void searchPlayersRanksWhatTheDatabaseReturns() {
        PlayerDAO dao = Mockito.mock(PlayerDAO.class);
        Mockito.when(dao.findPlayerNames(Mockito.eq("ket"), Mockito.anyInt())).thenReturn(List.of("xKet", "Ketura", "Ket"));
        var service = new AdminLookupService(null, null, dao);
        assertEquals(List.of("Ket", "Ketura", "xKet"), service.searchPlayers("ket", 10));
        assertEquals(List.of("Ket"), service.searchPlayers(" ket ", 1));
        assertTrue(service.searchPlayers("  ", 10).isEmpty());
        Mockito.verify(dao, Mockito.never()).findPlayerNames(Mockito.eq(""), Mockito.anyInt());
    }
}
