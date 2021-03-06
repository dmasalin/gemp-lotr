package com.gempukku.lotro.cards.set5.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 7
 * Type: Minion • Man
 * Strength: 14
 * Vitality: 4
 * Site: 4
 * Game Text: Southron. Skirmish: Remove (3) to play a [RAIDER] mount from your discard pile.
 */
public class Card5_070 extends AbstractMinion {
    public Card5_070() {
        super(7, 14, 4, 4, Race.MAN, Culture.RAIDER, "Army of Haradrim", null, true);
        addKeyword(Keyword.SOUTHRON);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 3)
                && PlayConditions.canPlayFromDiscard(playerId, game, 3, 0, Culture.RAIDER, PossessionClass.MOUNT)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(3));
            action.appendEffect(
                    new ChooseAndPlayCardFromDiscardEffect(playerId, game, Culture.RAIDER, PossessionClass.MOUNT));
            return Collections.singletonList(action);
        }
        return null;
    }
}
