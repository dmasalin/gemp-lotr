package com.gempukku.lotro.cards.set2.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfTurnModifierEffect;
import com.gempukku.lotro.logic.modifiers.TwilightCostModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Mines of Moria
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Stealth. Regroup: Spot 2 Hobbits to make each site's Shadow number -2 until the end of the turn.
 */
public class Card2_099 extends AbstractEvent {
    public Card2_099() {
        super(Side.FREE_PEOPLE, 0, Culture.SHIRE, "Deft in Their Movements", Phase.REGROUP);
        addKeyword(Keyword.STEALTH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Race.HOBBIT);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new AddUntilEndOfTurnModifierEffect(
                        new TwilightCostModifier(self, Filters.in(Filters.filterActive(game, CardType.SITE)), -2)));
        return action;
    }
}
