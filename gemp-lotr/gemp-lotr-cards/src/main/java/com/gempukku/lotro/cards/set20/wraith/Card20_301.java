package com.gempukku.lotro.cards.set20.wraith;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * 2
 * Stricken Dumb
 * Ringwraith	Event • Maneuver
 * Exert The Witch-king to discard a tale or weapon.
 */
public class Card20_301 extends AbstractEvent {
    public Card20_301() {
        super(Side.SHADOW, 2, Culture.WRAITH, "Stricken Dumb", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Filters.witchKing);
    }

    @Override
    public PlayEventAction getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.witchKing));
        action.appendEffect(
                new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, Filters.or(Keyword.TALE, Filters.weapon)));
        return action;
    }
}
