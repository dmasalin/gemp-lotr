package com.gempukku.lotro.cards.set20.moria;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * 14
 * •The Balrog, Dread Servant of Melkor
 * Moria	Minion • Balrog
 * 17	5	4
 * Damage +1. Fierce.
 * To play, spot a [Moria] minion.
 * If the fellowship has moved more than once this turn, the Balrog's twilight cost is -4.
 * Regroup: Exert the Balrog four times to make the Free Peoples player
 * discard a companion (except the Ring-bearer).
 */
public class Card20_254 extends AbstractMinion {
    public Card20_254() {
        super(14, 17, 5, 4, Race.BALROG, Culture.MORIA, "The Balrog", "Dread Servant of Melkor", true);
        addKeyword(Keyword.DAMAGE, 1);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.MORIA, CardType.MINION);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self) {
        return (game.getGameState().getMoveCount() > 1) ? -4 : 0;
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)
            && PlayConditions.canSelfExert(self, 4, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndDiscardCardsFromPlayEffect(action, game.getGameState().getCurrentPlayerId(), 1, 1, CardType.COMPANION, Filters.not(Filters.ringBearer)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
