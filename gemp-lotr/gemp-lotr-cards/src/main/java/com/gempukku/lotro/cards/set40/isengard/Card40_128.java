package com.gempukku.lotro.cards.set40.isengard;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Title: My Fighting Uruk-hai
 * Set: Second Edition
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 1
 * Type: Event - Maneuver
 * Card Number: 1C128
 * Game Text: Exert an Uruk-hai to discard a Free Peoples condition for each battleground in the current region.
 */
public class Card40_128 extends AbstractEvent {
    public Card40_128() {
        super(Side.SHADOW, 1, Culture.ISENGARD, "My Fighting Uruk-hai", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Race.URUK_HAI);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.URUK_HAI));
        int count = Filters.countSpottable(game, Keyword.BATTLEGROUND, Filters.currentRegion);
        action.appendEffect(
                new ChooseAndDiscardCardsFromPlayEffect(action, playerId, count, count, Side.FREE_PEOPLE, CardType.CONDITION));
        return action;
    }
}