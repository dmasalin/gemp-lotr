package com.gempukku.lotro.cards.set6.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.ExhaustCharacterEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromHandEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Ents of Fangorn
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: Plays to your support area. Regroup: Exert a [SAURON] Orc to play a Nazgul or [SAURON] minion.
 * Its twilight cost is -4 and it comes into play exhausted.
 */
public class Card6_105 extends AbstractPermanent {
    public Card6_105() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.SAURON, "Peril");
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(final String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)
                && PlayConditions.canExert(self, game, Culture.SAURON, Race.ORC)
                && PlayConditions.canPlayFromHand(playerId, game, -4, CardType.MINION, Filters.or(Race.NAZGUL, Culture.SAURON))) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.SAURON, Race.ORC));
            action.appendEffect(
                    new ChooseAndPlayCardFromHandEffect(playerId, game, -4, CardType.MINION, Filters.or(Race.NAZGUL, Culture.SAURON)) {
                        @Override
                        protected void afterCardPlayed(PhysicalCard cardPlayed) {
                            action.appendEffect(
                                    new ExhaustCharacterEffect(self, action, cardPlayed));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
