package com.gempukku.lotro.cards.set7.elven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.DrawCardsEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 2
 * Type: Condition • Support Area
 * Game Text: To play, spot 2 Elves. Response: If an [ELVEN] skirmish event is played, discard this condition
 * to draw 3 cards.
 */
public class Card7_022 extends AbstractPermanent {
    public Card7_022() {
        super(Side.FREE_PEOPLE, 2, CardType.CONDITION, Culture.ELVEN, "Hope Comes", null, true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, 2, Race.ELF);
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, Culture.ELVEN, CardType.EVENT, Keyword.SKIRMISH)
                && game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new DrawCardsEffect(action, playerId, 3));
            return Collections.singletonList(action);
        }
        return null;
    }
}
