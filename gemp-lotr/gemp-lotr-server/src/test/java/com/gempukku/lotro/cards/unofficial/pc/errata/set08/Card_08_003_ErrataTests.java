package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_003_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "58_3");
					put("gimli", "8_5");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BloodRunsChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Blood Runs Chill
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If the fellowship moves, spot a Dwarf who is damage +X and exert that Dwarf to make an opponent hinder X Shadow cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Blood Runs Chill", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BloodRunsChillTest1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(card);
		scn.MoveCardsToSupportArea(runner);

		scn.StartGame();
		scn.SkipToMovementDecision();

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.IsHindered(runner));
		scn.FreepsChooseToMove();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsHindered(runner));
	}
}
