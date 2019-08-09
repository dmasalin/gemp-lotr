package com.gempukku.lotro.cards.set4.gondor;

import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.WoundCharactersEffect;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Maneuver: Exert a [GONDOR] Man to wound a minion once (or twice if that minion is an Uruk-hai).
 */
public class Card4_115 extends AbstractEvent {
    public Card4_115() {
        super(Side.FREE_PEOPLE, 0, Culture.GONDOR, "Defend It and Hope", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile)
                && PlayConditions.canExert(self, game, Culture.GONDOR, Race.MAN);
    }

    @Override
    public PlayEventAction getPlayCardAction(String playerId, LotroGame game, final PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.GONDOR, Race.MAN));
        action.appendEffect(
                new ChooseActiveCardEffect(self, playerId, "Choose minion", CardType.MINION, Filters.canTakeWounds(self, 1)) {
                    @Override
                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                        boolean urukHai = (card.getBlueprint().getRace() == Race.URUK_HAI);
                        action.insertEffect(
                                new WoundCharactersEffect(self, card));
                        if (urukHai)
                            action.insertEffect(
                                    new WoundCharactersEffect(self, card));
                    }
                });
        return action;
    }
}
